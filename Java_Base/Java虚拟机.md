# 一、运行时数据区
## 程序计数器
记录正在执行的虚拟机字节码指令的地址，如果正在执行的是本地方法则为空。
**此处不会产生OOM（OutOfMemoryError）异常**

## Java虚拟机栈
每个Java方法在执行的同时会创建一个栈帧用于存储局部变量表、操作数栈、常量池引用等信息。从方法调用直至执行完成的过程，对应着一个栈帧在 Java 虚拟机栈中入栈和出栈的过程。

可以通过-Xss这个虚拟机参数来指定每个线程的Java虚拟机栈内存大小，JDK1.4中默认256k，在JDK1.5以后为1M：

在Java虚拟机栈区域可能抛出以下异常：
- 线程请求栈深度超过最大值，会抛出StackOverflowError异常；
- 栈进行动态扩展时如果无法申请到足够内存，会抛出OutOfMemoryError异常

## 本地方法栈
本地方法栈与 Java 虚拟机栈类似，它们之间的区别只不过是本地方法栈为本地方法服务。

本地方法一般是用其它语言（C、C++ 或汇编语言等）编写的，并且被编译为基于本机硬件和操作系统的程序，对待这些方法需要特别处理。

**上述三类随着线程一起产生和销毁。**

## 堆
所有**对象**都在这里分配内存，时垃圾收集的主要区域（GC堆）

现代垃圾收集器基本都是采用分代收集算法，其主要思想是针对不同类型的对象采用不同的垃圾回收算法。可以将堆分成两块：
- 新生代：以复制算法为主
- 老年代：以标记整理算法为主

堆不需要连续内存，并且可以动态增加其内存，增加失败会抛出OutOfMemoryError异常。

## 方法区
用于存放已被加载的**类信息、常量、静态变量、即时编译器编译后的代码**等数据。

和堆一样不需要连续的内存，并且可以动态扩展，动态扩展失败一样会抛出OOM异常。

对这块区域进行垃圾回收的主要目标是对常量池的回收和对类的卸载，但是一般比较难实现。

从 JDK 1.8 开始，移除永久代，并把方法区移至元空间，它位于本地内存中，而不是虚拟机内存中。

## 运行时常量池
运行时常量池是方法区的一部分。

Class文件中的常量池（编译器生成的字面量和服号引用）会在类加载后被放入这个区域。

除了在编译期生成的常量，还允许动态生成，例如String类的intern()

## 直接内存
在JDK1.4中新引入了NIO类，它可以使用Native函数库直接分配对外内存，然后通过Java堆里的DirectByteBuffer 对象作为这块内存的引用进行操作。这样能在一些场景中显著提高性能，因为避免了在堆内存和堆外内存来回拷贝数据。

# 二、垃圾收集
垃圾收集主要是针对堆和方法去进行。程序计数器、虚拟机栈和本地方法栈这三个区域属于线程私有的，只存在于线程的生命周期内，线程结束之后就会消失，因此不需要对这三个区域进行垃圾回收。

## 判断一个对象是否可以被回收
### 1.引用计数法
为对象添加一个引用计数器，当对象增加一个引用时计数器加 1，引用失效时计数器减 1。引用计数为 0 的对象可被回收。

在两个对象出现循环引用的情况下，此时引用计数器永远不为 0，导致无法对它们进行回收。正是因为循环引用的存在，因此 Java 虚拟机不使用引用计数算法。

### 可达性分析算法
以GC Root为起始点进行搜索，可达的对象都是存活的，不可达的对象可被回收。

Java虚拟机使用该算法来判断对象是否可被回收，GC Roots一般包含以下内容：
- 虚拟机栈中局部变量表中引用的对象
- 本地方法区中JNI中引用的对象
- 方法区中类静态属性引用的对象
- 方法区中常量引用的对象

### 方法区的回收
方法区主要是对常量池的回收和对类的卸载。

为了避免内存溢出，在大量使用反射和动态代理的场景都需要虚拟机具备类卸载功能。

类卸载的条件很多，需要满足以下三个条件，并且满足了条件也不一定会被卸载：
- 该类的所有实例均被回收
- 该类的ClassLoader已被回收
- 该类对应的Class对象没有在任何地方被引用，也无法通过反射访问到该类方法

### finalize()
当一个对象可被回收时，如果需要执行该对象的 finalize() 方法，那么就有可能在该方法中让对象重新被引用，从而实现自救。自救只能进行一次，如果回收的对象之前调用了 finalize() 方法自救，后面回收时不会再调用该方法。

