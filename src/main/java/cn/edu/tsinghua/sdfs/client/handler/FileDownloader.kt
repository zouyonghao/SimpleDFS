package cn.edu.tsinghua.sdfs.client.handler

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.io.FileUtil
import cn.edu.tsinghua.sdfs.io.NetUtil
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.DownloadRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.NameItem
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.stream.ChunkedWriteHandler
import java.io.RandomAccessFile
import java.util.concurrent.CountDownLatch

object FileDownloader {
    lateinit var countDownLatch: CountDownLatch
    lateinit var localFile: String
    lateinit var remoteFile: String

    var fileLength: Long = 0L
    var fileDownloadLength = 0L

    val partitionFiles = mutableSetOf<RandomAccessFile>()
    var channels = mutableSetOf<Channel>()

    fun download(packet: NameItem) {
        packet.partitions.forEachIndexed { index, partition ->
            run {
                val name = remoteFile + String.format("%07d", index)
                val future: ChannelFuture = getChannel(partition.random(), localFile + String.format("%07d", index))
                Codec.writeAndFlushPacket(future.channel(), DownloadRequest(name))
            }
        }
    }

    private fun getChannel(it: Server, file: String): ChannelFuture {
        return NetUtil.connect(it,
                ChunkedWriteHandler(),
                object : ChannelInboundHandlerAdapter() {
                    private var fileName: String = file
                    private var randomAccessFile: RandomAccessFile = RandomAccessFile(fileName, "rw")
                    private var position: Long = 0

                    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                        partitionFiles.add(randomAccessFile)
                        channels.add(ctx.channel())
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
                            randomAccessFile.channel.force(true)
                            byteBuf.release()
                            synchronized(FileDownloader) {
                                position += written
                                fileDownloadLength += written

                                if (fileDownloadLength == fileLength) {
                                    partitionFiles.forEach { it.close() }
                                    channels.forEach { NetUtil.shutdownGracefully(it) }
                                    FileUtil.mergeFiles(localFile, localFile)
                                    countDownLatch.countDown()
                                }
                            }

                            return
                        }
                    }
                })
    }
}