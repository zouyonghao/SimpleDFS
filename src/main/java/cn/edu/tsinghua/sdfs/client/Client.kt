package cn.edu.tsinghua.sdfs.client

import cn.edu.tsinghua.sdfs.client.console.SendFileConsole
import cn.edu.tsinghua.sdfs.client.handler.ClientCommandHandler
import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.io.NetUtil
import cn.edu.tsinghua.sdfs.io.delimiterBasedFrameDecoder
import io.netty.channel.ChannelFuture


object Client {

    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        if (args.isEmpty()) {
            println("Usage: Client [command] [options...]")
            return
        }
        var future: ChannelFuture? = null
        try {
            future = NetUtil.connect(
                    config.master,
                    delimiterBasedFrameDecoder(),
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
        } finally {
            NetUtil.shutdownGracefully(future?.channel())
        }
    }

}