## 引用类型
无论是通过引用计数算法判断对象的引用数量，还是通过可达性分析算法判断对象是否可达，判定对象是否可被回收都与引用有关。
Java 提供了四种强度不同的引用类型。

### 1.强引用
直接new就是强引用,被强引用关联的对象不会被回收。
```java
Object obj = new Object();
```

### 2.软引用
使用SoftReference类创建的为软引用，被软引用关联的对象只有在内存不够的情况下才会被回收。
```java
Object obj = new Object();
SoftReference<Object> sf = new SoftReference<Object>(obj);
obj = null;  // 使对象只被软引用关联
```

### 3.弱引用

使用WeakReference类创建的为弱引用，被弱引用关联的对象一定会被回收，也就是说它只能存活到下一次垃圾回收发生之前。
```java
Object obj = new Object();
WeakReference<Object> wf = new WeakReference<Object>(obj);
obj = null;
```
### 4. 虚引用
又称为幽灵引用或者幻影引用，一个对象是否有虚引用的存在，不会对其生存时间造成影响，也无法通过虚引用得到一个对象。

为一个对象设置虚引用的唯一目的是能在这个对象被回收时收到一个系统通知。

使用 PhantomReference 来创建虚引用。
```java
Object obj = new Object();
PhantomReference<Object> pf = new PhantomReference<Object>(obj, null);
obj = null;
```

## 垃圾收集算法
### 1.标记-清除
在标记阶段，程序会检查每个对象是否为活动对象，如果是活动对象，则程序会在对象头部打上标记。

在清除阶段，会进行对象回收并取消标志位，另外，还会判断回收后的分块与前一个空闲分块是否连续，若连续，会合并这两个分块。回收对象就是把对象作为分块，连接到被称为 “空闲链表” 的单向链表，之后进行分配时只需要遍历这个空闲链表，就可以找到分块。也就是说“空闲链表”中存储着清理完成后空余内存的分布情况。

在下一次分配时，程序会搜索空闲链表寻找空间大于等于新对象大小 size 的块 block。如果它找到的块等于 size，会直接返回这个分块；如果找到的块大于 size，会将块分割成大小为 size 与 (block - size) 的两部分，返回大小为 size 的分块，并把大小为 (block - size) 的块返回给空闲链表。

**不足**：
- 标记和清除过程的效率都不高；
- 会产生大量不连续的内存碎片，导致无法给大对象分配内存。

### 2.标记-整理
在标记后移动所有的存活对象至内存的一端，然后清除掉边界以外的所有内存。

**优点**
- 不会产生碎片

**缺点**
- 因为要移动存活对象，效率更为底下

### 3.复制
将内存划分为大小相等的两块，每次只使用其中一块，当这一块内存用完了就将还存活的对象复制到另一块上面，然后再把使用过的内存空间进行一次清理。

主要不足是**只使用了内存的一半**。

现在的商业虚拟机都采用这种收集算法回收**新生代**，但是并不是划分为大小相等的两块，而是一块较大的 Eden 空间和两块较小的 Survivor 空间，每次使用 Eden 和其中一块 Survivor。在回收时，将 Eden 和 Survivor 中还存活着的对象全部复制到另一块 Survivor 上，最后清理 Eden 和使用过的那一块 Survivor。

HotSpot 虚拟机的 Eden 和 Survivor 大小比例默认为 **8:1**，保证了内存的利用率达到 90%。如果每次回收有多于 10% 的对象存活，那么一块 Survivor 就不够用了，此时需要依赖于老年代进行空间分配担保，也就是借用老年代的空间存储放不下的对象。

### 4.分代收集
现在的商业虚拟机采用分代收集算法，它根据对象存活周期将内存划分为几块，不同块采用适当的收集算法。

一般将堆分为新生代和老年代。
- 新生代使用：复制算法
- 老年代使用：标记 - 清除 或者 标记 - 整理 算法

## 垃圾收集器
垃圾收集器一共分为七种
- 单线程与多线程：单线程指垃圾收集器只使用一个线程，而多线程则使用多个线程
- 串行与并行：串行指的是垃圾收集器与用户程序交替执行，这意味着在执行垃圾收集的时候需要停顿用户程序；并行指的是垃圾收集器和用户程序同时执行。除了CMS和G1，其他垃圾收集器都是以串行的方式执行。

