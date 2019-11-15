package cn.edu.tsinghua.sdfs.server

import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.server.handler.SlaveCommandHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.stream.ChunkedWriteHandler

fun main() {
    val bootstrap = ServerBootstrap()

    val boss = NioEventLoopGroup()
    val worker = NioEventLoopGroup()

    bootstrap.group(boss, worker)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<NioSocketChannel>() {
                @Throws(Exception::class)
                override fun initChannel(channel: NioSocketChannel) {
                    channel.pipeline()
                            .addLast("streamer", ChunkedWriteHandler())
                            .addLast("handler", SlaveCommandHandler())
                }
            })

    val future = bootstrap.bind(config.slaves[0].port).sync()
    future.channel().closeFuture().sync()

    worker.shutdownGracefully()
    boss.shutdownGracefully()
}