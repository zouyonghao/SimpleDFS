package cn.edu.tsinghua.sdfs.client.console;

import cn.edu.tsinghua.sdfs.codec.Codec;
import cn.edu.tsinghua.sdfs.protocol.FilePacket;
import io.netty.channel.Channel;

import java.io.File;
import java.util.Scanner;

public class SendFileConsole {

    public static void exec(Channel channel) {
        Scanner sc = new Scanner(System.in);
        System.out.println("please input the file path: ");
        String path = sc.nextLine();
        File file = new File(path);
        FilePacket filePacket = new FilePacket(file);
        channel.writeAndFlush(Codec.INSTANCE.encode(channel.alloc().ioBuffer(), filePacket));
    }

}
