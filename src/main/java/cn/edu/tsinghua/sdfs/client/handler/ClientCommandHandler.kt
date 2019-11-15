package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.client.console.SendFileConsole
import cn.edu.tsinghua.sdfs.exception.WrongCodecException
import cn.edu.tsinghua.sdfs.io.NetUtil.shutdownGracefully
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.protocol.packet.impl.RmPartition
import cn.edu.tsinghua.sdfs.server.NameItem
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ClientCommandHandler : ChannelInboundHandlerAdapter() {

    companion object {
        var partitionsNeedDelete: Int = 0
        var partitionsDeleted: Int = 0
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            throw WrongCodecException()
        }
        when (val packet = Codec.decode(byteBuf)) {
            is ResultToClient -> {
                println(packet.result)
                ctx.channel().close()
            }
            is NameItem -> {
                if (packet.exist) {
                    // download
                    if (packet.download) {
                        FileDownloader.fileLength = packet.fileLength
                        FileDownloader.download(packet)
                        ctx.channel().close()
                    } else {
                        packet.partitions.forEach { it.forEach { _ -> partitionsNeedDelete++ } }
                        println("file already exist, old one will be deleted")
                        FileUploader.deleteOld(packet)
                    }
                } else {
                    if (packet.partitions.size > 0) {
                        FileUploader.upload(packet)
                    }
                    ctx.channel().close()
                }
            }
            // from slave server

            // delete success
            is RmPartition -> {
                println("PartitionsDeleted: $partitionsDeleted")
                println("PartitionsNeedDelete: $partitionsNeedDelete")
                partitionsDeleted++
                // TODO: check whether all partitions deleted
                if (partitionsDeleted == partitionsNeedDelete) {
                    println("all partitions deleted!")
                    SendFileConsole.sendCreateRequest()
                }
                ctx.channel().close()
            }

            // create success
            is FilePacket -> {
                FileUploader.doUpload(packet, ctx.channel())
            }
            else -> {
                println(packet)
                ctx.channel().close()
            }
        }
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        super.channelUnregistered(ctx)
        shutdownGracefully(ctx.channel())
    }
}