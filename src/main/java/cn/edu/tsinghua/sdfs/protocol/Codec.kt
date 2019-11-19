package cn.edu.tsinghua.sdfs.protocol

import cn.edu.tsinghua.sdfs.exception.WrongCodecException
import cn.edu.tsinghua.sdfs.protocol.packet.Packet
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.DownloadRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.LsPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.NameItem
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.protocol.packet.impl.RmPartition
import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel

object Codec {

    const val TYPE = 0x12345678

    // command
    const val CREATE_REQUEST = 1
    const val LS = 2
    // do not support yet
    // const val CD = 3
    // const val PWD = 4
    const val COPY_FROM_LOCAL = 5
    const val COPY_TO_LOCAL = 6
    const val RM = 7
    const val FILE_PACKET = 8
    const val RESULT = 10
    const val NAME_ITEM = 11
    const val RM_PARTITION = 12
    const val DOWNLOAD_REQUEST = 13
    const val USER_PROGRAM = 14

    private val packetTypeMap = mapOf(
            CREATE_REQUEST   to CreateRequest::class.java,
            LS               to LsPacket::class.java,
            RESULT           to ResultToClient::class.java,
            NAME_ITEM        to NameItem::class.java,
            FILE_PACKET      to FilePacket::class.java,
            RM_PARTITION     to RmPartition::class.java,
            DOWNLOAD_REQUEST to DownloadRequest::class.java,
            USER_PROGRAM     to UserProgram::class.java
    )

    fun encode(byteBuf: ByteBuf, packet: Packet): ByteBuf {
        val bytes = Serializer.DEFAULT.serialize(packet)
        byteBuf.writeInt(TYPE)
        byteBuf.writeInt(packet.command)
        byteBuf.writeInt(bytes.size)
        byteBuf.writeBytes(bytes)
        return byteBuf
    }

    fun decode(byteBuf: ByteBuf): Packet {
        byteBuf.readInt()
        val command = byteBuf.readInt()
        val len = byteBuf.readInt()
        val bytes = ByteArray(len)
        byteBuf.readBytes(bytes)

        val clazz = packetTypeMap[command] ?: throw WrongCodecException()

        return Serializer.DEFAULT.deserialize(bytes, clazz)
    }

    fun writeAndFlushPacket(channel: Channel, packet: Packet) {
        channel.writeAndFlush(encode(channel.alloc().ioBuffer(), packet))
    }

}