# NIO 与 Netty 编程

写在最前：本篇基于传智播客的Netty教案学习。

# 一、多线程编程

## 1.1 基本知识

​	线程是比进程更小的，能独立运行的基本单位，它是进程的一部分，一个进程可以拥有多个线程，但至少要有一个线程，即主执行线程（Java 的 main 方法）。我没既可以编写单线程应用，也可以编写多线程应用。

​	一个进程的多个线程可以并发执行，一些执行时间长、需要等待的任务上（例如文件的读写和网络传输等），多线程就比较有用了。

​	多线程可以共享内存、充分利用CPU，通过提高资源（内存和CPU）使用率，从而提高程序的执行效率。CPU使用抢占式调度模式在多个线程间进行着随机的高速切换。对于CPU的一个核来说，某个时刻只能执行一个线程，而CPU在多个线程间的切换速度非常快，看上去像是多个线程或任务同时运行。

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

​	多个线程操作的是同一个共享资源， 但是线程之间是彼此独立、 互相隔绝的， 因此就会 出现数据（共享资源） 不能同步更新的情况， 这就是线程安全问题。    

### 1.2.2 解决线程安全问题

​	Java 中提供了一个同步机制(锁)来解决线程安全问题， 即让操作共享数据的代码在某一 时间段， 只被一个线程执行(锁住)， 在执行过程中， 其他线程不可以参与进来， 这样共享数 据就能同步了。 简单来说， 就是给某些代码加把锁。    

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

​	StringBuffer 和 Vector 类中的大部分方法都是同步方法，这两个类在使用时是保证线程安全的； 而 StringBuilder 和 ArrayList 类中的方法都是普通方法， 没有使用 synchronized 关键字进行修饰， 所以证明这两个类在使用时不保证线程安全。 线程 安全和性能之间不可兼得， 保证线程安全就会损失性能， 保证性能就不能满足线程安全。    

## 1.3 线程间通信

​	多个线程并发执行时, 在默认情况下 CPU 是随机性的在线程之间进行切换的， 但是有时 候我们希望它们能有规律的执行, 那么， 多线程之间就需要一些协调通信来改变或控制 CPU 的随机性。 

​	Java 提供了等待唤醒机制来解决这个问题， 具体来说就是多个线程依靠一个同步 锁， 然后借助于 **wait()**和 **notify()**方法就可以实现线程间的协调通信。 同步锁相当于中间人的作用， 多个线程必须用同一个同步锁(认识同一个中间人)， 只有 同一个锁上的被等待的线程， 才可以被持有该锁的另一个线程唤醒， 使用不同锁的线程之间 不能相互唤醒， 也就无法协调通信。

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

​	该模式在现实生活中很常见， 在项目开发中也广泛应用， 它是线程间通信的经典应用。 生产者是一堆线程， 消费者是另一堆线程， 内存缓冲区可以使用 List 集合存储数据。 该模式 的关键之处是如何处理多线程之间的协调通信， 内存缓冲区为空的时候， 消费者必须等待， 而内存缓冲区满的时候， 生产者必须等待， 其他时候可以是个动态平衡。    

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

# 二、BIO 编程

BIO有的称之为basic（基本）IO，有的称之为block（阻塞）IO，主要应用于文件 IO 和网络 IO 。

​	在JDK1.4之前，我们建立网络连接只能采用BIO，需要先在服务器启动一个ServerSocket，然后再客户端启动Socket来对服务器进行通信，默认情况下服务端需要对每个请求建立一个线程进行通信。二客户端发送请求后，先咨询服务端是否有线程响应，如果没有则会一直等待或者遭到拒绝；如果有的话，客户端线程会等待请求结束后才继续执行，这就是阻塞式 IO。

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

​	java.nio 全称 java non-blocking IO， 是指 JDK 提供的新 API。 从 JDK1.4 开始， Java 提供了 一系列改进的输入/输出的新特性， 被统称为 NIO(即 New IO)。 新增了许多用于处理输入输出 的类， 这些类都被放在 java.nio 包及子包下， 并且对原 java.io 包中的很多类进行改写， 新增 了满足 NIO 的功能。    

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO01.png)

