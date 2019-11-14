package cn.edu.tsinghua.sdfs.protocol.packet.impl


import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.FILE_PACKET
import cn.edu.tsinghua.sdfs.protocol.packet.Packet
import java.io.File

data class FilePacket(val file: String, val fileLength:Long) : Packet {

    override val command: Int
        get() = FILE_PACKET
}
