package NIO_Demo.Chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

//聊天程序服务器端
public class ChatServer {
    private ServerSocketChannel serverSocketChannel;    //监听通道
    private Selector selector;  //轮询器
    private static final int PORT = 9999;   //服务器端口号

    public ChatServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器已就绪！");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start(){

            try {
                while (true){
                    if(selector.select(2000) == 0){
                        System.out.println("当前暂无客户端响应");
                        continue;
                    }
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()){
                        SelectionKey selectionKey = keyIterator.next();
                        if(selectionKey.isAcceptable()){
                            ///ServerSocketChannel ssl = (ServerSocketChannel) selectionKey.channel();
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector,SelectionKey.OP_READ);
                            System.out.println(socketChannel.getRemoteAddress().toString().substring(1) + "已连接...");
                        }
                        else if (selectionKey.isReadable()){
                            readMsg(selectionKey);
                        }
                        keyIterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    //读取客户端发来的消息并广播出去
    private void readMsg(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int count = socketChannel.read(buffer);
        if(count > 0){
            String msg = new String(buffer.array());
            printInfo(msg);

            //发广播
            boardCast(socketChannel,msg);
        }
    }

    //给所有的客户端发送广播
    private void boardCast(SocketChannel socketChannel, String msg) throws IOException {
        System.out.println("服务器发送了广播");
        for (SelectionKey key:selector.keys()
             ) {
            Channel targetChannel = key.channel();
            if(targetChannel instanceof SocketChannel && targetChannel != socketChannel){
                SocketChannel destChannel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                destChannel.write(buffer);
            }
        }

    }


    private void printInfo(String str){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("[" + sdf.format(new Date()) + "] -> " + str);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
