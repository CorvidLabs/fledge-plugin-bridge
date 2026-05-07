package com.corvidlabs.bridge.cli

import com.corvidlabs.bridge.commands.ConnectCommand
import com.corvidlabs.bridge.commands.DisconnectCommand
import com.corvidlabs.bridge.commands.StatusCommand
import com.corvidlabs.bridge.commands.VersionCommand
import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

class BridgeCli(private val initMessage: InitMessage?) : CliktCommand(name = "bridge") {
    // Allow running without a subcommand so we can handle help ourselves
    // via the fledge-v1 JSON protocol instead of Clikt printing raw text.
    override val invokeWithoutSubcommand = true
    override val printHelpOnEmptyArgs = false

    init {
        subcommands(
            ConnectCommand(initMessage),
            DisconnectCommand(initMessage),
            StatusCommand(initMessage),
            VersionCommand(initMessage),
        )
    }

    override fun help(context: Context) = "Bridge your local dev environment to corvid-agent"

    override fun run() {
        // When invoked without a subcommand, send help text through the
        // fledge-v1 JSON protocol so it doesn't corrupt the stream.
        // If a subcommand IS specified, Clikt will dispatch to it after
        // this method returns — do nothing here in that case.
        if (currentContext.invokedSubcommand == null) {
            val helpText = buildString {
                appendLine("fledge bridge — Bridge your local dev environment to corvid-agent")
                appendLine()
                appendLine("Usage: fledge bridge <command>")
                appendLine()
                appendLine("Commands:")
                appendLine("  connect      Connect to a corvid-agent server via WebSocket")
                appendLine("  disconnect   Disconnect active bridge session")
                appendLine("  status       Show bridge connection status")
                appendLine("  version      Show plugin version")
            }
            FledgeProtocol.output(helpText)
        }
    }
}