### 1.Serial 收集器
**单线程串行**收集器，优点是简单高效，在单CPU环境下，由于没有线程交互的开销，拥有最高的单线程收集效率。

它是**Client场景下默认的新生代收集器**。

### 2.ParNew 收集器
**多线程串行**收集器，它是 Serial 收集器的多线程版本。

它是**Server场景下默认的新生代收集器**，除了性能原因外，主要是因为除了 Serial 收集器，只有它能与 CMS 收集器配合使用。

### 3.Parallel Scavenge收集器
**多线程串行**收集器。

和其他收集器不同的是，其他收集器的目标是缩短垃圾收集时的用户线程的停顿时间，而它的目标的是达到一个可控制吞吐量，因此它被称为“吞吐量优先”收集器。这里的吞吐量指的是CPU用于运行用户程序的时间占总时间的比值。

停顿时间越短就越适合需要与用户交互的程序，良好的响应速度能提升用户体验。而高吞吐量则可以高效率地利用 CPU 时间，尽快完成程序的运算任务，适合在**后台运算而不需要太多交互的任务。**

### 4.Serial Old 收集器
是 Serial 收集器的老年代版本，也是给 Client 场景下的虚拟机使用。

如果用在 Server 场景下，它有两大用途：
- 在 JDK 1.5 以及之前版本（Parallel Old 诞生以前）中与 Parallel Scavenge 收集器搭配使用。
- 作为 CMS 收集器的后备预案，在并发收集发生 Concurrent Mode Failure 时使用。

### 5.Parallel Old收集器
是 Parallel Scavenge 收集器的老年代版本。

在注重吞吐量以及 CPU 资源敏感的场合，都可以优先考虑 Parallel Scavenge 加 Parallel Old 收集器。

### 6.CMS 收集器
CMS（Concurrent Mark Sweep），Mark Sweep 指的是标记 - 清除算法。

分为以下几个流程
- 初始标记，标记GCRoot能直接关联到的对象（也就是设置起点），速度很快，需要停顿。
- 并发标记，进行GCRoot Tracing，耗时最长不需要停顿
- 重新标记：修正在并发标记期间所有因程序运行而导致标记更改的标记记录，需要停顿。
- 并发清除：不需要停顿。

在整个过程中耗时最长的并发标记和并发清除过程中，收集器线程都可以与用户线程一起工作，不需要进行停顿。

**缺点**
- 低停顿导致的低吞吐量
- 无法处理浮动垃圾，浮动垃圾就是在并发清除过程中由于程序运行从而产生的新的垃圾，这部分垃圾只能在下次GC中清除，需要预留一部分内存来存放浮动垃圾，如果预留的内存不足以存放垃圾，则会出现Concurrent Mode Failure，这时虚拟机将临时启动Serial Old来替代CMS。
- 标记清除算法导致存在大量内存碎片，往往出现老年代空间剩余，但无法找到足够大连续空间来分配当前对象，不得不提前触发一次 Full GC。

### 7.G1 收集器
G1（Garbage-FIrst），它是一款面向服务端应用的垃圾收集器，在多 CPU 和大内存的场景下有很好的性能。HotSpot 开发团队赋予它的使命是未来可以替换掉 CMS 收集器。

堆被分为新生代和老年代，其它收集器进行收集的范围都是整个新生代或者老年代，而 G1 可以直接对新生代和老年代一起回收。

G1把堆划分成多个大小相等的独立区域（Region），新生代和老年代不再物理隔离。


通过引入 Region 的概念，从而将原来的一整块内存空间划分成多个的小空间，使得每个小空间可以单独进行垃圾回收。这种划分方法带来了很大的灵活性，使得可预测的停顿时间模型成为可能。通过记录每个 Region 垃圾回收时间以及回收所获得的空间（这两个值是通过过去回收的经验获得），并维护一个优先列表，每次根据允许的收集时间，优先回收价值最大的 Region。

每个 Region 都有一个 Remembered Set，用来记录该 Region 对象的引用对象所在的 Region。通过使用 Remembered Set，在做可达性分析的时候就可以避免全堆扫描。

如果不计算维护 Remembered Set 的操作，G1 收集器的运作大致可划分为以下几个步骤：
- 初始标记
- 并发标记
- 最终标记：为了修正在并发标记期间因用户程序继续运作而导致标记产生变动的那一部分标记记录，虚拟机将这段时间对象变化记录在线程的 Remembered Set Logs 里面，最终标记阶段需要把 Remembered Set Logs 的数据合并到 Remembered Set 中。这阶段需要停顿线程，但是可并行执行。
- 筛选回收：首先对各个 Region 中的回收价值和成本进行排序，根据用户所期望的 GC 停顿时间来制定回收计划。此阶段其实也可以做到与用户程序一起并发执行，但是因为只回收一部分 Region，时间是用户可控制的，而且停顿用户线程将大幅度提高收集效率。

