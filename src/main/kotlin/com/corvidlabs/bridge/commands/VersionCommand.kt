package com.corvidlabs.bridge.commands

import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

class VersionCommand(private val initMessage: InitMessage?) : CliktCommand(name = "version") {
    override fun help(context: Context) = "Show plugin version"

    override fun run() {
        // Prefer the version from the protocol init message (set by fledge
        // from plugin.toml) so we never drift from the declared version.
        val version = initMessage?.plugin?.version ?: "0.4.0"
        FledgeProtocol.output("fledge-plugin-bridge $version\n")
    }
}
