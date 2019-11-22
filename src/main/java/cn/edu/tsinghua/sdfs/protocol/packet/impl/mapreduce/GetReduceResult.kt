package cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce

import cn.edu.tsinghua.sdfs.protocol.Codec.GET_REDUCE_RESULT
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

data class GetReduceResult(val file: String, val channelId: String, val reduceId: Int, var result: String = "") : Packet {
    override val command: Int
        get() = GET_REDUCE_RESULT

}
