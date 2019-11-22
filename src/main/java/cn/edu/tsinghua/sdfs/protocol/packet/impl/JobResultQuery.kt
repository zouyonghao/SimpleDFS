package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.protocol.Codec.JOB_RESULT
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

data class JobResultQuery(val id: String) : Packet {
    override val command: Int
        get() = JOB_RESULT
}