具备如下特点：
- 空间整合：整体来看是基于“标记 - 整理”算法实现的收集器，从局部（两个 Region 之间）上来看是基于“复制”算法实现的，这意味着运行期间不会产生内存空间碎片。
- 可预测的停顿：能让使用者明确指定在一个长度为 M 毫秒的时间片段内，消耗在 GC 上的时间不得超过 N 毫秒。



# 三、内存分配与回收策略

## Minor GC 和 Full GC

- **Minor GC**：回收新生代，因为新生代对象存活时间很短，因此 Minor GC 会频繁执行，执行的速度一般也会比较快。
- **Full GC**：回收老年代和新生代，老年代对象其存活时间长，因此 Full GC 很少执行，执行速度会比 Minor GC 慢很多。

## 内存分配策略

### 1.对象优先在Eden分配

大多数情况下，对象在新生代Eden上分配，当Eden空间不够时，发起Minor GC。

### 2.大对象直接进入老年代

大对象是指需要连续内存空间的对象，最典型的大对象是那种很长的字符串以及数组。

经常出现大对象会提前触发垃圾收集以获取足够的连续空间分配给大对象

### 3.长期存活的对象进入老年代

为对象定义年龄计数器，对象在Eden出生并经过Minor GC依然存活，将移动到Survivor中，年龄就增加一岁，增加到一定年龄则移动到老年代中。

### 4.动态对象年龄判定

虚拟机并不是永远要求对象的年龄达到年龄计数器的阈值才能晋升老年代，如果在Survivor中相同年龄所有对象的大小总和大于Survivor的一半（也即Minor GC时无法存储），则年龄大于或等于该年龄的对象可以直接进入老年代，无需等到设定值的年龄。

### 5.空间分配担保

在发生Minor GC之前，虚拟机先检查老年代最大可用的连续可用空间是否大于新生代所有对象总空间（最差情况新生代全部无法清除），如果条件成立的话，那么Minor GC可以确认时安全的。

如果不成立的话，虚拟机会查看 HandlePromotionFailure 的值是否允许担保失败，如果允许那么就会继续检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小，如果大于，将尝试着进行一次 Minor GC；如果小于，或者 HandlePromotionFailure 的值不允许冒险，那么就要进行一次 Full GC。

## Full GC 的触发条件

对于Minor GC，其触发条件非常简单，当 Eden 空间满时，就将触发一次 Minor GC。而 Full GC 则相对复杂，有以下条件：

### 1.系统调用System.gc()

只是建议虚拟机执行Full GC，但是虚拟机不一定真正区执行。这种方式不推荐，建议让虚拟机管理内存。

### 2.老年代空间不足

老年代空间不足的常见场景为前文所讲的大对象直接进入老年代、长期存活的对象进入老年代等。

 为了避免以上原因引起的 Full GC，应当尽量不要创建过大的对象以及数组

### 3.空间分配担保失败

使用复制算法的Minor GC需要老年代的内存空间作为担保，如果担保失败会执行一次Full GC

### 4.JDK1.7以前的永久代空间不足

### 5.由CMS收集器带来的Concurrent Mode Failure

执行 CMS GC 的过程中同时有对象要放入老年代，而此时老年代空间不足（可能是 GC 过程中浮动垃圾过多导致暂时性的空间不足），便会报 Concurrent Mode Failure 错误，并触发 Full GC。



# 四、类加载机制

类是在**运行期间第一次使用时动态加载的**，而不是一次性加载所有类。因为如果一次性加载会占用很多内存。

## 类的生命周期

类的生命周期包括以下七个阶段

- **加载（Loading）**
- **验证（Verification）**
- **准备（Preparation）**
- **解析（Resolution）**
- **初始化（Initialization）**
- 使用（Using）
- 卸载（Unloading）

## 类加载过程

包含了加载、验证、准备、解析和初始化五个阶段。

### 1.加载

加载时类加载的一个阶段，注意不要混淆。

在加载过程会完成以下三件事

