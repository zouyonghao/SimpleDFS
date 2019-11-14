package cn.edu.tsinghua.sdfs.client.console

import cn.edu.tsinghua.sdfs.client.handler.FileUploader
import cn.edu.tsinghua.sdfs.codec.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.LsPacket
import io.netty.channel.Channel
import java.nio.file.Files
import java.nio.file.Paths

object SendFileConsole {

    fun exec(channel: Channel, args: Array<String>) {
        when (args[0]) {
            "ls" -> {
                // println("ls command executing...")
                channel.writeAndFlush(Codec.INSTANCE.encode(channel.alloc().ioBuffer(), LsPacket(args[1])))
                // channel.close()
            }
            "copyFromLocal" -> {
                val localFile = args[1]
                val remoteFile = args[2]
                FileUploader.localFile = localFile
                FileUploader.remoteFile = remoteFile
                if (Files.notExists(Paths.get(localFile))) {
                    println("local file not exist.")
                    channel.close()
                    return
                }
                val createRequest = CreateRequest(localFile, remoteFile, Files.size(Paths.get(localFile)))
                channel.writeAndFlush(Codec.INSTANCE.encode(channel.alloc().ioBuffer(), createRequest))
            }
        }
    }

}
