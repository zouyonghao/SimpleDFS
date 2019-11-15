package cn.edu.tsinghua.sdfs.protocol

import cn.edu.tsinghua.sdfs.exception.WrongCodecException
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.CREATE_REQUEST
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.DOWNLOAD_REQUEST
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.FILE_PACKET
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.LS
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.NAME_ITEM
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.RESULT
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.RM_PARTITION
import cn.edu.tsinghua.sdfs.protocol.packet.Packet
import cn.edu.tsinghua.sdfs.protocol.packet.impl.*
import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer
import cn.edu.tsinghua.sdfs.server.NameItem
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel

object Codec {

    const val TYPE = 0x12345678

    private val packetTypeMap = mapOf(
            CREATE_REQUEST      to CreateRequest::class.java,
            LS                  to LsPacket::class.java,
            RESULT              to ResultToClient::class.java,
            NAME_ITEM           to NameItem::class.java,
            FILE_PACKET         to FilePacket::class.java,
            RM_PARTITION        to RmPartition::class.java,
            DOWNLOAD_REQUEST    to DownloadRequest::class.java
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

    fun writeAndFlushPacket(channel:Channel, packet: Packet) {
        channel.writeAndFlush(encode(channel.alloc().ioBuffer(), packet))
    }

}
