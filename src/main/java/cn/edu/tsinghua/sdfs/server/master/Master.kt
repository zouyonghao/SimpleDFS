package cn.edu.tsinghua.sdfs.server.master

import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.server.mapreduce.UserProgramManager
import cn.edu.tsinghua.sdfs.server.master.handler.MasterCommandHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.nio.file.Paths

object Master {
    @JvmStatic
    fun main(args: Array<String>) {

        UserProgramManager.ROOT_DIR = Paths.get(config.master.folder)

        val bootstrap = ServerBootstrap()

        val boss = NioEventLoopGroup()
        val worker = NioEventLoopGroup()

        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<NioSocketChannel>() {
                    @Throws(Exception::class)
                    override fun initChannel(channel: NioSocketChannel) {
                        channel.pipeline()
                                .addLast("handler", MasterCommandHandler())
                    }
                })

        val future = bootstrap.bind(config.master.port).sync()

        SlaveManager.initSlaveConnections()

        future.channel().closeFuture().sync()

        worker.shutdownGracefully()
        boss.shutdownGracefully()

        SlaveManager.close()

    }
}
