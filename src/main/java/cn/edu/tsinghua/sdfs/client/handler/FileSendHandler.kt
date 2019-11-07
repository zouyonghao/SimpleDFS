package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.FilePacket
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.stream.ChunkedFile
import java.io.File

class FileSendHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val filePacket = Codec.INSTANCE.decode(byteBuf) as FilePacket

        if (filePacket.file != null) {
            println("prepared send: " + (filePacket.file!!.name))

            val channel = ctx.channel()
            channel.writeAndFlush(ChunkedFile(File(filePacket.file!!.name)))
        }

    }
}