​	NIO 和 BIO 有着相同的目的和作用，但是他们的实现方式完全不同，BIO 以流的方式处理数据，而NIO以块的方式处理数据，块 I/O 的效率比流 I/O 高很多。另外，NIO 是非阻塞式的，这一点跟 BIO 也很不同，使用它可以提供非阻塞式的高伸缩性网络。

​	NIO 主要有三大核心部分：Channel（通道），Buffer（缓冲区），Selector（选择器）。传统的 BIO 基于字节流和字符流进行操作，而 NIO 基于 Channel 和 Buffer 进行操作。数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中。Selector 用于监听多个通道的事件（比如：连接请求，数据到达等），因此使用单个线程就可以监听多个客户端通道。

## 3.2 文件 IO

### 3.2.1 概述和核心 API

#### Buffer（缓冲区）

​	缓冲区（Buffer）：实际上是一个容器，是一个特殊的数组，缓冲区对象内置了一些机制，能够跟踪和记录缓冲区的状态变化情况。Channel提供从文件、网络读取数据的渠道，但是读取或写入的数据都必须经由 Buffer。

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

​	通道（ Channel） ： 类似于 BIO 中的 stream， 例如 FileInputStream 对象， 用来建立到目 标（ 文件， 网络套接字， 硬件设备等） 的一个连接， 但是需要注意： BIO 中的 stream 是单向 的， 例如 FileInputStream 对象只能进行读取数据的操作， 而 NIO 中的通道(Channel)是双向的， 既可以用来进行读操作， 也可以用来进行写操作。 常用的 Channel 类有： 

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

​	NIO 中的通道是从输出流对象里通过 getChannel 方法获取到的， 该通道是双向的， 既可 以读， 又可以写。 在往通道里写数据之前， 必须通过 put 方法把数据存到 ByteBuffer 中， 然 后通过通道的 write 方法写数据。 在 write 之前， 需要调用 flip 方法翻转缓冲区， 把内部重置 到初始位置， 这样在接下来写数据时才能把所有数据写到通道里。 

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

​	从输入流中获得一个通道， 然后提供 ByteBuffer 缓冲区， 该缓冲区的初始容量 和文件的大小一样， 最后通过通道的 read 方法把数据读取出来并存储到了 ByteBuffer 中。    

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

​	上述代码分别从两个流中得到两个通道， sourceCh 负责读数据， destCh 负责写数据， 然 后直接调用 transferFrom 方法一步到位实现了文件复制。    

## 3.3 网络 IO

### 3.3.1 概述和核心 API

​	前面在进行文件 IO 时用到的 FileChannel 并不支持非阻塞操作， 学习 NIO 主要就是进行 网络 IO， Java NIO 中的网络通道是非阻塞 IO 的实现， 基于事件驱动， 非常适用于服务器需 要维持大量连接， 但是数据交换量不大的情况， 例如一些即时通信的服务等等.... 在 Java 中编写 Socket 服务器， 通常有以下几种模式：

- 一个客户端连接用一个线程，优点：程序编写简单；缺点：如果连接非常多，分配的线 程也会非常多，服务器可能会因为资源耗尽而崩溃。
- 把每一个客户端连接交给一个拥有固定数量线程的连接池，优点： 程序编写相对简单，可以处理大量的连接。 确定：线程的开销非常大，连接如果非常多，排队现象会比较严重。    
- 使用 Java 的 NIO， 用非阻塞的 IO 方式处理。这种模式可以用一个线程，处理大量的客户端连接。    

#### 1. Selector

​	Selector(选择器)， 能够检测多个注册的通道上是否有事件发生，如果有事件发生，便获取事件然后针对每个事件进行相应的处理。这样就可以只用一个单线程去管理多个通道，也就是管理多个连接。这样使得只有在连接真正有读写事件发生时， 才会调用函数来进行读写，就大大地减少了系统开销，并且不必为每个连接都创建一个线程，不用去维护多个线程，并且避免了多线程之间的上下文切换导致的开销。

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

​	SocketChannel， 网络 IO 通道， 具体负责进行读写操作。 NIO 总是把缓冲区的数据写入通 道， 或者把通道里的数据读到缓冲区。 

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

API 学习完毕后，接下来我们使用 NIO 开发一个入门案例，实现服务器端和客户端之间 的数据通信（ 非阻塞）   

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO04.png)

