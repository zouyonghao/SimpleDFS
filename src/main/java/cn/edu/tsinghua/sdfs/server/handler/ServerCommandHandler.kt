package cn.edu.tsinghua.sdfs.server.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.LsPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.server.NameManager
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.io.FileOutputStream

class ServerCommandHandler : ChannelInboundHandlerAdapter() {

    private var outputStream: FileOutputStream? = null

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            val bytes = ByteArray(byteBuf.readableBytes())
            byteBuf.readBytes(bytes)
            outputStream?.write(bytes)
            println(msg)
            byteBuf.release()
            return
        }
        when (val packet = Codec.INSTANCE.decode(byteBuf)) {
            is CreateRequest -> {
                NameManager.create(packet.remoteFile, packet.fileSize)
                // ctx.channel().writeAndFlush(Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(), packet))
            }
            is LsPacket -> {
                println("get Ls Packet")
                ctx.channel().writeAndFlush(
                        Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(),
                                ResultToClient("send a result"))
                )
            }
            else -> {
                println(packet)
                ctx.channel().close()
            }
        }


    }
}
