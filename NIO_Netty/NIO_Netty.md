# NIO 与 Netty 编程

# 一、多线程编程

## 1.1 基本知识

线程是比进程更小的，能独立运行的基本单位，它是进程的一部分，一个进程可以拥有多个线程，但至少要有一个线程，即主执行线程（Java 的 main 方法）。我没既可以编写单线程应用，也可以编写多线程应用。

一个进程的多个线程可以并发执行，一些执行时间长、需要等待的任务上（例如文件的读写和网络传输等），多线程就比较有用了。

多线程可以共享内存、充分利用CPU，通过提高资源（内存和CPU）使用率，从而提高程序的执行效率。CPU使用抢占式调度模式在多个线程间进行着随机的高速切换。对于CPU的一个核来说，某个时刻只能执行一个线程，而CPU在多个线程间的切换速度非常快，看上去像是多个线程或任务同时运行。

Java通过两种编程方式支持多线程：

- 继承 Thread 类

```java
//继承 Thread 类
public class ThreadFor1 extends Thread{
    public void run() {
        for (int i = 0; i < 50; i++) {
            System.out.println(this.getName()+":"+i);
        }
    }
}
```

```java
//执行线程
public class TestThreadFor {
    public static void main(String[] args) {
        ThreadFor1 tf1=new ThreadFor1();
        tf1.setName("线程 A");
        tf1.start();
    }
}
```



- 实现 Runnable 接口

```java
public class RunnableFor1 implements Runnable{
    public void run() {
        for (int i = 0; i < 50; i++) {
            System.out.println(Thread.currentThread().getName()+":"+i);
        }
    }
}
```

```java
//执行线程
public class TestRunnableFor {
    public static void main(String[] args) {
        Thread t1=new Thread(new RunnableFor1());
        t1.setName("线程 A");
        t1.start();
    }
}
```

## 1.2 线程安全

### 1.2.1 产生线程安全问题的原因

多个线程操作的是同一个共享资源， 但是线程之间是彼此独立、 互相隔绝的， 因此就会 出现数据（共享资源） 不能同步更新的情况， 这就是线程安全问题。    

### 1.2.2 解决线程安全问题

Java 中提供了一个同步机制(锁)来解决线程安全问题， 即让操作共享数据的代码在某一 时间段， 只被一个线程执行(锁住)， 在执行过程中， 其他线程不可以参与进来， 这样共享数 据就能同步了。 简单来说， 就是给某些代码加把锁。    

Java 的同步机制提供了两种实现方式：

- 同步代码块： 即给代码块上锁， 变成同步代码块
- 同步方法： 即给方法上锁， 变成同步方法    

```java
synchronized (this) { 
    //同步代码块
} 

//同步方法
public synchronized void saleOne(){}
```

### 1.2.3 Java API 中的线程安全问题

StringBuffer 和 Vector 类中的大部分方法都是同步方法，这两个类在使用时是保证线程安全的； 而 StringBuilder 和 ArrayList 类中的方法都是普通方法， 没有使用 synchronized 关键字进行修饰， 所以证明这两个类在使用时不保证线程安全。 线程 安全和性能之间不可兼得， 保证线程安全就会损失性能， 保证性能就不能满足线程安全。    

## 1.3 线程间通信

多个线程并发执行时, 在默认情况下 CPU 是随机性的在线程之间进行切换的， 但是有时 候我们希望它们能有规律的执行, 那么， 多线程之间就需要一些协调通信来改变或控制 CPU 的随机性。 

Java 提供了等待唤醒机制来解决这个问题， 具体来说就是多个线程依靠一个同步 锁， 然后借助于 **wait()**和 **notify()**方法就可以实现线程间的协调通信。 同步锁相当于中间人的作用， 多个线程必须用同一个同步锁(认识同一个中间人)， 只有 同一个锁上的被等待的线程， 才可以被持有该锁的另一个线程唤醒， 使用不同锁的线程之间 不能相互唤醒， 也就无法协调通信。

- public final void wait()：当前线程释放锁
- public final native void wait(long timeout)：当前线程释放锁，并等待 timeout 毫秒
- public final native void notify()：唤醒持有同一锁的某个线程
- public final native void notifyAll()：唤醒持有同一锁的所有线程

#### 案例1：线程交替运行

```java
//自制锁对象
public class MyLock {
    public static Object o = new Object();
}
```

