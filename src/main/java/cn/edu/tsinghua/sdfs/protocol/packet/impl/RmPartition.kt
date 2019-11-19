package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.protocol.Codec.RM_PARTITION
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

data class RmPartition(val file: String, val it: Server):Packet {
    override val command: Int
        get() = RM_PARTITION
}