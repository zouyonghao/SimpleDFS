package cn.edu.tsinghua.sdfs.server.master

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.io.NetUtil
import cn.edu.tsinghua.sdfs.io.delimiterBasedFrameDecoder
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoMapPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoReducePacket
import cn.edu.tsinghua.sdfs.server.mapreduce.IntermediateFile
import cn.edu.tsinghua.sdfs.server.mapreduce.Job
import cn.edu.tsinghua.sdfs.server.master.handler.MasterCommandHandler
import io.netty.channel.ChannelFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object SlaveManager {

    private val executor = Executors.newSingleThreadScheduledExecutor()

    private val slaveChannels = mutableListOf<Slave>()

    data class Slave(val server: Server,
                     var future: ChannelFuture?,
                     var connectionSuccess: Boolean = false
    ) {
        init {
            executor.scheduleAtFixedRate({
                if (!ok()) {
                    try {
                        NetUtil.shutdownGracefully(future?.channel())
                        println("connecting to $server")
                        future = NetUtil.connect(
                                server,
                                delimiterBasedFrameDecoder(),
                                MasterCommandHandler())
                        connectionSuccess = true
                    } catch (e: Exception) {
                        println("connect to $server fail")
                        e.printStackTrace()
                    }
                } else {
                    // println("slave $server already connected.")
                }
            }, 0, 5, TimeUnit.SECONDS)
        }

        // TODO: fail if first connect success but fail later
        fun ok(): Boolean {
            return connectionSuccess && future?.isSuccess ?: false
        }
    }

    fun initSlaveConnections() {
        config.slaves.forEach {
            slaveChannels.add(Slave(it, null, false))
        }
    }


    fun close() {
        executor.shutdown()
        slaveChannels.forEach {
            NetUtil.shutdownGracefully(it.future?.channel())
        }
    }

    fun uploadUserProgram(packet: UserProgram, listener: () -> Unit) {
        // TODO("add queue to each Slave in order to resume when slave down")
        var uploadCount = 0
        var uploadedCount = 0
        slaveChannels.filter { it.ok() }.forEach {
            val channel = it.future!!.channel()
            uploadCount++
            Codec.writeAndFlushPacket(channel, packet)
                    .addListener {
                        // println(uploadedCount)
                        uploadedCount++
                        if (uploadCount == uploadedCount) {
                            listener.invoke()
                        }
                    }
        }
    }

    fun doMap(job: Job, slaves: MutableList<Server>, filePartition: Int): Server? {
        val slave = slaveChannels.find { it.ok() && slaves.contains(it.server) } ?: return null
        Codec.writeAndFlushPacket(slave.future!!.channel(), DoMapPacket(job, slave.server, filePartition))
        job.jobContext.mapper.add(slave.server)
        return slave.server
    }

    fun doReduce(job: Job, reducePartition: Int, intermediateFiles: MutableSet<IntermediateFile>): Server? {
        val slave = slaveChannels.find { it.ok() } ?: return null
        Codec.writeAndFlushPacket(slave.future!!.channel(), DoReducePacket(job, slave.server, reducePartition, intermediateFiles))
        job.jobContext.reducer.add(slave.server)
        return slave.server
    }
}