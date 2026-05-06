package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

class StatusCommand(private val initMessage: InitMessage?) : CliktCommand(name = "status") {
    override fun help(context: Context) = "Show bridge connection status"

    override fun run() {
        FledgeProtocol.output("Bridge status: disconnected\n")
        FledgeProtocol.output("No active session.\n")
    }
}
