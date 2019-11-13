package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.stream.ChunkedFile
import java.io.File

class FileSendHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val packet = Codec.INSTANCE.decode(byteBuf)

        when {
            packet is CreateRequest && packet.localFile.isNotEmpty() -> {
                val channel = ctx.channel()
                channel.writeAndFlush(ChunkedFile(File(packet.localFile)))
            }
        }

    }
}
