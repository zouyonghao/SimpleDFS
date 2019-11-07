package cn.edu.tsinghua.sdfs.client


import cn.edu.tsinghua.sdfs.client.console.SendFileConsole
import cn.edu.tsinghua.sdfs.client.handler.FileSendHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.stream.ChunkedWriteHandler

//object Client {

private val HOST = System.getProperty("host", "127.0.0.1")

private val PORT = Integer.parseInt(System.getProperty("port", "6732"))

//    @Throws(InterruptedException::class)
//    @JvmStatic
fun main() {//args: Array<String>) {

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
                    val pipeline = channel.pipeline()
                    pipeline.addLast("streamer", ChunkedWriteHandler())
                    pipeline.addLast(FileSendHandler())
                }
            })

    val future = bootstrap.connect(HOST, PORT).sync()
    if (future.isSuccess) {
        println("连接服务器成功")
        val channel = future.channel()
        console(channel)
    } else {
        println("连接服务器失败")
    }

    future.channel().closeFuture().sync()

}


// TODO: add a state machine to exec user commands.
private fun console(channel: Channel) {
    Thread { SendFileConsole.exec(channel) }.start()
}

//}