```java
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
```

上面代码用 NIO 实现了一个服务器端程序，能不断接受客户端连接并读取客户端发过来的数据    

```java
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
```

### 3.3.3 网络聊天案例

```java
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
```

```java
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

```

```java
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
```

## 3.4 AIO 编程

​	JDK 7 引入了 Asynchronous I/O， 即 AIO。 在进行 I/O 编程中， 常用到两种模式： Reactor 和 Proactor。 Java 的 NIO 就是 Reactor， 当有事件触发时， 服务器端得到通知， 进行相应的 处理。 AIO 即 NIO2.0， 叫做异步不阻塞的 IO。 AIO 引入异步通道的概念， 采用了 Proactor 模式， 简化了程序编写， 一个有效的请求才启动一个线程， 它的特点是先由操作系统完成后才通知 服务端程序启动线程去处理， 一般适用于连接数较多且连接时间较长的应用。    

## 3.5 IO 对比总结

IO 的方式通常分为几种： 同步阻塞的 BIO、 同步非阻塞的 NIO、 异步非阻塞的 AIO。 

- BIO 方式适用于连接数目比较小且固定的架构， 这种方式对服务器资源要求比较高， 并 发局限于应用中， JDK1.4 以前的唯一选择， 但程序直观简单易理解。 

- NIO 方式适用于连接数目多且连接比较短（轻操作） 的架构， 比如聊天服务器， 并发局 限于应用中， 编程比较复杂， JDK1.4 开始支持。 

- AIO 方式使用于连接数目多且连接比较长（重操作） 的架构， 比如相册服务器， 充分调 用 OS 参与并发操作， 编程比较复杂， JDK7 开始支持。

   举个例子再理解一下： 

- 同步阻塞： 你到饭馆点餐， 然后在那等着， 啥都干不了， 饭馆没做好， 你就必须等着！ 

- 同步非阻塞： 你在饭馆点完餐， 就去玩儿了。 不过玩一会儿， 就回饭馆问一声： 好了没 啊！ 

- 异步非阻塞： 饭馆打电话说， 我们知道您的位置， 一会给你送过来， 安心玩儿就可以了， 类似于现在的外卖。    

# 四、Netty

## 4.1 概述

​	Netty 是由 JBOSS 提供的一个 Java 开源框架。 Netty 提供**异步的、 基于事件驱动**的网络 应用程序框架， 用以快速开发高性能、 高可靠性的网络 IO 程序。 

​	Netty 是一个**基于 NIO** 的网络编程框架， 使用 Netty 可以帮助你快速、 简单的开发出一 个网络应用， 相当于简化和流程化了 NIO 的开发过程。 

​	作为当前最流行的 NIO 框架， Netty 在互联网领域、 大数据分布式计算领域、 游戏行业、 通信行业等获得了广泛的应用， 知名的 Elasticsearch 、 Dubbo 框架内部都采用了 Netty。     

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO05.png)

## 4.2 Netty 整体设计

### 4.2.1 线程模型

#### 1. 单线程模型

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO06.png)

​	服务器端用一个线程通过多路复用搞定所有的 IO 操作（包括连接、读、写等），编码简单，清晰明了，但是如果客户端连接数量较多，将无法支撑。我们之前所讲的 NIO 就属于这种模型。

#### 2. 线程池模型

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO07.png)

​	服务器端采用一个线程专门处理客户端连接请求，采用一个线程组负责 IO 操作。在绝大多数场景下，该模型都能满足使用。

#### 3. Netty 模型

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO08.png)

​	比较类似于上面的线程池模型，Netty抽象出两组线程池，BossGroup专门负责接受客户端连接，WorkerGroup 专门负责网络读写操作。NioEventLoop 表示一个不断循环执行处理任务的线程，每个 NioEventLoop 都有一个 selector，用于监听绑定在其上的 socket 网络通道。 NioEventLoop 内部采用串行化设计，从消息的读取->解码->处理->编码->发送，始终由 IO 线程 NioEventLoop负责

