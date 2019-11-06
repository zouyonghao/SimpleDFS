package cn.edu.tsinghua.sdfs.client;


import cn.edu.tsinghua.sdfs.client.console.SendFileConsole;
import cn.edu.tsinghua.sdfs.client.handler.FileSendHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;

public class Client {

    private static final String HOST = System.getProperty("host", "127.0.0.1");

    private static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

    public static void main(String[] args) throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();

        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("streamer", new ChunkedWriteHandler());
                        pipeline.addLast(new FileSendHandler());
                    }
                });

        ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
        if (future.isSuccess()) {
            System.out.println("连接服务器成功");
            Channel channel = future.channel();
            console(channel);
        } else {
            System.out.println("连接服务器失败");
        }

        future.channel().closeFuture().sync();

    }


    // TODO: add a state machine to exec user commands.
    private static void console(Channel channel) {
        new Thread(() -> SendFileConsole.exec(channel)).start();
    }

}
