package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.io.FileUtil
import cn.edu.tsinghua.sdfs.io.NetUtil
import cn.edu.tsinghua.sdfs.protocol.packet.impl.FilePacket
import cn.edu.tsinghua.sdfs.server.NameItem
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.DefaultFileRegion
import io.netty.handler.stream.ChunkedWriteHandler
import java.io.RandomAccessFile

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
                    future.channel().run {
                        writeAndFlush(
                                Codec.INSTANCE.encode(future.channel().alloc().ioBuffer(),
                                        FilePacket(name, splitFile.toFile().length())
                                ))
                        // FIX TCP PACKET in @see{SlaveCommandHandler}
                        // Thread.sleep(200)
                        // writeAndFlush(Unpooled.wrappedBuffer("/".toByteArray()))
                        println("start upload file: $localFile")
                        println("fileSize: ${splitFile.toFile().length()}")

                        // writeAndFlush(
                        //         DefaultFileRegion(splitFile.toFile(), 0, splitFile.toFile().length())
                        // )
                    }
                }
            }
        }

        // serverToFutureMap.values.forEach {
        //     it.channel().closeFuture().sync()
        //     NetUtil.shutdownGracefully(it)
        // }
    }

    // remote_file with 0000001
    fun doUpload(packet: FilePacket, channel: Channel) {
        println("uploading...")
        val name = localFile + packet.file.substringAfter(remoteFile)
        channel.writeAndFlush(
                DefaultFileRegion(RandomAccessFile(name, "rw").channel, 0, packet.fileLength))
                // .addListener {
                //     // this listener has is not called for unknown reason
                //     ChannelFutureListener { future ->
                //         run {
                //             println(future.channel())
                //             NetUtil.shutdownGracefully(future.channel())
                //         }
                //     }
                // }
    }

    private fun getChannel(it: Server): ChannelFuture {
        return NetUtil.connect(it.ip, it.port,
                ChunkedWriteHandler(),
                ClientCommandHandler())
    }
}