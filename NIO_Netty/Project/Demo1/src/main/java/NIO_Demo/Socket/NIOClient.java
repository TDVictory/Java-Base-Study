package NIO_Demo.Socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

//网络客户端程序
public class NIOClient {
    public static void main(String[] args) throws IOException {
        //1.得到一个网络通道
        SocketChannel socketChannel = SocketChannel.open();

        //2.设置阻塞方式
        socketChannel.configureBlocking(false);

        //3.提供服务器的IP地址和端口号
        InetSocketAddress address = new InetSocketAddress("127.0.0.1",9999);

        //4.连接服务器
        if (!socketChannel.connect(address)){
            while (!socketChannel.finishConnect()){
                System.out.println("客户端可以执行额外工作");
            }
        }

        //5.得到缓冲区，存入数据
        String msg = "Hello,Server";
        ByteBuffer writeBuf = ByteBuffer.wrap(msg.getBytes());

        //6.发送数据
        socketChannel.write(writeBuf);

        System.in.read();

    }
}
