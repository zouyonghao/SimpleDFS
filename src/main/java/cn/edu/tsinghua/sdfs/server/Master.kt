package cn.edu.tsinghua.sdfs.server

import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.server.handler.ServerCommandHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.stream.ChunkedWriteHandler

//object Server {

//    @JvmStatic
fun main(args: Array<String>) {
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
                            .addLast("handler", ServerCommandHandler())
                }
            })

    try {
        val future = bootstrap.bind(config.master.port).sync()
        future.channel().closeFuture().sync()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        worker.shutdownGracefully()
        boss.shutdownGracefully()
    }

}


//}
