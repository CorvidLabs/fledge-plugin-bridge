package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.corvidlabs.bridge.security.CapabilityGuard
import com.corvidlabs.bridge.ws.BridgeClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

class ConnectCommand(private val initMessage: InitMessage?) : CliktCommand(name = "connect") {
    override fun help(context: Context) = "Connect to a corvid-agent server via WebSocket"

    private val server by option("--server", "-s", help = "Server URL (e.g. ws://localhost:3000)")
        .required()

    private val token by option("--token", "-t", help = "Authentication token")
        .required()

    private val project by option("--project", "-p", help = "Project ID to scope the bridge to")
        .default("")

    private val sandboxPath by option("--sandbox", help = "Root directory to sandbox file operations to")
        .default(".")

    private val allowRead by option("--allow-read", help = "Allow file read operations").default("true")
    private val allowWrite by option("--allow-write", help = "Allow file write operations").default("false")
    private val allowExec by option("--allow-exec", help = "Allow command execution").default("false")

    override fun run() = runBlocking {
        val capabilities = CapabilityGuard(
            allowRead = allowRead.toBoolean(),
            allowWrite = allowWrite.toBoolean(),
            allowExec = allowExec.toBoolean(),
            sandboxRoot = sandboxPath,
        )

        FledgeProtocol.output("Connecting to $server...\n")

        val client = BridgeClient(
            serverUrl = server,
            token = token,
            projectId = project,
            capabilities = capabilities,
        )

        client.connect()
    }
}
