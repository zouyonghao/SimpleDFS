package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.protocol.Codec.JOB_QUERY
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

data class JobStatusQuery(val id: String) : Packet {
    override val command: Int
        get() = JOB_QUERY
}