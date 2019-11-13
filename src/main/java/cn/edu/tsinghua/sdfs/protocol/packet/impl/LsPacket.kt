package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.protocol.command.Command
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

class LsPacket() : Packet {
    constructor(path: String) : this() {
        this.path = path
    }

    var path = ""
        get
        set

    override val command: Int
        get() = Command.LS

}
