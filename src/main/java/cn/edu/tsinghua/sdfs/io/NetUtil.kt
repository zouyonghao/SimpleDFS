package cn.edu.tsinghua.sdfs.io

import cn.edu.tsinghua.sdfs.Server
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder

object NetUtil {

    private val futureToGroupMap = mutableMapOf<Channel, NioEventLoopGroup>()
    private val serverToChannelMap = mutableMapOf<Server, ChannelFuture>()

    fun connect(server: Server, vararg handlers: ChannelHandler): ChannelFuture {

        if (serverToChannelMap.containsKey(server)) {
            return serverToChannelMap[server]!!
        }

        val bootstrap = Bootstrap()

        val group = NioEventLoopGroup()

        bootstrap.group(group)
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

        val future = bootstrap.connect(server.ip, server.port).sync()
        // shutdownGracefully(future.channel())
        // println(future.channel())

        futureToGroupMap[future.channel()] = group
        serverToChannelMap[server] = future
        return future
    }

    fun shutdownGracefully(channel: Channel?) {
        // println(channel)
        futureToGroupMap[channel]?.shutdownGracefully()
        futureToGroupMap.remove(channel)
    }

}

fun delimiterBasedFrameDecoder() =
        DelimiterBasedFrameDecoder(Int.MAX_VALUE, Unpooled.copiedBuffer("__\r\n__".toByteArray()))