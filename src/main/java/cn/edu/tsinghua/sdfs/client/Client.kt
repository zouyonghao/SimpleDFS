package cn.edu.tsinghua.sdfs.client

import cn.edu.tsinghua.sdfs.client.console.SendFileConsole
import cn.edu.tsinghua.sdfs.client.handler.ClientCommandHandler
import cn.edu.tsinghua.sdfs.config
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

//object Client {

@Throws(InterruptedException::class)
// @JvmStatic
fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Usage: Client [command] [options...]")
        return
    }

    val master = Bootstrap()

    val group = NioEventLoopGroup()

    master.group(group)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(object : ChannelInitializer<NioSocketChannel>() {
                @Throws(Exception::class)
                override fun initChannel(channel: NioSocketChannel) {
                    channel.pipeline()
                            .addLast(ClientCommandHandler())
                }
            })

    val future = master.connect(config.master.ip, config.master.port).sync()
    if (future.isSuccess) {
        println("connect success!")
        val channel = future.channel()
        SendFileConsole.exec(channel, args)
    } else {
        println("connect fail, exiting...")
        return
    }

    future.channel().closeFuture().sync()

    group.shutdownGracefully()

}

//}
