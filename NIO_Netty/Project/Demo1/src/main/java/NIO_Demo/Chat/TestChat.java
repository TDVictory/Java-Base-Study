package NIO_Demo.Chat;

import java.io.IOException;
import java.util.Scanner;

//客户端启动
public class TestChat {
    public static void main(String[] args) throws IOException {
        ChatClient chatClient = new ChatClient();

        new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        chatClient.receiveMsg();
                        Thread.sleep(2000);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String msg = scanner.nextLine();
            chatClient.sendMsg(msg);
        }
    }
}
