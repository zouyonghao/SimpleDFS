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

    private lateinit var randomAccessFile: RandomAccessFile
    private var fileLength: Long = 0
    private var position: Long = 0

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            val byteBuffer = byteBuf.nioBuffer()
            var written = 0
            val size = byteBuf.readableBytes()
            while (written < size) {
                written += randomAccessFile.channel.write(byteBuffer)
            }
            // byteBuf.readerIndex(byteBuf.readerIndex() + written)
            position += written
            randomAccessFile.channel.force(true)
            byteBuf.release()

            if (position == fileLength) {
                println("upload success")
                randomAccessFile.close()
                ctx.channel().close()
            }
            return
        }
        Codec.INSTANCE.decode(byteBuf).cast<FilePacket>().run {
            this@SlaveCommandHandler.fileLength = this.fileLength
            randomAccessFile = DataManager.getRandomAccessFile(this.file)
            // fix TCP packet here
            // TODO: better solution
            if (byteBuf.readableBytes() > 0) {
                var written = 0
                val size = byteBuf.readableBytes()
                while (written < size) {
                    written += randomAccessFile.channel.write(byteBuf.nioBuffer())
                }
                // byteBuf.readerIndex(byteBuf.readerIndex() + written)
                position += written
                randomAccessFile.channel.force(true)
            }
        }
    }
}