package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

/**
 * Stub: fledge plugins are ephemeral processes — there is no persistent
 * session whose status can be queried. Each `connect` invocation lives
 * for the duration of its WebSocket connection and exits when done. This
 * command exists so `fledge bridge status` prints a helpful message
 * rather than an "unknown subcommand" error.
 */
class StatusCommand(private val initMessage: InitMessage?) : CliktCommand(name = "status") {
    override fun help(context: Context) = "Show bridge connection status"

    private val json by option("--json", help = "Output status as JSON").flag()

    override fun run() {
        if (json) {
            FledgeProtocol.output("""{"status":"disconnected","session":null}""" + "\n")
        } else {
            FledgeProtocol.output("Bridge status: disconnected\n")
            FledgeProtocol.output("No active session.\n")
        }
    }
}
