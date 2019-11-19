package cn.edu.tsinghua.sdfs.protocol.packet.impl


import cn.edu.tsinghua.sdfs.protocol.Codec.CREATE_REQUEST
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

class CreateRequest() : Packet {
    var remoteFile: String = ""
    var localFile: String = ""
    var fileLength: Long = 0

    constructor(localFile: String, remoteFile: String, fileLength:Long) : this() {
        this.localFile = localFile
        this.remoteFile = remoteFile
        this.fileLength = fileLength
    }

    override val command: Int
        get() = CREATE_REQUEST

}
