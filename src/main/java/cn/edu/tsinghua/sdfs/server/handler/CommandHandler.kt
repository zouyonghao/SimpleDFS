package cn.edu.tsinghua.sdfs.server.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.FilePacket
import cn.edu.tsinghua.sdfs.protocol.command.Command.Companion.FILE_PACKET
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.io.File
import java.io.FileOutputStream

class CommandHandler : ChannelInboundHandlerAdapter() {

    // TODO: support multi files.
    private var outputStream: FileOutputStream? = null

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            val bytes = ByteArray(byteBuf.readableBytes())
            byteBuf.readBytes(bytes)
            outputStream!!.write(bytes)
            println(msg)
            byteBuf.release()
        } else {
            val packet = Codec.INSTANCE.decode(byteBuf)
            when (packet.command) {
                FILE_PACKET -> {
                    val filePacket = packet as FilePacket
                    println("receive file from client: " + filePacket.file!!.name)
                    outputStream = FileOutputStream(File("./receive-" + filePacket.file!!.name))
                    ctx.channel().writeAndFlush(Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(), filePacket))
                }
                // TODO: add more commands.
                else -> {
                }
            }// pass
        }

    }
}
