package cn.edu.tsinghua.sdfs

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE

class NettyNioTest {

    // @Test
    fun startHttpServer() {
        ServerBootstrap().apply {
            group(NioEventLoopGroup(), NioEventLoopGroup(2))
            channel(NioServerSocketChannel::class.java)
            childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(socketChannel: SocketChannel) {
                    socketChannel.pipeline()
                            .addLast(HttpServerCodec())
                            .addLast(object : ChannelInboundHandlerAdapter() {
                                override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
                                    println(Thread.currentThread())
                                    if (msg is HttpRequest) {
                                        println("hello")
                                    }
                                    val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("abc".toByteArray()));
                                    response.headers().set(CONTENT_TYPE, "text/plain")
                                    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                                    ctx.channel().writeAndFlush(response)
                                }
                            })
                }

            })
        }.bind(8088).channel().closeFuture().sync()
    }
}
