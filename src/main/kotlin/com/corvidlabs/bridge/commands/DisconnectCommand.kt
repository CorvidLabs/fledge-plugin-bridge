package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

/**
 * Stub: fledge plugins are ephemeral processes — there is no persistent
 * session to disconnect. The `connect` subcommand opens a WebSocket,
 * runs the request loop, and exits when the server closes the connection.
 * This command exists so `fledge bridge disconnect` prints a helpful
 * message rather than an "unknown subcommand" error.
 */
class DisconnectCommand(private val initMessage: InitMessage?) : CliktCommand(name = "disconnect") {
    override fun help(context: Context) = "Disconnect active bridge session"

    override fun run() {
        FledgeProtocol.output("No active bridge session to disconnect.\n")
    }
}
