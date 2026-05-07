package com.corvidlabs.bridge

import com.corvidlabs.bridge.cli.BridgeCli
import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ContextCliktError
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.parse

fun main(args: Array<String>) {
    val initMessage = FledgeProtocol.readInit()
    val cli = BridgeCli(initMessage)
    val effectiveArgs = if (initMessage != null) initMessage.args else args.toList()

    try {
        cli.parse(effectiveArgs)
    } catch (e: PrintHelpMessage) {
        // Route Clikt's help output through the fledge-v1 JSON protocol
        // instead of letting it write raw text to stdout.
        val helpText = (e as? ContextCliktError)?.context?.command?.getFormattedHelp(e)
            ?: "No help available."
        FledgeProtocol.output(helpText + "\n")
    } catch (e: PrintMessage) {
        val msg = e.message
        if (!msg.isNullOrEmpty()) {
            FledgeProtocol.output(msg + "\n")
        }
    } catch (e: CliktError) {
        // Usage errors and other Clikt errors — route through protocol.
        val msg = if (e is ContextCliktError) {
            e.context?.command?.getFormattedHelp(e) ?: e.message ?: "Unknown error"
        } else {
            e.message ?: "Unknown error"
        }
        FledgeProtocol.error(msg)
    }
}
