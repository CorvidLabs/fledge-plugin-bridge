package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

class DisconnectCommand(private val initMessage: InitMessage?) : CliktCommand(name = "disconnect") {
    override fun help(context: Context) = "Disconnect active bridge session"

    override fun run() {
        FledgeProtocol.output("No active bridge session to disconnect.\n")
    }
}
