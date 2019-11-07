package cn.edu.tsinghua.sdfs.server

import cn.edu.tsinghua.sdfs.server.handler.CommandHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.stream.ChunkedWriteHandler

//object Server {

//    @JvmStatic
fun main() {//args: Array<String>) {
    val bootstrap = ServerBootstrap()

    val boss = NioEventLoopGroup()
    val worker = NioEventLoopGroup()

    bootstrap.group(boss, worker)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<NioSocketChannel>() {
                @Throws(Exception::class)
                override fun initChannel(channel: NioSocketChannel) {
                    val pipeline = channel.pipeline()
                    pipeline.addLast("streamer", ChunkedWriteHandler())
                    pipeline.addLast("handler", CommandHandler())
                }
            })

    try {
        val future = bootstrap.bind(6732).sync()
        future.channel().closeFuture().sync()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        worker.shutdownGracefully()
        boss.shutdownGracefully()
    }

}


//}
