package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.protocol.Codec.USER_PROGRAM
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

data class UserProgram(val id: String, val content: String) : Packet {
    override val command: Int
        get() = USER_PROGRAM
}