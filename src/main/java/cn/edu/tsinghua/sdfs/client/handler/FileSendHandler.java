package cn.edu.tsinghua.sdfs.client.handler;

import cn.edu.tsinghua.sdfs.codec.Codec;
import cn.edu.tsinghua.sdfs.protocol.FilePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;

public class FileSendHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        FilePacket filePacket = (FilePacket) Codec.INSTANCE.decode(byteBuf);
        System.out.println("prepared send: " + filePacket.getFile().getName());

        Channel channel = ctx.channel();
        channel.writeAndFlush(new ChunkedFile(new File(filePacket.getFile().getName())));

    }
}
