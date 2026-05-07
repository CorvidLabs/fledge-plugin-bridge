package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

/**
 * Stub: fledge plugins are ephemeral processes — there is no persistent
 * session whose status can be queried. Each `connect` invocation lives
 * for the duration of its WebSocket connection and exits when done. This
 * command exists so `fledge bridge status` prints a helpful message
 * rather than an "unknown subcommand" error.
 */
class StatusCommand(private val initMessage: InitMessage?) : CliktCommand(name = "status") {
    override fun help(context: Context) = "Show bridge connection status"

    override fun run() {
        FledgeProtocol.output("Bridge status: disconnected\n")
        FledgeProtocol.output("No active session.\n")
    }
}
