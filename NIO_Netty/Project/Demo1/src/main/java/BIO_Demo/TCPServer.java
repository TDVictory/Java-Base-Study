package BIO_Demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static void main(String[] args) throws IOException {
        //1.创建ServerSocket对象
        ServerSocket ss = new ServerSocket(9999);

        while (true){
            //2.监听客户端
            Socket socket = ss.accept();//阻塞
            System.out.println("监听到客户端");

            //3.从连接中取出输入流来接收消息
            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println(clientIP + "输入：" + bf.readLine());
            System.out.println("输入结束");

            //4.从连接中取出输出流并回复
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            printWriter.write("告辞");
            printWriter.flush();

            //5.关闭连接
            bf.close();
            printWriter.close();
            socket.close();
        }
    }
}
