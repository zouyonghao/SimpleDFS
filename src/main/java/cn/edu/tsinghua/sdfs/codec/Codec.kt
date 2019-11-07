package cn.edu.tsinghua.sdfs.codec

import cn.edu.tsinghua.sdfs.protocol.FilePacket
import cn.edu.tsinghua.sdfs.protocol.Packet
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.FILE_PACKET
import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer
import io.netty.buffer.ByteBuf
import java.util.*

class Codec private constructor() {

    private val packetTypeMap: MutableMap<Int, Class<out Packet>>

    init {
        packetTypeMap = HashMap()
        packetTypeMap[FILE_PACKET] = FilePacket::class.java
    }

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
