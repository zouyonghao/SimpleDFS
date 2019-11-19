package cn.edu.tsinghua.sdfs.client.console

import cn.edu.tsinghua.sdfs.client.handler.FileDownloader
import cn.edu.tsinghua.sdfs.client.handler.FileUploader
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.DownloadRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.LsPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import io.netty.channel.Channel
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

object SendFileConsole {

    lateinit var localFile: String
    lateinit var remoteFile: String
    lateinit var masterChannel: Channel

    fun exec(masterChannel: Channel, args: Array<String>) {
        this.masterChannel = masterChannel
        when (args[0]) {
            "ls" -> {
                // println("ls command executing...")
                Codec.writeAndFlushPacket(masterChannel, LsPacket(args[1]))
                // masterChannel.close()
            }
            "copyFromLocal" -> {
                localFile = args[1]
                remoteFile = args[2]
                FileUploader.localFile = localFile
                FileUploader.remoteFile = remoteFile
                if (Files.notExists(Paths.get(localFile))) {
                    println("local file not exist.")
                    masterChannel.close()
                    return
                }
                sendCreateRequest()
            }
            "copyFromRemote" -> {
                remoteFile = args[1]
                localFile = args[2]
                FileDownloader.localFile = localFile
                FileDownloader.remoteFile = remoteFile
                if (Files.exists(Paths.get(localFile))) {
                    println("local file exist.")
                    masterChannel.close()
                    return
                }
                Codec.writeAndFlushPacket(masterChannel, DownloadRequest(remoteFile))
            }
            "submit" -> {
                localFile = args[1]
                if (Files.notExists(Paths.get(localFile))) {
                    masterChannel.close()
                    error("File $localFile not exists.")
                }
                Codec.writeAndFlushPacket(masterChannel,
                        UserProgram(UUID.randomUUID().toString(),
                                String(Files.readAllBytes(Paths.get(localFile)))))
            }
        }
    }

    fun sendCreateRequest() {
        val createRequest = CreateRequest(localFile, remoteFile, Files.size(Paths.get(localFile)))
        Codec.writeAndFlushPacket(masterChannel, createRequest)
    }

}
