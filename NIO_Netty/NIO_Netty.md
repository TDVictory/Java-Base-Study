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
- 