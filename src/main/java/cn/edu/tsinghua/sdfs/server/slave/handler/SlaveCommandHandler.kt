package cn.edu.tsinghua.sdfs.server.slave.handler

import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.DownloadRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.RmPartition
import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoMapPacket
import cn.edu.tsinghua.sdfs.server.mapreduce.Mapper
import cn.edu.tsinghua.sdfs.server.mapreduce.UserProgramManager
import cn.edu.tsinghua.sdfs.server.slave.DataManager
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.DefaultFileRegion
import java.io.RandomAccessFile


class SlaveCommandHandler : ChannelInboundHandlerAdapter() {

    private var randomAccessFile: RandomAccessFile? = null
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
                written += randomAccessFile!!.channel.write(byteBuffer)
            }
            // byteBuf.readerIndex(byteBuf.readerIndex() + written)
            position += written
            randomAccessFile!!.channel.force(true)
            byteBuf.release()

            if (position == fileLength) {
                println("File $fileName upload success")
                randomAccessFile!!.close()
                ctx.channel().close()
            }
            return
        }
        when (val packet = Codec.decode(byteBuf)) {
            is FilePacket -> {
                this@SlaveCommandHandler.fileLength = packet.fileLength
                this@SlaveCommandHandler.fileName = packet.file
                randomAccessFile = DataManager.deleteAndCreate(packet.file)
                ctx.pipeline().remove("delimiter")
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
                Codec.writeAndFlushPacket(ctx.channel(), packet)
            }
            is DownloadRequest -> {
                println(packet)
                val randomAccessFile = DataManager.getFile(packet.filePath)
                ctx.channel().writeAndFlush(DefaultFileRegion(randomAccessFile.channel, 0, randomAccessFile.length()))
            }
            is RmPartition -> {
                DataManager.deleteFile(packet.file)
                Codec.writeAndFlushPacket(ctx.channel(), packet)
                ctx.channel().close()
            }
            is UserProgram -> {
                UserProgramManager.saveUserProgram(packet)
            }
            is DoMapPacket -> {
                Mapper.doMap(packet)
                Codec.writeAndFlushPacket(ctx.channel(), packet)
            }
        }
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        super.channelUnregistered(ctx)
        randomAccessFile?.close()
    }
}