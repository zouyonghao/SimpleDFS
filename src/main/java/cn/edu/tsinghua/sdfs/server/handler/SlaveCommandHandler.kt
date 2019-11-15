package cn.edu.tsinghua.sdfs.server.handler

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.RmPartition
import cn.edu.tsinghua.sdfs.server.DataManager
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.io.RandomAccessFile


class SlaveCommandHandler : ChannelInboundHandlerAdapter() {

    private lateinit var randomAccessFile: RandomAccessFile
    private lateinit var fileName: String
    private var fileLength: Long = 0
    private var position: Long = 0

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        // println(byteBuf.readableBytes())
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
                println("File $fileName upload success")
                randomAccessFile.close()
                ctx.channel().close()
            }
            return
        }
        when (val packet = Codec.INSTANCE.decode(byteBuf)) {
            is FilePacket -> {
                this@SlaveCommandHandler.fileLength = packet.fileLength
                this@SlaveCommandHandler.fileName = packet.file
                randomAccessFile = DataManager.getRandomAccessFile(packet.file)
                // fix TCP packet here
                // TODO: better solution
                // if (byteBuf.readableBytes() > 0) {
                //     var written = 0
                //     val size = byteBuf.readableBytes()
                //     while (written < size) {
                //         written += randomAccessFile.channel.write(byteBuf.nioBuffer())
                //     }
                //     position += written
                //     randomAccessFile.channel.force(true)
                // }
                // send a packet to let client start sending file
                ctx.channel().writeAndFlush(Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(), packet))
            }
            is RmPartition -> {
                DataManager.deleteFile(packet.file)
                ctx.channel().writeAndFlush(Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(), packet))
                ctx.channel().close()
            }
        }
    }
}