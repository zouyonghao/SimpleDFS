package cn.edu.tsinghua.sdfs.client

import cn.edu.tsinghua.sdfs.client.console.SendFileConsole
import cn.edu.tsinghua.sdfs.client.handler.ClientCommandHandler
import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.io.NetUtil
import io.netty.buffer.Unpooled
import io.netty.handler.codec.DelimiterBasedFrameDecoder


object Client {

    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        if (args.isEmpty()) {
            println("Usage: Client [command] [options...]")
            return
        }

        val future = NetUtil.connect(
                config.master.ip,
                config.master.port,
                DelimiterBasedFrameDecoder(8192, Unpooled.copiedBuffer("__\r\n__".toByteArray())),
                ClientCommandHandler())
        if (future.isSuccess) {
            // println("connect success!")
            val channel = future.channel()
            SendFileConsole.exec(channel, args)
        } else {
            println("connect fail, exiting...")
            return
        }

        future.channel().closeFuture().sync()

        // NetUtil.shutdownGracefully(future.channel())
    }

}
