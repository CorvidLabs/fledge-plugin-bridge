package com.corvidlabs.bridge.cli

import com.corvidlabs.bridge.commands.ConnectCommand
import com.corvidlabs.bridge.commands.DisconnectCommand
import com.corvidlabs.bridge.commands.StatusCommand
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

class BridgeCli(private val initMessage: InitMessage?) : CliktCommand(
    name = "bridge",
    help = "Bridge your local dev environment to corvid-agent",
) {
    init {
        subcommands(
            ConnectCommand(initMessage),
            DisconnectCommand(initMessage),
            StatusCommand(initMessage),
        )
    }

    override fun run() = Unit
}
