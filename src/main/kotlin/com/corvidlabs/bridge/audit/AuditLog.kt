package com.corvidlabs.bridge.audit

import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant

/**
 * Append-only NDJSON record of every accepted bridge request. The
 * developer can inspect it after the fact to see what the agent did.
 *
 * The log lives at $FLEDGE_BRIDGE_AUDIT_LOG if set, otherwise
 * `~/.fledge/bridge-audit.log`. We never log token / response bodies —
 * only request shape (type, path, command, exit code, duration).
 *
 * Failure to write to the audit log is non-fatal: we log to stderr and
 * keep going. Losing audit visibility is bad, but failing the user's
 * actual operation just because their disk is full is worse.
 */
object AuditLog {

    private val path: Path by lazy {
        val override = System.getenv("FLEDGE_BRIDGE_AUDIT_LOG")
        if (!override.isNullOrBlank()) {
            Path.of(override)
        } else {
            val home = System.getProperty("user.home")
            Path.of(home, ".fledge", "bridge-audit.log")
        }
    }

    fun record(
        requestId: String,
        type: String,
        sandbox: String,
        path: String? = null,
        command: String? = null,
        exitCode: Int? = null,
        durationMs: Long? = null,
        truncated: Boolean? = null,
        accepted: Boolean = true,
        error: String? = null,
    ) {
        try {
            val entry = buildJson {
                put("ts", Instant.now().toString())
                put("requestId", requestId)
                put("type", type)
                put("sandbox", sandbox)
                put("accepted", accepted)
                if (path != null) put("path", path)
                // Truncate command field at 256 chars — full command can be
                // huge and we don't want the log file to grow uncontrollably.
                if (command != null) {
                    val short = if (command.length > 256) command.take(256) + "…" else command
                    put("command", short)
                }
                if (exitCode != null) put("exitCode", exitCode.toString())
                if (durationMs != null) put("durationMs", durationMs.toString())
                if (truncated != null) put("truncated", truncated.toString())
                if (error != null) put("error", error)
            }
            val parent = this.path.parent
            if (parent != null && !Files.isDirectory(parent)) {
                Files.createDirectories(parent)
            }
            Files.writeString(
                this.path,
                entry + "\n",
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
            )
            // Tighten perms on first creation. Failure here is fine —
            // the user can chmod themselves.
            try {
                File(this.path.toString()).setReadable(false, false)
                File(this.path.toString()).setReadable(true, true)
                File(this.path.toString()).setWritable(false, false)
                File(this.path.toString()).setWritable(true, true)
            } catch (_: Exception) {}
        } catch (e: Exception) {
            System.err.println("[bridge audit] failed to write audit log: ${e.message}")
        }
    }

    // Tiny JSON builder that doesn't pull a dependency for one usage.
    private fun buildJson(block: JsonBuilder.() -> Unit): String {
        val b = JsonBuilder()
        b.block()
        return "{${b.parts.joinToString(",")}}"
    }

    private class JsonBuilder {
        val parts = mutableListOf<String>()
        fun put(key: String, value: String) {
            parts.add("${Json.encodeToString(key)}:${Json.encodeToString(value)}")
        }
        fun put(key: String, value: Boolean) {
            parts.add("${Json.encodeToString(key)}:$value")
        }
    }
}
