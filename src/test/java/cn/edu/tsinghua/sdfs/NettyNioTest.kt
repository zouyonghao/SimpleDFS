package cn.edu.tsinghua.sdfs

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.DefaultFileRegion
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.CharsetUtil
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.Paths

class NettyNioTest {

    @Test
    fun startHttpServer() {
        var first = true
        ServerBootstrap().apply {
            group(NioEventLoopGroup(1))
            channel(NioServerSocketChannel::class.java)
            childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(socketChannel: SocketChannel) {
                    socketChannel.pipeline()
                            // .addLast(HttpServerCodec())
                            // .addLast(HttpObjectAggregator(65536))
                            .addLast(StringEncoder(CharsetUtil.UTF_8))
                            .addLast(LineBasedFrameDecoder(8192))
                            .addLast(StringDecoder(CharsetUtil.UTF_8))
                            .addLast(ChunkedWriteHandler())
                            .addLast(object : ChannelInboundHandlerAdapter() {
                                override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
                                    println("${Thread.currentThread()} start, " +
                                            "which channel id is ${ctx.channel().id()}, " +
                                            "time is ${System.currentTimeMillis()}")
                                    if (first) {
                                        first = false
                                        println("hello")
                                        // ctx.channel().writeAndFlush("you are the first client\n")
                                        // val f = RandomAccessFile("test_file/numberFile", "rw")
                                        // val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                                        // response.headers().set(CONTENT_TYPE, "text/plain")
                                        // response.headers().set(CONTENT_LENGTH, f.length())
                                        // readAndWrite(ctx.channel())
                                        // FileInputStream(File("")).channel.
                                        // val f = RandomAccessFile("test_file/numberFile", "rw")

                                        // nio zero-copy
                                        val f = File("test_file/numberFile")
                                        ctx.channel().writeAndFlush(DefaultFileRegion(f, 0, f.length())).addListener {
                                            println("write file finished")
                                        }

                                        // aio file operation
                                        // aioTest(ctx)

                                        // ctx.channel().writeAndFlush(ChunkedNioFile(f.channel)).addListener {
                                        //     println("write file finished")
                                        // }
                                        // ctx.writeAndFlush(ChunkedFile(f, 1000_000_000))
                                        //         .addListener {
                                        //     println("write file finished")
                                        // }
                                        // ctx.channel().writeAndFlush(Files.readAllBytes(Paths.get("test_file/numberFile")))
                                        // ctx.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                                        //     override fun channelActive(ctx: ChannelHandlerContext) {
                                        //         // readAndWrite(ctx.channel())
                                        //     }
                                        //
                                        //     override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
                                        //         // val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("abc".toByteArray()));
                                        //         // response.headers().set(CONTENT_TYPE, "text/plain")
                                        //         // response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                                        //         // ctx.channel().writeAndFlush(ChunkedNioFile(RandomAccessFile("test_file/numberFile", "rw").channel))
                                        //         // ctx.channel().writeAndFlush(Files.readAllBytes(Paths.get("test_file/numberFile")))
                                        //         // readAndWrite(ctx.channel())
                                        //     }
                                        // })
                                        return
                                    } else {
                                        println("Not first")
                                        // val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("abc".toByteArray()));
                                        // response.headers().set(CONTENT_TYPE, "text/plain")
                                        // response.headers().set(CONTENT_LENGTH, response.content().readableBytes())
                                        // ctx.channel().writeAndFlush(response)
                                        ctx.writeAndFlush("you are not the first client\n")
                                        println("${Thread.currentThread()} finished this request, which channel id is ${ctx.channel().id()}, " +
                                                "time is ${System.currentTimeMillis()}")
                                    }
                                }

                                override fun channelUnregistered(ctx: ChannelHandlerContext?) {
                                    super.channelUnregistered(ctx)
                                    println("Channel unregistered")
                                }
                            })
                }

            })
        }.bind(8088).channel().closeFuture().sync()
    }

    private fun aioTest(ctx: ChannelHandlerContext) {
        val buffer = ByteBuffer.allocate(1000_000_000)
        var count = 0L
        AsynchronousFileChannel.open(Paths.get("test_file/numberFile")).read<Any>(buffer, count, null, object :CompletionHandler<Int, Any> {
            override fun completed(result: Int?, attachment: Any?) {
                ctx.writeAndFlush("read file finished\n")
                println(result)
                println(buffer)
                for ( i in 0 until result!!) {
                    // println(buffer.get(i))
                    ctx.writeAndFlush(buffer.get(i).toString())
                }
            }

            override fun failed(exc: Throwable?, attachment: Any?) {
                println("error")
            }
        })
    }

    private fun readAndWrite(channel: Channel) {
        val length = File("test_file/numberFile").length()
        val f = BufferedReader(FileReader("test_file/numberFile"))
        val result = StringBuilder()
        var count = 0
        while (true) {
            f.readLine()?.apply {
                // println("reading in thread ${Thread.currentThread()}")
                // result.append(this)

                channel.writeAndFlush(this)
                count += this.length
            } ?: break
        }
        println("$length, $count")
        // channel.writeAndFlush(result)
    }
}