- 通过类的完全限定名称获取定义该类的二进制字符流
- 将二进制字符流传入JVM的运行时方法区
- 在内存中产生一个该类的Class对象，作为方法区中该类各种数据的访问入口。

其中二进制字节流的获取方式有：

- 从ZIP包读取，成为JAR、EAR、WAR格式的基础
- 从网络中获取
- 运行时计算生成
- 其他文件生成

### 2.验证

确保 Class 文件的字节流中包含的信息符合当前虚拟机的要求，并且不会危害虚拟机自身的安全。

### 3.准备

类变量是被static修饰的变量，准备阶段为类变量分配内存并设置初始值，使用的是方法区的内存。

这里的初始值不是我们代码中的赋值，准备阶段可以看作类变量在方法区的“占座”。

初始值一般为0值，例如下面的类变量value被初始化为0而不是123.

```java
public static int value = 123;
```

如果类变量是常量，那么就会将其初始化为表达式所定义的值而不是0.

```java
public static final int value = 123;
```

实例变量不会在这阶段分配内存，他会在对象实例化时随着对象一起被分配在堆中。应该注意到，实例化不是类加载的一个过程，类加载发生在所有实例化操作之前，并且类加载只进行一次，实例化可以进行多次。

### 4.解析

将常量池的符号引用替换为直接引用的过程。（ 在编译时，java类并不知道引用类的实际内存地址，因此只能使用符号引用来代替。通过解析过程后替换成实际内存地址下的直接引用） 

其中解析过程在某些情况下可以在初始化阶段之后再开始，这是为了支持 Java 的动态绑定。

### 5.初始化

初始化阶段菜真正开始执行类中定义的Java程序代码。初始化阶段是虚拟机执行类构造器<clinit>()方法的过程。在准备阶段，类变量已经赋过一次系统要求的初始值，而在初始化阶段，根据程序员通过程序制定的主观计划去初始化类变量和其它资源。

<clinit>() 是由编译器自动收集类中所有类变量的赋值动作和静态语句块中的语句合并产生的，编译器收集的顺序由语句在源文件中出现的顺序决定。

特别注意的是，静态语句块只能访问到定义在它之前的类变量，定义在它之后的类变量只能赋值，不能访问。例如以下代码：

```java
public class Test {
    static {
        i = 0;                // 给变量赋值可以正常编译通过
        System.out.print(i);  // 这句编译器会提示“非法向前引用”
    }
    static int i = 1;
}
```

由于父类的 <clinit>() 方法先执行，也就意味着父类中定义的静态语句块的执行要优先于子类。例如以下代码：

 

```java
static class Parent {
    public static int A = 1;
    static {
        A = 2;
    }
}

static class Sub extends Parent {
    public static int B = A;
}

public static void main(String[] args) {
     System.out.println(Sub.B);  // 2
}
```

接口中不可以使用静态语句块，但仍然有类变量初始化的赋值操作，因此接口与类一样都会生成 <clinit>() 方法。但接口与类不同的是，执行接口的 <clinit>() 方法不需要先执行父接口的 <clinit>() 方法。只有当父接口中定义的变量使用时，父接口才会初始化。另外，接口的实现类在初始化时也一样不会执行接口的 <clinit>() 方法。

## 类的初始化时机

### 1.主动引用

虚拟机规范中并没有强制约束何时进行加载，但是规范严格规定了有且只有下列五种情况必须对类进行初始化（加载、验证、准备都会随之发生）：

- **遇到 new、getstatic、putstatic、invokestatic 这四条字节码指令**时，如果类没有进行过初始化，则必须先触发其初始化。最常见的生成这 4 条指令的场景是：使用 new 关键字实例化对象的时候；读取或设置一个类的静态字段（被 final 修饰、已在编译期把结果放入常量池的静态字段除外）的时候；以及调用一个类的静态方法的时候。
- 使用 java.lang.reflect 包的方法对类进行**反射调用**的时候，如果类没有进行初始化，则需要先触发其初始化。
- 当初始化一个类的时候，如果发现其**父类**还没有进行过初始化，则需要先触发其父类的初始化。
- 当虚拟机启动时，用户需要指定一个**要执行的主类**（包含 main() 方法的那个类），虚拟机会先初始化这个主类；
- 当使用 JDK 1.7 的动态语言支持时，如果一个 java.lang.invoke.MethodHandle 实例最后的解析结果为 REF_getStatic, REF_putStatic, REF_invokeStatic 的方法句柄，并且这个方法句柄所对应的类没有进行过初始化，则需要先触发其初始化；

