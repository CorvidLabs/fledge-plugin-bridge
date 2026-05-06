package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand

class StatusCommand(private val initMessage: InitMessage?) : CliktCommand(
    name = "status",
    help = "Show bridge connection status",
) {
    override fun run() {
        FledgeProtocol.output("Bridge status: disconnected\n")
        FledgeProtocol.output("No active session.\n")
    }
}