- 一个 NioEventLoopGroup 下包含多个 NioEventLoop 
- 每个 NioEventLoop 中包含有一个 Selector， 一个 taskQueue 
- 每个 NioEventLoop 的 Selector 上可以注册监听多个 NioChannel 
- 每个 NioChannel 只会绑定在唯一的 NioEventLoop 上 
- 每个 NioChannel 都绑定有一个自己的 ChannelPipeline    

### 4.2.2 异步模型

#### FUTURE，CALLBACK 和 HANDLER

​	Netty 的异步模型是建立在 future 和 callback 之上的。我们这里重点讲 Future，它的核心思想是：假设一个方法fun，计算过程中可能非常耗时，等待 fun 返回显然不合适。那么可以在调用 fun 的时候，立马返回一个 Future，后续可以通过 Future 去监控方法 fun 的处理过程。

​	在使用 Netty 进行编程时， 拦截操作和转换出入站数据只需要您提供 callback 或利用 future 即可。 这使得链式操作简单、高效、并有利于编写可重用的、通用的代码。Netty 框架的目标就是让你的业务逻辑从网络基础应用编码中分离出来、解脱出来。

​    ![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO09.png)

## 4.3 核心 API

#### ChannelHandler 及其实现类

​	ChannelHandler 接口定义了许多事件处理的方法，我们可以通过重写这些方法去实现业务的具体逻辑。

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO10.png)

​	我们经常需要自定义一个 Handler 类去继承 ChannelInboundHandlerAdapter， 然后通过重写相应方法实现业务逻辑， 我们接下来看看一般都需要重写哪些方法： 

- public void channelActive(ChannelHandlerContext ctx)， 通道就绪事件 
- public void channelRead(ChannelHandlerContext ctx, Object msg)， 通道读取数据事件 
- public void channelReadComplete(ChannelHandlerContext ctx) ， 数据读取完毕事件
- public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)， 通道发生异常事件    

#### Pipeline 和 ChannelPipeline

​	ChannelPipeline 是一个 Handler 的集合， 它负责处理和拦截 inbound 或者 outbound 的事件和操作， 相当于一个贯穿 Netty 的链。    

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO11.png)

- ChannelPipeline **addFirst(ChannelHandler... handlers)**， 把一个业务处理类（ handler） 添加 到链中的第一个位置 
- ChannelPipeline **addLast(ChannelHandler... handlers)**， 把一个业务处理类（ handler） 添加 到链中的最后一个位置    

#### ChannelHandlerContext    

​	这是事件处理上下文对象，Pipeline链中的实际处理节点。每个处理节点 ChannelHandlerContext 中包含一个具体的事件处理器 ChannelHandler，同时 ChannelHandlerContext 中也绑定了对应的 pipeline 和Channel 的信息，方便对 ChannelHandler 进行调用。

常用方法如下：

- ChannelFuture **close()**， 关闭通道
- ChannelOutboundInvoker **flush()**， 刷新
- ChannelFuture **writeAndFlush(Object msg) **， 将 数 据 写 到 ChannelPipeline 中 当 前 ChannelHandler 的下一个 ChannelHandler 开始处理（ 出站）    

#### ChannelOption

​	Netty 在创建 Channel 实例后,一般都需要设置 ChannelOption 参数。 ChannelOption 是 Socket 的标准参数， 而非 Netty 独创的。 

常用的参数配置有： 

1. **ChannelOption.SO_BACKLOG** 对应 TCP/IP 协议 listen 函数中的 backlog 参数， 用来初始化服务器可连接队列大小。 服务端处理客户端连接请求是顺序处理的， 所以同一时间只能处理一个客户端连接。 多个客户 端来的时候， 服务端将不能处理的客户端连接请求放在队列中等待处理， backlog 参数指定了队列的大小。 
2. **ChannelOption.SO_KEEPALIVE** ， 一直保持连接活动状态。    

#### ChannelFuture

​	表示 Channel 中异步 I/O 操作的结果， 在 Netty 中所有的 I/O 操作都是异步的， I/O 的调 用会直接返回， 调用者并不能立刻获得结果， 但是可以通过 ChannelFuture 来获取 I/O 操作 的处理状态。 

常用方法如下所示： 

- Channel **channel()**， 返回当前正在进行 IO 操作的通道
- ChannelFuture **sync()**， 等待异步操作执行完毕    

#### EventLoopGroup 和其实现类 NioEventLoopGroup

