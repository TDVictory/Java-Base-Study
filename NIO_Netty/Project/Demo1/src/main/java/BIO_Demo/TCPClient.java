package BIO_Demo;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        //1.创建socket对象
        Socket socket = new Socket("127.0.0.1",9999);

        //2.从连接中取出输入流并发消息
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入消息");
        String msg = scanner.nextLine();
        printWriter.println(msg);
        printWriter.flush();

        //3.从连接中取出输出流
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("客户端返回：" + br.readLine());

        //4.关闭连接
        printWriter.close();
        br.close();
        socket.close();
    }
}
