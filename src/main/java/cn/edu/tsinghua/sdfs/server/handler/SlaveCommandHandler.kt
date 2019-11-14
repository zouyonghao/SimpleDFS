package cn.edu.tsinghua.sdfs.server.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.server.DataManager
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.jetbrains.kotlin.utils.addToStdlib.cast
import java.io.RandomAccessFile

class SlaveCommandHandler : ChannelInboundHandlerAdapter() {

    private lateinit var randomAccessFile:RandomAccessFile
    private var fileLength:Long = 0

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            randomAccessFile.seek(randomAccessFile.length())
            randomAccessFile.channel.write(byteBuf.nioBuffer())
            byteBuf.release()
            if (randomAccessFile.length() == fileLength) {
                ctx.channel().close()
            }
            return
        }
        Codec.INSTANCE.decode(byteBuf).cast<FilePacket>().run {
            this@SlaveCommandHandler.fileLength = this.fileLength
            randomAccessFile = DataManager.getFileChannel(this.file)
        }
    }
}