package com.corvidlabs.bridge

import com.corvidlabs.bridge.cli.BridgeCli
import com.corvidlabs.bridge.protocol.FledgeProtocol
import com.corvidlabs.bridge.protocol.InitMessage

fun main(args: Array<String>) {
    val initMessage = FledgeProtocol.readInit()
    if (initMessage != null) {
        BridgeCli(initMessage).main(initMessage.args)
    } else {
        BridgeCli(null).main(args)
    }
}
