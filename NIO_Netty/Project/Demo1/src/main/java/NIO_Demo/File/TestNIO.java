package NIO_Demo.File;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestNIO {
    @Test
    public void readTest() throws IOException {
        //1.创建输出流
        FileOutputStream fos = new FileOutputStream("basic.txt");
        //2.从流中得到一个通道
        FileChannel fc = fos.getChannel();
        //3.提供一个缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //4.往缓冲区中存入数据
        buffer.put("hello,nio".getBytes());
        //5.反转缓冲区
        buffer.flip();
        //6.把缓冲区写到通道中
        fc.write(buffer);
        //7.关闭
        fos.close();
    }

    @Test
    public void writeTest() throws IOException{
        //1.创建输入流
        File file = new File("basic.txt");
        FileInputStream fis = new FileInputStream(file);

        //2.获取通道
        FileChannel fileChannel = fis.getChannel();
        //3.提供缓冲区
        ByteBuffer buffer = ByteBuffer.allocate((int)file.length());
        //4.读入数据
        fileChannel.read(buffer);
        //5.输出数据
        System.out.println(new String(buffer.array()));
        //6.关闭
        fis.close();
    }

    //传统BIO复制
    @Test
    public void bioCopy() throws Exception{
        FileInputStream fis=new FileInputStream("C:\\Users\\zdx\\Desktop\\oracle.mov");
        FileOutputStream fos=new FileOutputStream("d:\\oracle.mov");
        byte[] b=new byte[1024];
        while (true) {
            int res=fis.read(b);
            if(res==-1){
                break;
            }
            fos.write(b,0,res);
        }
        fis.close();
        fos.close();
    }

    //NIO复制
    @Test
    public void nioCopyTest() throws Exception{
        FileInputStream fis=new FileInputStream("basic.txt");
        FileOutputStream fos=new FileOutputStream("e:\\basic.txt");
        FileChannel sourceCh = fis.getChannel();
        FileChannel destCh = fos.getChannel();
        destCh.transferFrom(sourceCh, 0, sourceCh.size());
        sourceCh.close();
        destCh.close();
    }
}
