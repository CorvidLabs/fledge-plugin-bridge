package com.corvidlabs.bridge.ws

import com.corvidlabs.bridge.audit.AuditLog
import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.security.CapabilityGuard
import kotlinx.serialization.json.*
import java.io.File
import java.util.concurrent.TimeUnit

class RequestHandler(private val capabilities: CapabilityGuard) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handle(request: BridgeRequest): BridgeResponse {
        val started = System.currentTimeMillis()
        val response = try {
            when (request.type) {
                "file.read" -> handleFileRead(request)
                "file.write" -> handleFileWrite(request)
                "file.list" -> handleFileList(request)
                "exec" -> handleExec(request)
                "ping" -> BridgeResponse(id = request.id, type = "pong", success = true)
                else -> BridgeResponse(
                    id = request.id,
                    type = "error",
                    success = false,
                    error = "Unknown request type: ${request.type}",
                )
            }
        } catch (e: IllegalStateException) {
            BridgeResponse(id = request.id, type = "error", success = false, error = e.message)
        } catch (e: IllegalArgumentException) {
            BridgeResponse(id = request.id, type = "error", success = false, error = e.message)
        } catch (e: Exception) {
            FledgeProtocol.error("Request ${request.id} failed: ${e.message}")
            BridgeResponse(id = request.id, type = "error", success = false, error = "Internal error")
        }
        val truncated = (response.data as? JsonObject)?.get("truncated")?.jsonPrimitive?.booleanOrNull
        val exitCode = (response.data as? JsonObject)?.get("code")?.jsonPrimitive?.intOrNull
        AuditLog.record(
            requestId = request.id,
            type = request.type,
            sandbox = capabilities.sandboxPath.toString(),
            path = request.path,
            command = request.command,
            exitCode = exitCode,
            durationMs = System.currentTimeMillis() - started,
            truncated = truncated,
            accepted = response.success,
            error = response.error,
        )
        return response
    }

    private fun handleFileRead(request: BridgeRequest): BridgeResponse {
        capabilities.assertRead()
        val path = capabilities.validatePath(request.path ?: error("Missing path"))
        val file = path.toFile()

        if (!file.exists()) {
            return BridgeResponse(id = request.id, type = "file.read", success = false, error = "File not found")
        }

        if (file.length() > MAX_FILE_READ_BYTES) {
            return BridgeResponse(
                id = request.id,
                type = "file.read",
                success = false,
                error = "File exceeds maximum read size of ${MAX_FILE_READ_BYTES / (1024 * 1024)} MiB",
            )
        }

        val content = file.readText()
        return BridgeResponse(
            id = request.id,
            type = "file.read",
            success = true,
            data = JsonPrimitive(content),
        )
    }

    private fun handleFileWrite(request: BridgeRequest): BridgeResponse {
        capabilities.assertWrite()
        val path = capabilities.validatePath(request.path ?: error("Missing path"))
        val content = request.content ?: error("Missing content")

        path.parent?.toFile()?.mkdirs()
        path.toFile().writeText(content)

        return BridgeResponse(id = request.id, type = "file.write", success = true)
    }

    private fun handleFileList(request: BridgeRequest): BridgeResponse {
        capabilities.assertRead()
        val path = capabilities.validatePath(request.path ?: ".")
        val dir = path.toFile()

        if (!dir.isDirectory) {
            return BridgeResponse(id = request.id, type = "file.list", success = false, error = "Not a directory")
        }

        val entries = dir.listFiles()?.map { f ->
            buildJsonObject {
                put("name", f.name)
                put("type", if (f.isDirectory) "directory" else "file")
                put("size", f.length())
            }
        } ?: emptyList()

        return BridgeResponse(
            id = request.id,
            type = "file.list",
            success = true,
            data = JsonArray(entries),
        )
    }

    private fun handleExec(request: BridgeRequest): BridgeResponse {
        capabilities.assertExec()
        val command = capabilities.validateCommand(request.command ?: error("Missing command"))
        val cwd = request.cwd?.let { capabilities.validatePath(it).toFile() } ?: capabilities.sandboxPath.toFile()
        val timeout = request.timeout ?: 30_000L

        val process = ProcessBuilder("sh", "-c", command)
            .directory(cwd)
            .redirectErrorStream(false)
            .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
            .start()

        // Read stdout/stderr concurrently with waitFor so commands that
        // produce more than the OS pipe buffer (~64 KB) don't deadlock.
        // Each stream is capped at MAX_OUTPUT_BYTES and excess is dropped
        // with a tail marker — adversarial output can't OOM the JVM.
        val stdoutCollector = drainBounded(process.inputStream)
        val stderrCollector = drainBounded(process.errorStream)

        val completed = process.waitFor(timeout, TimeUnit.MILLISECONDS)
        if (!completed) {
            process.destroyForcibly()
            // Wait briefly so the drain threads see EOF and finish
            stdoutCollector.thread.join(500)
            stderrCollector.thread.join(500)
            return BridgeResponse(
                id = request.id,
                type = "exec",
                success = false,
                error = "Command timed out",
                data = buildJsonObject {
                    put("code", -1)
                    put("stdout", stdoutCollector.text())
                    put("stderr", stderrCollector.text())
                    put("truncated", stdoutCollector.truncated || stderrCollector.truncated)
                },
            )
        }

        stdoutCollector.thread.join()
        stderrCollector.thread.join()
        val exitCode = process.exitValue()

        return BridgeResponse(
            id = request.id,
            type = "exec",
            success = exitCode == 0,
            data = buildJsonObject {
                put("code", exitCode)
                put("stdout", stdoutCollector.text())
                put("stderr", stderrCollector.text())
                put("truncated", stdoutCollector.truncated || stderrCollector.truncated)
            },
        )
    }

    private companion object {
        private const val MAX_OUTPUT_BYTES = 1 * 1024 * 1024 // 1 MiB per stream
        private const val MAX_FILE_READ_BYTES = 100L * 1024 * 1024 // 100 MiB
    }

    private class DrainResult(
        val thread: Thread,
        private val buffer: StringBuilder,
    ) {
        @Volatile var truncated: Boolean = false
        fun text(): String = synchronized(buffer) { buffer.toString() }
    }

    private fun drainBounded(stream: java.io.InputStream): DrainResult {
        val buffer = StringBuilder()
        val resultRef = arrayOf<DrainResult?>(null)
        val thread = Thread {
            stream.bufferedReader().use { reader ->
                val chunk = CharArray(8 * 1024)
                while (true) {
                    val n = try { reader.read(chunk) } catch (_: Exception) { -1 }
                    if (n < 0) break
                    val current = resultRef[0]
                    synchronized(buffer) {
                        if (buffer.length + n > MAX_OUTPUT_BYTES) {
                            val take = (MAX_OUTPUT_BYTES - buffer.length).coerceAtLeast(0)
                            if (take > 0) buffer.appendRange(chunk, 0, take)
                            current?.truncated = true
                        } else {
                            buffer.appendRange(chunk, 0, n)
                        }
                    }
                    if (current?.truncated == true) {
                        // Drain the rest so the child process doesn't block
                        // on a full pipe; we just discard the bytes.
                        while (reader.read(chunk) >= 0) { /* drain */ }
                        break
                    }
                }
            }
        }.apply { isDaemon = true }
        val result = DrainResult(thread, buffer)
        resultRef[0] = result
        thread.start()
        return result
    }
}
