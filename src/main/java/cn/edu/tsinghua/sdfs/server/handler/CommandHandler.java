package cn.edu.tsinghua.sdfs.server.handler;

import cn.edu.tsinghua.sdfs.codec.Codec;
import cn.edu.tsinghua.sdfs.protocol.FilePacket;
import cn.edu.tsinghua.sdfs.protocol.Packet;
import cn.edu.tsinghua.sdfs.protocol.command.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;

import static cn.edu.tsinghua.sdfs.protocol.command.Command.FILE_PACKET;

public class CommandHandler extends ChannelInboundHandlerAdapter {

    // TODO: support multi files.
    private FileOutputStream outputStream;

    public CommandHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        int type = byteBuf.getInt(0);
        if (type != Codec.TYPE) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            outputStream.write(bytes);
            System.out.println(msg);
            byteBuf.release();
        } else {
            Packet packet = Codec.INSTANCE.decode(byteBuf);
            switch (packet.getCommand()) {
                case FILE_PACKET:
                    FilePacket filePacket = (FilePacket) packet;
                    System.out.println("receive file from client: " + filePacket.getFile().getName());
                    outputStream = new FileOutputStream(new File("./receive-" + filePacket.getFile().getName()));
                    ctx.channel().writeAndFlush(Codec.INSTANCE.encode(ctx.channel().alloc().ioBuffer(), filePacket));
                    break;
                    // TODO: add more commands.
                default:
                    // pass
                    break;
            }
        }

    }
}
