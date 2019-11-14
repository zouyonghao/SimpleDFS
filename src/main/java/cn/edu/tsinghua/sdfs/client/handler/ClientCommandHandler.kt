package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.exception.WrongCodecException
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.server.NameItem
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.stream.ChunkedFile
import java.io.File

class ClientCommandHandler : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            throw WrongCodecException()
        }
        when (val packet = Codec.INSTANCE.decode(byteBuf)) {
            is ResultToClient -> {
                println(packet.result)
                ctx.close()
            }
            is NameItem -> {
                if (packet.partitions.size > 0) {
                    FileUploader.upload(packet)
                }
            }
            else -> {
                println(packet)
                ctx.channel().close()
            }
        }
    }
}