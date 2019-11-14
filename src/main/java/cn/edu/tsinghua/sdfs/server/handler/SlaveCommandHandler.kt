package cn.edu.tsinghua.sdfs.server.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.io.FileOutputStream

class SlaveCommandHandler : ChannelInboundHandlerAdapter() {

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

        }

    }
}