package cn.edu.tsinghua.sdfs.protocol.packet.impl


import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.CREATE_REQUEST
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

class CreateRequest() : Packet {
    var remoteFile: String = ""
    var localFile: String = ""
    var fileSize: Long = 0

    constructor(localFile: String, remoteFile: String, fileSize:Long) : this() {
        this.localFile = localFile
        this.remoteFile = remoteFile
        this.fileSize = fileSize
    }

    override val command: Int
        get() = CREATE_REQUEST

}
