package cn.edu.tsinghua.sdfs.codec

import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.CREATE_REQUEST
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.FILE_PACKET
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.LS
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.NAME_ITEM
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.RESULT
import cn.edu.tsinghua.sdfs.protocol.packet.Packet
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.LsPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer
import cn.edu.tsinghua.sdfs.server.NameItem
import io.netty.buffer.ByteBuf

class Codec private constructor() {

    private val packetTypeMap = mapOf(
            CREATE_REQUEST  to CreateRequest::class.java,
            LS              to LsPacket::class.java,
            RESULT          to ResultToClient::class.java,
            NAME_ITEM       to NameItem::class.java,
            FILE_PACKET     to FilePacket::class.java
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

        val clazz = packetTypeMap[command] ?: throw NullPointerException("解析失败，没有该类型的数据包")

        return Serializer.DEFAULT.deserialize(bytes, clazz)
    }

    companion object {

        const val TYPE = 0x12345678

        var INSTANCE = Codec()
    }


}
