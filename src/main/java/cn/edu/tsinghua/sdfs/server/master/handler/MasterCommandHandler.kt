package cn.edu.tsinghua.sdfs.server.master.handler

import cn.edu.tsinghua.sdfs.exception.WrongCodecException
import cn.edu.tsinghua.sdfs.protocol.Codec
import cn.edu.tsinghua.sdfs.protocol.packet.impl.CreateRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.DownloadRequest
import cn.edu.tsinghua.sdfs.protocol.packet.impl.LsPacket
import cn.edu.tsinghua.sdfs.protocol.packet.impl.ResultToClient
import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import cn.edu.tsinghua.sdfs.server.mapreduce.UserProgramManager
import cn.edu.tsinghua.sdfs.server.master.NameManager
import cn.edu.tsinghua.sdfs.server.master.SlaveManager
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class MasterCommandHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        val type = byteBuf.getInt(0)
        if (type != Codec.TYPE) {
            throw WrongCodecException()
        }
        when (val packet = Codec.decode(byteBuf)) {
            is CreateRequest -> {
                val nameItem = NameManager.createOrGet(packet.remoteFile, packet.fileLength)
                Codec.writeAndFlushPacket(ctx.channel(), nameItem)
            }
            is LsPacket -> {
                Codec.writeAndFlushPacket(ctx.channel(), ResultToClient(NameManager.ls(packet.path)))
            }
            is DownloadRequest -> {
                Codec.writeAndFlushPacket(ctx.channel(), NameManager.getNameItemForDownload(packet.filePath))
            }
            is UserProgram -> {
                UserProgramManager.saveUserProgram(packet)
                SlaveManager.uploadUserProgram(packet)
            }
        }
    }
}
