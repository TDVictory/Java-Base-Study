package NIO_Demo.Chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

//聊天程序客户端
public class ChatClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9999;
    private String userName;
    private SocketChannel socketChannel;
    private InetSocketAddress address;

    public ChatClient() {

        try {
            address = new InetSocketAddress(HOST,PORT);
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            if(!socketChannel.connect(address)){
                while (!socketChannel.finishConnect()){
                    System.out.println("持续连接服务器中...");
                }
            }
            userName = socketChannel.getLocalAddress().toString().substring(1);
            System.out.println(("---Client(" + userName + ") is ready!---"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //向服务器发送消息
    public void sendMsg(String msg) throws IOException {
        if(msg.equalsIgnoreCase("bye")){
            socketChannel.close();
            return;
        }
        msg = userName + "说：" + msg;
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        socketChannel.write(buffer);
    }

    //从服务器端接受数据
    public void receiveMsg() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int size = socketChannel.read(buffer);
        if(size > 0){
            String msg = new String(buffer.array());
            System.out.println(msg.trim());
        }

    }
}