### 2.被动引用

以上 5 种场景中的行为称为对一个类进行主动引用。除此之外，所有引用类的方式都不会触发初始化，称为被动引用。被动引用的常见例子包括：

- 通过子类引用父类的静态字段，不会导致子类初始化

```java
System.out.println(SubClass.value);  // value 字段在 SuperClass 中定义
```

- 通过数组定义来引用类，不会触发此类的初始化。该过程会对数组类进行初始化，数组类是一个由虚拟机自动生成的、直接继承自 Object 的子类，其中包含了数组的属性和方法。

```java
SuperClass[] sca = new SuperClass[10];
```

- 常量在编译阶段会存入调用类的常量池中，本质上并没有直接引用到定义常量的类，因此不会触发定义常量的类的初始化。

```java
System.out.println(ConstClass.HELLOWORLD);
```

## 类与类加载器

两个类相等，需要类本身相等，并且使用同一个类加载器进行加载。这是因为每一个类加载器都拥有一个独立的类名称空间。 

这里的相等，包括类的 Class 对象的 equals() 方法、isAssignableFrom() 方法、isInstance() 方法的返回结果为 true，也包括使用 instanceof 关键字做对象所属关系判定结果为 true。

## 类加载器分类

从 Java 虚拟机的角度来讲，只存在以下两种不同的类加载器： 

- 启动类加载器（Bootstrap ClassLoader），使用 C++ 实现，是虚拟机自身的一部分；
- 所有其它类的加载器，使用 Java 实现，独立于虚拟机，继承自抽象类 java.lang.ClassLoader。

从 Java 开发人员的角度看，类加载器可以划分得更细致一些：

- 启动类加载器（Bootstrap ClassLoader），启动类加载器无法被 Java 程序直接引用
- 扩展类加载器（Extension ClassLoader），开发者可以直接使用扩展类加载器。
- 应用程序类加载器（Application ClassLoader），它负责加载用户类路径（ClassPath）上所指定的类库，开发者可以直接使用这个类加载器，如果应用程序中没有自定义过自己的类加载器，一般情况下这个就是程序中默认的类加载器。

## 双亲委派机制

应用程序是由三种类加载器互相配合从而实现类加载，除此之外还可以加入自己定义的类加载器。双亲委派模型要求除了顶层的启动类加载器外，其它的类加载器都要有自己的父类加载器。这里的父子关系一般通过组合关系（Composition）来实现，而不是继承关系（Inheritance）。

最高级为启动类加载器，其次是扩展类加载器，再是应用程序类加载器，最后为用户自定义类加载器。

### 1. 工作过程 

一个类加载器首先将类加载请求转发到父类加载器，只有当父类加载器无法完成时才尝试自己加载。

### 2.好处

使得 Java 类随着它的类加载器一起具有一种带有优先级的层次关系，从而使得基础类得到统一。保证程序中某个类不会出现多种来源。

### 3. 实现 

以下是抽象类 java.lang.ClassLoader 的代码片段，其中的 loadClass() 方法运行过程如下：先检查类是否已经加载过，如果没有则让父类加载器去加载。当父类加载器加载失败时抛出 ClassNotFoundException，此时尝试自己去加载。

```java
public abstract class ClassLoader {
    // The parent class loader for delegation
    private final ClassLoader parent;

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    c = findClass(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }
}
```

## 自定义类加载器实现

以下代码中的 FileSystemClassLoader 是自定义类加载器，继承自 java.lang.ClassLoader，用于加载文件系统上的类。它首先根据类的全名在文件系统上查找类的字节代码文件（.class 文件），然后读取该文件内容，最后通过 defineClass() 方法来把这些字节代码转换成 java.lang.Class 类的实例。 

java.lang.ClassLoader 的 loadClass() 实现了双亲委派模型的逻辑，自定义类加载器一般不去重写它，但是需要重写 findClass() 方法。

```java
public class FileSystemClassLoader extends ClassLoader {

    private String rootDir;

    public FileSystemClassLoader(String rootDir) {
        this.rootDir = rootDir;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classData = getClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            return defineClass(name, classData, 0, classData.length);
        }
    }

    private byte[] getClassData(String className) {
        String path = classNameToPath(className);
        try {
            InputStream ins = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesNumRead;
            while ((bytesNumRead = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesNumRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String classNameToPath(String className) {
        return rootDir + File.separatorChar
                + className.replace('.', File.separatorChar) + ".class";
    }
}
```

