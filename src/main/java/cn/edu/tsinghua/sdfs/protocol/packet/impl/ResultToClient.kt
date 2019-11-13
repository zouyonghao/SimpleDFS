package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.RESULT
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

class ResultToClient() : Packet {
    var result: String = ""

    constructor(result: String) : this() {
        this.result = result
    }

    override val command: Int
        get() = RESULT

}
