package netty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        //1. 创建一个线程组
        EventLoopGroup group = new NioEventLoopGroup();
        //2. 创建客户端的启动助手
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)  //3. 设置线程组
                .channel(NioSocketChannel.class)    //4. 设置客户端通道的实现类
                .handler(new ChannelInitializer<SocketChannel>() {  //5. 创建一个通道初始化对象
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("encoder",new ProtobufEncoder());
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });
        System.out.println("......Client is ready......");
        //7. 启动客户端去连接服务器端
        ChannelFuture cf = bootstrap.connect("127.0.0.1",9999).sync();

        //8. 关闭通道
        cf.channel().closeFuture().sync();

    }
}
