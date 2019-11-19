package cn.edu.tsinghua.sdfs.protocol.packet.impl


import cn.edu.tsinghua.sdfs.protocol.Codec.FILE_PACKET
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

data class FilePacket(val file: String, val fileLength:Long) : Packet {

    override val command: Int
        get() = FILE_PACKET
}
