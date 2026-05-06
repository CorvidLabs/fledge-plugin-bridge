package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand

class DisconnectCommand(private val initMessage: InitMessage?) : CliktCommand(
    name = "disconnect",
    help = "Disconnect active bridge session",
) {
    override fun run() {
        FledgeProtocol.output("No active bridge session to disconnect.\n")
    }
}
