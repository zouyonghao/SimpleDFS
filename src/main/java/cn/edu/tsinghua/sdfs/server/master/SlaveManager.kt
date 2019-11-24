package cn.edu.tsinghua.sdfs.server.master

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.io.NetUtil
import cn.edu.tsinghua.sdfs.io.delimiterBasedFrameDecoder
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoMapPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoReducePacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.GetReduceResult
import cn.edu.tsinghua.sdfs.server.mapreduce.IntermediateFile
import cn.edu.tsinghua.sdfs.server.mapreduce.Job
import cn.edu.tsinghua.sdfs.server.master.handler.MasterCommandHandler
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

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
                        future = NetUtil.connect(
                                server,
                                delimiterBasedFrameDecoder(),
                                MasterCommandHandler())
                        future!!.awaitUninterruptibly()
                        if (future!!.isSuccess)
                            connectionSuccess = true
                        else {
                            NetUtil.shutdownGracefully(future!!.channel())
                        }
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

    fun getOkSlave(list: MutableList<Server>): Server? {
        slaveChannels.filter { it.ok() && !list.contains(it.server) }.apply {
            if (isEmpty()) {
                println("allocate slave fail")
                return null
            }
            return random().server
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
        val uploadedCount = AtomicInteger(0)
        val okSlaveChannels = slaveChannels.filter { it.ok() }
        val uploadCount = okSlaveChannels.count()
        okSlaveChannels.forEach {
            val channel = it.future!!.channel()
            Codec.writeAndFlushPacket(channel, packet)
                    .addListener {
                        println("upload user program success")
                        if (uploadCount == uploadedCount.incrementAndGet()) {
                            listener.invoke()
                        }
                    }
        }
    }

    fun doMap(job: Job, slaves: MutableList<Server>, filePartition: Int): Server? {
        val slave = slaveChannels.filter { it.ok() && slaves.contains(it.server) }.random()
        Codec.writeAndFlushPacket(slave.future!!.channel(), DoMapPacket(job, slave.server, filePartition))
        job.jobContext.mapper.add(slave.server)
        return slave.server
    }

    fun doReduce(job: Job, reducePartition: Int, intermediateFiles: MutableSet<IntermediateFile>): Server? {
        val slave = slaveChannels.filter { it.ok() }.random()
        Codec.writeAndFlushPacket(slave.future!!.channel(), DoReducePacket(job, slave.server, reducePartition, intermediateFiles))
        job.jobContext.reducer.add(slave.server)
        return slave.server
    }

    fun getReduceResult(server: Server, file: String, channelId: String, reduceIndex: Int) {
        val future = slaveChannels.find { it.server == server }!!.future
        Codec.writeAndFlushPacket(future!!.channel(), GetReduceResult(file, channelId, reduceIndex))
    }

    fun slaveChannelUnregister(channel: Channel) {
        slaveChannels.find { it.future?.channel() == channel }?.apply {
            println("A slave connection reset")
            this.connectionSuccess = false
        }
    }
}