```java
//循环输出10个1
public class Thread01 extends Thread{
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            //以自制锁作为线程锁
            synchronized (MyLock.o){
                System.out.println(1);
                //执行完当前输出后唤醒其他线程并休眠自己
                MyLock.o.notify();
                try {
                    MyLock.o.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

```java
//循环输出10个2
public class Thread02 extends Thread{
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            synchronized (MyLock.o){
                System.out.println(2);
                MyLock.o.notify();
                try {
                    MyLock.o.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

```java
//1和2交替输出
public class TestThread {
    public static void main(String[] args) {
        Thread01 thread01 = new Thread01();
        Thread02 thread02 = new Thread02();
        thread01.start();
        thread02.start();
    }
}
```

#### 案例2：生产者与消费者

该模式在现实生活中很常见， 在项目开发中也广泛应用， 它是线程间通信的经典应用。 生产者是一堆线程， 消费者是另一堆线程， 内存缓冲区可以使用 List 集合存储数据。 该模式 的关键之处是如何处理多线程之间的协调通信， 内存缓冲区为空的时候， 消费者必须等待， 而内存缓冲区满的时候， 生产者必须等待， 其他时候可以是个动态平衡。    

```java
public class Kuang {
    //这个集合就是水果筐 假设最多存 10 个水果
    public static ArrayList<String> kuang=new ArrayList<String>();
}
```

上述代码定义一个静态集合作为内存缓冲区用来存储数据， 同时这个集合也可以作为锁去被 多个线程使用    

```java
public class Child extends Thread {
    @Override
    public void run() {
        //小孩的工作是不断吃水果
        while (true){
            synchronized (Kuang.kuang){
                //如果框内水果等于0，小孩休息
                if(Kuang.kuang.size() == 0){
                    try {
                        Kuang.kuang.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //吃水果
                Kuang.kuang.remove("fruit");
                System.out.println("小孩吃了一个水果，现在框内还有" + Kuang.kuang.size() + "个水果");
                Kuang.kuang.notify();
            }

            //控制速度
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

```java
public class Farmer extends Thread{
    @Override
    public void run() {
        //农夫的工作是不断放水果
        while (true){
            synchronized (Kuang.kuang){
                //如果框内水果超过10，农夫休息
                if(Kuang.kuang.size() == 10){
                    try {
                        Kuang.kuang.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //放置水果
                Kuang.kuang.add("fruit");
                System.out.println("农夫放进了一个水果，现在框内还有" + Kuang.kuang.size() + "个水果");
                Kuang.kuang.notify();
            }

            //控制速度
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

```java
//测试类
public class MainTest {
    public static void main(String[] args) {
        Child child = new Child();
        Farmer farmer = new Farmer();
        child.start();
        farmer.start();
    }
}
```

# 二、BIO

BIO有的称之为basic（基本）IO，有的称之为block（阻塞）IO，主要应用于文件 IO 和网络 IO 。

在JDK1.4之前，我们建立网络连接只能采用BIO，需要先在服务器启动一个ServerSocket，然后再客户端启动Socket来对服务器进行通信，默认情况下服务端需要对每个请求建立一个线程进行通信。二客户端发送请求后，先咨询服务端是否有线程响应，如果没有则会一直等待或者遭到拒绝；如果有的话，客户端线程会等待请求结束后才继续执行，这就是阻塞式 IO。

```java
//服务器程序
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
```

```java
//客户端程序
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
```

# 三、NIO 编程

## 3.1 概述

java.nio 全称 java non-blocking IO， 是指 JDK 提供的新 API。 从 JDK1.4 开始， Java 提供了 一系列改进的输入/输出的新特性， 被统称为 NIO(即 New IO)。 新增了许多用于处理输入输出 的类， 这些类都被放在 java.nio 包及子包下， 并且对原 java.io 包中的很多类进行改写， 新增 了满足 NIO 的功能。    

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO01.png)

NIO 和 BIO 有着相同的目的和作用，但是他们的实现方式完全不同，BIO 以流的方式处理数据，而NIO以块的方式处理数据，块 I/O 的效率比流 I/O 高很多。另外，NIO 是非阻塞式的，这一点跟 BIO 也很不同，使用它可以提供非阻塞式的高伸缩性网络。

NIO 主要有三大核心部分：Channel（通道），Buffer（缓冲区），Selector（选择器）。传统的 BIO 基于字节流和字符流进行操作，而 NIO 基于 Channel 和 Buffer 进行操作。数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中。Selector 用于监听多个通道的事件（比如：连接请求，数据到达等），因此使用单个线程就可以监听多个客户端通道。

## 3.2 文件 IO

### 3.2.1 概述和核心 API

#### Buffer（缓冲区）

缓冲区（Buffer）：实际上是一个容器，是一个特殊的数组，缓冲区对象内置了一些机制，能够跟踪和记录缓冲区的状态变化情况。Channel提供从文件、网络读取数据的渠道，但是读取或写入的数据都必须经由 Buffer。

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO02.png)

在 NIO中，Buffer是一个顶层父类，他是一个抽象类，常用的 Buffer 子类有：

- **ByteBuffer**， 存储字节数据到缓冲区
- **ShortBuffer**， 存储字符串数据到缓冲区    
- **CharBuffer**， 存储字符数据到缓冲区 
- **IntBuffer**， 存储整数数据到缓冲区 
- **LongBuffer**， 存储长整型数据到缓冲区 
- **DoubleBuffer**， 存储小数到缓冲区 
- **FloatBuffer**， 存储小数到缓冲区    

对于 Java 中的基本数据类型， 都有一个 Buffer 类型与之相对应， 最常用的自然是 ByteBuffer 类（ 二进制数据） ， 该类的主要方法如下所示： 

- public abstract ByteBuffer **put(byte[] b)**; 存储字节数据到缓冲区 
- public abstract byte[] **get()**; 从缓冲区获得字节数据 
- public final byte[] **array()**; 把缓冲区数据转换成字节数组 
- public static ByteBuffer **allocate(int capacity)**; 设置缓冲区的初始容量 
- public static ByteBuffer **wrap(byte[] array)**; 把一个现成的数组放到缓冲区中使用 
- public final Buffer **flip()**; 翻转缓冲区， 重置位置到初始位置    

#### Channel（通道）

通道（ Channel） ： 类似于 BIO 中的 stream， 例如 FileInputStream 对象， 用来建立到目 标（ 文件， 网络套接字， 硬件设备等） 的一个连接， 但是需要注意： BIO 中的 stream 是单向 的， 例如 FileInputStream 对象只能进行读取数据的操作， 而 NIO 中的通道(Channel)是双向的， 既可以用来进行读操作， 也可以用来进行写操作。 常用的 Channel 类有： 

- FileChannel 用于文件的数据读写
- DatagramChannel 用于 UDP 的数据读写
- ServerSocketChannel 用于 TCP 的 服务器数据读写。

- SocketChannel 用于 TCP 的 客户端数据读写。

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO03.png)

主要方法如下所示： 

- public int read(ByteBuffer dst) ， 从通道读取数据并放到缓冲区中 
- public int write(ByteBuffer src) ， 把缓冲区的数据写到通道中 
- public long transferFrom(ReadableByteChannel src, long position, long count)， 从目标通道 中复制数据到当前通道
- public long transferTo(long position, long count, WritableByteChannel target)， 把数据从当 前通道复制给目标通道    

### 3.2.2 案例

#### 1.往本地文件中写数据

NIO 中的通道是从输出流对象里通过 getChannel 方法获取到的， 该通道是双向的， 既可 以读， 又可以写。 在往通道里写数据之前， 必须通过 put 方法把数据存到 ByteBuffer 中， 然 后通过通道的 write 方法写数据。 在 write 之前， 需要调用 flip 方法翻转缓冲区， 把内部重置 到初始位置， 这样在接下来写数据时才能把所有数据写到通道里。 

```java
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
```

#### 2. 从本地文件中读数据

从输入流中获得一个通道， 然后提供 ByteBuffer 缓冲区， 该缓冲区的初始容量 和文件的大小一样， 最后通过通道的 read 方法把数据读取出来并存储到了 ByteBuffer 中。    

```java
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
```

#### 3. 文件复制

```java
//传统BIO复制
public void bioCopyTest() throws Exception{
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
```

```java
//NIO复制
public void nioCopyTest() throws Exception{
    FileInputStream fis=new FileInputStream("C:\\Users\\zdx\\Desktop\\oracle.mov");
    FileOutputStream fos=new FileOutputStream("d:\\oracle.mov");
    FileChannel sourceCh = fis.getChannel();
    FileChannel destCh = fos.getChannel();
    destCh.transferFrom(sourceCh, 0, sourceCh.size());
    sourceCh.close();
    destCh.close();
}
```

上述代码分别从两个流中得到两个通道， sourceCh 负责读数据， destCh 负责写数据， 然 后直接调用 transferFrom 方法一步到位实现了文件复制。    

## 3.3 网络 IO

### 3.3.1 概述和核心 API

前面在进行文件 IO 时用到的 FileChannel 并不支持非阻塞操作， 学习 NIO 主要就是进行 网络 IO， Java NIO 中的网络通道是非阻塞 IO 的实现， 基于事件驱动， 非常适用于服务器需 要维持大量连接， 但是数据交换量不大的情况， 例如一些即时通信的服务等等.... 在 Java 中编写 Socket 服务器， 通常有以下几种模式：

- 一个客户端连接用一个线程，优点：程序编写简单；缺点：如果连接非常多，分配的线 程也会非常多，服务器可能会因为资源耗尽而崩溃。
- 把每一个客户端连接交给一个拥有固定数量线程的连接池，优点： 程序编写相对简单，可以处理大量的连接。 确定：线程的开销非常大，连接如果非常多，排队现象会比较严重。    
- 使用 Java 的 NIO， 用非阻塞的 IO 方式处理。这种模式可以用一个线程，处理大量的客户端连接。    

#### 1. Selector

Selector(选择器)， 能够检测多个注册的通道上是否有事件发生，如果有事件发生，便获取事件然后针对每个事件进行相应的处理。这样就可以只用一个单线程去管理多个通道，也就是管理多个连接。这样使得只有在连接真正有读写事件发生时， 才会调用函数来进行读写，就大大地减少了系统开销，并且不必为每个连接都创建一个线程，不用去维护多个线程，并且避免了多线程之间的上下文切换导致的开销。

该类的常用方法如下所示： 

- public static Selector **open()**， 得到一个选择器对象 
- public int **select(long timeout)**，监控所有注册的通道，当其中有 IO 操作可以进行时，将对应的 SelectionKey 加入到内部集合中并返回，参数用来设置超时时间 
- public Set<SelectionKey> **selectedKeys()**，从内部集合中得到所有的 SelectionKey

#### 2. SelectionKey

SelectionKey， 代表了 Selector 和网络通道的注册关系,一共四种： 

- int **OP_ACCEPT**： 有新的网络连接可以 accept， 值为 16 
- int **OP_CONNECT**： 代表连接已经建立， 值为 8 
- int **OP_READ** 和 int **OP_WRITE**： 代表了读、 写操作， 值为 1 和 4    

该类的常用方法有：

- public abstract Selector **selector()**， 得到与之关联的 Selector 对象 
- public abstract SelectableChannel **channel()**， 得到与之关联的通道 
- public final Object **attachment()**， 得到与之关联的共享数据 
- public abstract SelectionKey **interestOps(int ops)**， 设置或改变监听事件 
- public final boolean **isAcceptable()**， 是否可以 accept 
- public final boolean **isReadable()**， 是否可以读 
- public final boolean **isWritable()**， 是否可以写    

#### 3. ServerSocketChannel

ServerSocketChannel， 用来在服务器端监听新的客户端 Socket 连接， 常用方法如下所示： 

- public static ServerSocketChannel **open()**， 得到一个 ServerSocketChannel 通道 
- public final ServerSocketChannel **bind(SocketAddress local)**， 设置服务器端端口号    
- public final SelectableChannel **configureBlocking(boolean block)**， 设置阻塞或非阻塞模式， 取值 false 表示采用非阻塞模式 
- public SocketChannel **accept()**， 接受一个连接， 返回代表这个连接的通道对象 
- public final SelectionKey **register(Selector sel, int ops)**， 注册一个选择器并设置监听事件    

#### 4. SocketChannel

SocketChannel， 网络 IO 通道， 具体负责进行读写操作。 NIO 总是把缓冲区的数据写入通 道， 或者把通道里的数据读到缓冲区。 

常用方法如下所示： 

- public static SocketChannel **open()**， 得到一个 SocketChannel 通道 
- public final SelectableChannel **configureBlocking(boolean block)**， 设置阻塞或非阻塞模式， 取值 false 表示采用非阻塞模式 
- public boolean **connect(SocketAddress remote)**， 连接服务器 
- public boolean **finishConnect()**， 如果上面的方法连接失败， 接下来就要通过该方法完成 连接操作 
- public int **write(ByteBuffer src)**， 往通道里写数据 
- public int **read(ByteBuffer dst)**， 从通道里读数据 
- public final SelectionKey **register(Selector sel, int ops, Object att)**， 注册一个选择器并设置 监听事件， 最后一个参数可以设置共享数据 
- public final void **close()**， 关闭通道    

### 3.3.2 案例



