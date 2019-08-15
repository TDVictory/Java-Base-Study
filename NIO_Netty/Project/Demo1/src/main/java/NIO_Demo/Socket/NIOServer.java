package NIO_Demo.Socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

//网络服务器端程序
public class NIOServer {
    public static void main(String[] args) throws IOException {
        //1.得到ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //2.得到Selector
        Selector selector = Selector.open();

        //3.绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(9999));

        //4.设置非阻塞
        serverSocketChannel.configureBlocking(false);

        //5.把ServerSocketChannel注册给Selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //6.逻辑整体
        while (true){
            //6.1 监控客户端
            if(selector.select(2000) == 0){
                System.out.println("服务器端：当前没有客户端连接");
                continue;
            }

            //6.2 得到SelectionKey，判断通道里的事件
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey selectionKey = keyIterator.next();
                if(selectionKey.isAcceptable()){    //客户端连接事件
                    System.out.println("客户端连接");
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }
                if (selectionKey.isReadable()){     //客户端读取事件
                    System.out.println("客户端读取事件");
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    channel.read(buffer);
                    System.out.println("客户端发来数据：" + new String(buffer.array()));
                }
                //6.3 手动移除当前key，防止重复处理
                keyIterator.remove();
            }
        }
    }
}
