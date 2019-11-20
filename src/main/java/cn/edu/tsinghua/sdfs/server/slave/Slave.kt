package cn.edu.tsinghua.sdfs.server.slave

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.io.delimiterBasedFrameDecoder
import cn.edu.tsinghua.sdfs.server.mapreduce.UserProgramManager
import cn.edu.tsinghua.sdfs.server.master.JobTracker
import cn.edu.tsinghua.sdfs.server.slave.handler.SlaveCommandHandler
import com.alibaba.fastjson.JSON
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.stream.ChunkedWriteHandler
import java.nio.file.Files
import java.nio.file.Paths

lateinit var slave: Server

fun main(args: Array<String>) {

    val slaveConfig: String?
    if (args.isNotEmpty()) {
        slaveConfig = args[0]
    } else {
        slaveConfig = "slave.json"
    }
    slave = JSON.parseObject(Files.readAllBytes(Paths.get(slaveConfig)), Server::class.java)
    UserProgramManager.ROOT_DIR = Paths.get(slave.folder)
    JobTracker.ROOT_DIR = Paths.get(slave.folder)

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
                            .addLast("delimiter", delimiterBasedFrameDecoder())
                            .addLast("handler", SlaveCommandHandler())
                }
            })

    val future = bootstrap.bind(slave.ip, slave.port).sync()
    future.channel().closeFuture().sync()

    worker.shutdownGracefully()
    boss.shutdownGracefully()
}