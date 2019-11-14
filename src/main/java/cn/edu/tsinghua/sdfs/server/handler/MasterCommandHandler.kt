package cn.edu.tsinghua.sdfs.server.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.exception.WrongCodecException
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.LsPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.server.NameManager
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class MasterCommandHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            throw WrongCodecException()
        }
        when (val packet = Codec.INSTANCE.decode(byteBuf)) {
            is CreateRequest -> {
                val nameItem = NameManager.create(packet.remoteFile, packet.fileSize)
                ctx.channel().writeAndFlush(Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(), nameItem))
            }
            is LsPacket -> {
                ctx.channel().writeAndFlush(
                        Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(),
                                ResultToClient(NameManager.ls(packet.path)))
                )
            }
        }

    }
}
