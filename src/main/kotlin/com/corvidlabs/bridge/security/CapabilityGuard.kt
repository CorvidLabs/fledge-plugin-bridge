package com.corvidlabs.bridge.security

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.isDirectory

class CapabilityGuard(
    val allowRead: Boolean,
    val allowWrite: Boolean,
    val allowExec: Boolean,
    sandboxRoot: String,
) {
    val sandboxPath: Path = Path(sandboxRoot).absolute().normalize()

    fun assertRead() {
        check(allowRead) { "File read operations are not allowed in this session" }
    }

    fun assertWrite() {
        check(allowWrite) { "File write operations are not allowed in this session" }
    }

    fun assertExec() {
        check(allowExec) { "Command execution is not allowed in this session" }
    }

    fun validatePath(requestedPath: String): Path {
        val resolved = sandboxPath.resolve(requestedPath).normalize()
        require(resolved.startsWith(sandboxPath)) {
            "Path escapes sandbox: $requestedPath"
        }
        return resolved
    }

    fun validateCommand(command: String): String {
        val blocked = listOf("rm -rf /", "mkfs", "dd if=", "> /dev/sd", ":(){ :|:& };:")
        require(blocked.none { command.contains(it) }) {
            "Command blocked by safety filter"
        }
        return command
    }
}
