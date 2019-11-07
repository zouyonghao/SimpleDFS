package cn.edu.tsinghua.sdfs.client.console

import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.FilePacket
import io.netty.channel.Channel
import java.io.File
import java.util.*

object SendFileConsole {

    fun exec(channel: Channel) {
        val sc = Scanner(System.`in`)
        println("please input the file path: ")
        val path = sc.nextLine()
        val file = File(path)
        val filePacket = FilePacket(file)
        channel.writeAndFlush(Codec.INSTANCE.encode(channel.alloc().ioBuffer(), filePacket))
    }

}
