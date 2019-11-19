package cn.edu.tsinghua.sdfs.protocol.packet.impl

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.Packet

/**
 * {
 *  "fileSize": 1000 (bytes)
 *  "partitions": [
 *      [1, 3, 4], // partition1 in slave1,3,4
 *      [2, 4, 5]
 *  ]
 * }
 */
data class NameItem(
        val fileLength: Long,
        val partitions: MutableList<MutableList<Server>>,
        val partitionSize: Long
) : Packet {
    override val command: Int
        get() = Codec.NAME_ITEM

    var exist = false
    var download = false
}