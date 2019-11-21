package cn.edu.tsinghua.sdfs.server.mapreduce

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.io.NetUtil
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.DownloadRequest
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.stream.ChunkedWriteHandler
import java.io.RandomAccessFile
import java.util.concurrent.CountDownLatch

class IntermediateFileDownloader {
    lateinit var localFile: String
    lateinit var countDownLatch: CountDownLatch

    fun download(intermediateFile: IntermediateFile) {
        val future: ChannelFuture = getChannel(intermediateFile.server, localFile)
        Codec.writeAndFlushPacket(future.channel(), DownloadRequest(intermediateFile.file))
    }

    private fun getChannel(it: Server, file: String): ChannelFuture {
        return NetUtil.connect(it,
                ChunkedWriteHandler(),
                object : ChannelInboundHandlerAdapter() {
                    private var randomAccessFile: RandomAccessFile = RandomAccessFile(file, "rw")
                    private var position: Long = 0

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

                            return
                        }
                    }

                    override fun channelUnregistered(ctx: ChannelHandlerContext) {
                        ctx.channel().close()
                        randomAccessFile.close()
                        countDownLatch.countDown()
                    }
                })
    }
}