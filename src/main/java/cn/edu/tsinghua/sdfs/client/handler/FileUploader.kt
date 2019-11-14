package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.io.FileUtil
import cn.edu.tsinghua.sdfs.io.NetUtil
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.server.NameItem
import io.netty.channel.ChannelFuture
import io.netty.channel.DefaultFileRegion
import io.netty.handler.stream.ChunkedWriteHandler

object FileUploader {

    lateinit var localFile: String
    lateinit var remoteFile: String

    fun upload(packet: NameItem) {
        val splitNum = packet.partitions.size
        val splitFiles = FileUtil.splitFile(localFile, packet.partitionSize)

        if (splitFiles.size != splitNum) {
            error("Split file fail.\n" +
                    "The number of local files is different from remote files")
        }

        val serverToFutureMap = mutableMapOf<Server, ChannelFuture>()

        splitFiles.zip(packet.partitions).forEach { (splitFile, partition) ->
            run {
                partition.forEach {
                    serverToFutureMap.putIfAbsent(it, getChannel(it))
                    val future: ChannelFuture = serverToFutureMap[it]!!
                    val name = remoteFile + splitFile.toString().substringAfter(localFile)
                    future.channel().writeAndFlush(
                            Codec.INSTANCE.encode(future.channel().alloc().ioBuffer(),
                                    FilePacket(name, splitFile.toFile().length())
                            ))
                    // TODO: HERE?!!! FIX TCP PACKET!!
                    Thread.sleep(200)
                    println(splitFile.toFile().length())
                    future.channel().writeAndFlush(
                            DefaultFileRegion(splitFile.toFile(), 0, splitFile.toFile().length())
                    )
                }
            }
        }

        serverToFutureMap.values.forEach {
            it.channel().closeFuture().sync()
            NetUtil.shutdownGracefully(it)
        }
    }

    private fun getChannel(it: Server): ChannelFuture {
        return NetUtil.connect(it.ip, it.port, ChunkedWriteHandler())
    }
}