​	EventLoopGroup 是一组 EventLoop 的抽象，Netty 为了更好的利用多核 CPU 资源，一般会有多个 EventLoop 同时工作， 每个 EventLoop 维护着一个 Selector 实例。 

​	EventLoopGroup 提供 next 接口，可以从组里面按照一定规则获取其中一个 EventLoop 来处理任务。 在 Netty 服务器端编程中， 我们一般都需要提供两个 EventLoopGroup，例如：BossEventLoopGroup 和 WorkerEventLoopGroup。 

​	通常一个服务端口即一个 ServerSocketChannel对应一个 Selector 和一个 EventLoop 线程。 BossEventLoop 负责接收客户端的连接并将 SocketChannel 交给 WorkerEventLoopGroup 来进行 IO 处理：

![](E:\DyjUser\Study\JavaStudy\Java-Base-Study\NIO_Netty\img\NIO12.png)

​	BossEventLoopGroup 通常是一个单线程的 EventLoop， EventLoop 维护着一个注册了 ServerSocketChannel 的 Selector 实例， BossEventLoop 不断轮询 Selector 将连接事件分离出来， 通常是 OP_ACCEPT 事件， 然后将接收到的 SocketChannel 交给 WorkerEventLoopGroup， WorkerEventLoopGroup 会由 next 选择其中一个 EventLoopGroup 来将这个 SocketChannel 注册到其维护的 Selector 并对其后续的 IO 事件进行处理    

常用方法如下所示：

- public **NioEventLoopGroup()**， 构造方法 
- public Future<?> **shutdownGracefully()**， 断开连接， 关闭线程    

#### ServerBootstrap 和 Bootstrap 

​	ServerBootstrap 是 Netty 中的服务器端启动助手， 通过它可以完成服务器端的各种配置； Bootstrap 是 Netty 中的客户端启动助手， 通过它可以完成客户端的各种配置。 

常用方法如下 所示： 

- public ServerBootstrap **group(EventLoopGroup parentGroup, EventLoopGroup childGroup)**， 该方法用于服务器端， 用来设置两个 EventLoop 
- public B **group(EventLoopGroup group) **， 该方法用于客户端， 用来设置一个 EventLoop
- public B **channel(Class<? extends C> channelClass)**， 该方法用来设置一个服务器端的通道实现
- public <T> B **option(ChannelOption<T> option, T value)**， 用来给 ServerChannel 添加配置 
- public <T> ServerBootstrap **childOption(ChannelOption<T> childOption, T value)**， 用来给接收到的通道添加配置 
- public ServerBootstrap **childHandler(ChannelHandler childHandler)**， 该方法用来设置业务处理类（自定义的 handler）    

#### Unpooled 类 

这是 Netty 提供的一个专门用来操作缓冲区的工具类。

常用方法如下所示： 

- public static ByteBuf **copiedBuffer(CharSequence string, Charset charset)**， 通过给定的数据和字符编码返回一个 ByteBuf 对象（ 类似于 NIO 中的 ByteBuffer 对象）    

## 4.4 入门案例

#### Maven 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.TDVictory</groupId>
    <artifactId>NIO_Netty_Demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>

        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-all</artifactId>
            <version>4.1.36.Final</version>
        </dependency>

    </dependencies>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.2</version>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                        <encoding>UTF-8</encoding>
                        <showWarnings>true</showWarnings>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

