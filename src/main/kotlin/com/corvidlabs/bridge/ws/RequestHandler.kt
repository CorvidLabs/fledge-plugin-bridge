package com.corvidlabs.bridge.ws

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.security.CapabilityGuard
import kotlinx.serialization.json.*
import java.io.File
import java.util.concurrent.TimeUnit

class RequestHandler(private val capabilities: CapabilityGuard) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handle(request: BridgeRequest): BridgeResponse {
        return try {
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
    }

    private fun handleFileRead(request: BridgeRequest): BridgeResponse {
        capabilities.assertRead()
        val path = capabilities.validatePath(request.path ?: error("Missing path"))
        val file = path.toFile()

        if (!file.exists()) {
            return BridgeResponse(id = request.id, type = "file.read", success = false, error = "File not found")
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
            .start()

        val completed = process.waitFor(timeout, TimeUnit.MILLISECONDS)
        if (!completed) {
            process.destroyForcibly()
            return BridgeResponse(id = request.id, type = "exec", success = false, error = "Command timed out")
        }

        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val exitCode = process.exitValue()

        return BridgeResponse(
            id = request.id,
            type = "exec",
            success = exitCode == 0,
            data = buildJsonObject {
                put("code", exitCode)
                put("stdout", stdout)
                put("stderr", stderr)
            },
        )
    }
}
