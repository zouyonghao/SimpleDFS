package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.client.console.SendFileConsole
import cn.edu.tsinghua.sdfs.exception.WrongCodecException
import cn.edu.tsinghua.sdfs.io.NetUtil.shutdownGracefully
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.NameItem
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.protocol.packet.impl.RmPartition
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.concurrent.CountDownLatch

class ClientCommandHandler : ChannelInboundHandlerAdapter() {

    companion object {
        var partitionsNeedDelete: Int = 0
        var partitionsDeleted: Int = 0
        // sum it ya!
        var sumItYa = false
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
                if (sumItYa && JSON.isValid(packet.result)) {
                    // MutableMap<Int, MutableList<String>>
                    var sum = 0
                    JSON.parseObject(packet.result, object : TypeReference<MutableMap<Int, MutableList<String>>>() {}).forEach { _, list ->
                        list.forEach {
                            sum += it.toInt()
                        }
                    }
                    println(sum)
                }
                ctx.channel().close()
            }
            is NameItem -> {
                packet.apply {
                    if (exist) {
                        // download
                        if (download) {
                            val countDownLatch = CountDownLatch(1)
                            FileDownloader.countDownLatch = countDownLatch
                            FileDownloader.fileLength = fileLength
                            FileDownloader.download(this)
                            countDownLatch.await()
                            ctx.channel().close()
                        } else {
                            partitions.forEach { it.forEach { _ -> partitionsNeedDelete++ } }
                            println("file already exist, old one will be deleted")
                            FileUploader.deleteOld(this)
                        }
                    } else {
                        if (download) {
                            println("file does not exist")
                        } else if (!success) {
                            println("allocate slave failed, check slave server")
                        } else if (partitions.size > 0) {
                            FileUploader.upload(this)
                        }
                        ctx.channel().close()
                    }
                }
            }
            // from slave server

            // delete success
            is RmPartition -> {
                // println("PartitionsDeleted: $partitionsDeleted")
                // println("PartitionsNeedDelete: $partitionsNeedDelete")
                partitionsDeleted++
                // TODO: check whether all partitions deleted
                if (partitionsDeleted == partitionsNeedDelete) {
                    println("all partitions deleted!")
                    SendFileConsole.sendCreateRequest()
                }
                shutdownGracefully(ctx.channel())
            }

            // create success
            is FilePacket -> {
                FileUploader.doUpload(packet, ctx.channel())
            }
            else -> {
                println(packet)
                // ctx.channel().close()
            }
        }
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        super.channelUnregistered(ctx)
        shutdownGracefully(ctx.channel())
    }
}