</project>
```

#### 服务器 ChannelHandler

```java
//服务器端业务处理类
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    //读取数据事件
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("调试用，ctx为：" + ctx);
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("客户端发来的消息：" + buf.toString(CharsetUtil.UTF_8));
    }

    //数据读取完毕事件
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("没钱",CharsetUtil.UTF_8));
    }

    //异常发生事件
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("服务器发生异常！");
        ctx.close();
    }
}
```

#### 服务器 Server

```java
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        //1. 创建线程组：接收客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //2. 创建一个线程组，处理网络IO操作
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        //3. 创建服务器端启动助手来配置参数
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)    //4. 设置两个线程组
                .channel(NioServerSocketChannel.class)  //5. 使用NioServerSocketChannel作为服务器端通道的实现
                .option(ChannelOption.SO_BACKLOG,128)   //6. 设置线程队列中等待连接的个数
                .childOption(ChannelOption.SO_KEEPALIVE,true)   //7. 保持活动连接状态
                .childHandler(new ChannelInitializer<SocketChannel>() { //8. 创建一个通道初始化对象
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {  //9. 往Pipeline链中添加自定义的handler类
                        socketChannel.pipeline().addLast(new NettyServerHandler());
                    }
                });
        System.out.println("......Server is ready......");
        ChannelFuture cf = serverBootstrap.bind(9999).sync();  //10. 绑定端口，非阻塞
        System.out.println("......Server is starting......");

        //11. 关闭通道，关闭线程组
        cf.channel().closeFuture().sync();

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
```

#### 客户端 ChannelHandler

```java
//客户端业务处理类
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client测试，ctx:" + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("老板还钱", CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("服务器端发来的消息：" + buf.toString(CharsetUtil.UTF_8));
    }
}
```

#### 客户端 Client

```java
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
```

## 4.5 网络聊天案例

```java
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    public static List<Channel> channels = new ArrayList<>();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel inChannel = ctx.channel();
        channels.add(inChannel);
        System.out.println("[Server]:" + inChannel.remoteAddress().toString().substring(1) + "上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel inChannel = ctx.channel();
        channels.remove(inChannel);
        System.out.println("[Server]:" + inChannel.remoteAddress().toString().substring(1) + "离线");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        Channel inChannel = channelHandlerContext.channel();
        for (Channel channel:channels
             ) {
            if(channel != inChannel){
                channel.writeAndFlush("[" + inChannel.remoteAddress().toString().substring(1) + "]: " + s + "\n");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[Server]:"+incoming.remoteAddress().toString().substring(1)+"异常");
        ctx.close();
    }
}
```

上述代码通过继承 SimpleChannelInboundHandler 类自定义了一个服务器端业务处理类， 并在该类中重写了四个方法， 当通道就绪时， 输出在线； 当通道未就绪时， 输出下线； 当通 道发来数据时， 读取数据； 当通道出现异常时， 关闭通道。    

```java
public class ChatServer {
    private int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast("decoder",new StringDecoder());
                            channelPipeline.addLast("encoder",new StringEncoder());
                            channelPipeline.addLast(new ChatServerHandler());
                        }
                    });

            System.out.println("Netty服务器启动");

            ChannelFuture cf = serverBootstrap.bind(port).sync();
            cf.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

    public static void main(String[] args) throws InterruptedException {
        ChatServer server = new ChatServer(9999);
        server.run();
    }
}
```

上述代码通过 Netty 编写了一个服务器端程序， 里面要特别注意的是： 我们往 Pipeline 链中添加了处理字符串的编码器和解码器， 它们加入到 Pipeline 链中后会自动工作， 使得我 们在服务器端读写字符串数据时更加方便（不用人工处理 ByteBuf）     

```java
public class ChatClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println(s.trim());
    }
}
```

上述代码通过继承 SimpleChannelInboundHandler 自定义了一个客户端业务处理类， 重写了一个方法用来读取服务器端发过来的数据    

```java
public class ChatClient {
    private String HOST;
    private int PORT;

    public ChatClient(String HOST, int PORT) {
        this.HOST = HOST;
        this.PORT = PORT;
    }

    public void run(){
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast("decoder",new StringDecoder());
                            channelPipeline.addLast("encoder",new StringEncoder());
                            channelPipeline.addLast(new ChatClientHandler());
                        }
                    });
            ChannelFuture cf = bootstrap.connect(HOST,PORT).sync();
            Channel channel = cf.channel();
            System.out.println("------" + channel.localAddress().toString().substring(1) + "------");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()){
                String msg = scanner.nextLine();
                channel.writeAndFlush(msg);
            }
        }catch (Exception e){

        }finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1",9999);
        client.run();
    }
}
```

上述代码通过 Netty 编写了一个客户端程序， 里面要特别注意的是： 我们往 Pipeline 链 中添加了处理字符串的编码器和解码器， 他们加入到 Pipeline 链中后会自动工作， 使得我们 在客户端读写字符串数据时更加方便（不用人工处理 ByteBuf）    



## 4.6 编码和解码

