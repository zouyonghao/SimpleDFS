package cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.protocol.Codec.DO_MAP_
import cn.edu.tsinghua.sdfs.protocol.packet.Packet
import cn.edu.tsinghua.sdfs.server.mapreduce.Job

data class DoMapPacket(val job: Job, val slave: Server, val partition: Int) : Packet {
    override val command: Int
        get() = DO_MAP_
}