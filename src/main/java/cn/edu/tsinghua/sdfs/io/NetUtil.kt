package cn.edu.tsinghua.sdfs.io

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

object NetUtil {

    private val futureToGroupMap = mutableMapOf<ChannelFuture, NioEventLoopGroup>()

    fun connect(ip:String, port:Int, vararg handlers:ChannelHandler): ChannelFuture {
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
                        handlers.forEach {
                            channel.pipeline().addLast(it)
                        }
                    }
                })

        val future = master.connect(ip, port).sync()
        futureToGroupMap[future] = group
        return future
    }

    fun shutdownGracefully(future: ChannelFuture) {
        futureToGroupMap[future]?.shutdownGracefully()
        futureToGroupMap.remove(future)
    }

}