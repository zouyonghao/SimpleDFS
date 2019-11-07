package cn.edu.tsinghua.sdfs.protocol


import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.FILE_PACKET
import java.io.File

data class FilePacket(var file: File?) : Packet() {


    constructor() : this(null)

    override val command: Int
        get() = FILE_PACKET

}
