package cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.protocol.Codec.DO_REDUCE_
import cn.edu.tsinghua.sdfs.protocol.packet.Packet
import cn.edu.tsinghua.sdfs.server.mapreduce.IntermediateFile
import cn.edu.tsinghua.sdfs.server.mapreduce.Job

data class DoReducePacket(val job: Job,
                          val server: Server,
                          val filePartition: Int,
                          val intermediateFiles: MutableSet<IntermediateFile>) : Packet {
    override val command: Int
        get() = DO_REDUCE_

}
