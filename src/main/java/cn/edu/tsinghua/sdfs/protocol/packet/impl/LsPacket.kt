package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.protocol.Codec.LS
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

class LsPacket() : Packet {
    constructor(path: String) : this() {
        this.path = path
    }

    var path = ""

    override val command: Int
        get() = LS

}
