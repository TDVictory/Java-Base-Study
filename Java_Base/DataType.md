# 基本类型
- byte/8 bit
- char/16 bit
- short/16 bit
- int/32 bit
- float/32 bit
- long/64 bit
- double/64 bit
- boolean/无确切规定

boolean（布尔值）只有两个值：true、false，可以采用1bit来存储，但是没有明确规定其具体大小。JVM会在编译时期将boolean类型的数据转换为int，使用1代表true，0代表false。JVM并不支持boolean数组，而是使用byte数组来表示。
# 包装类型
java中的数据类型int，double等不是对象，无法通过向上转型获取到Object提供的方法，导致无法参与转型，泛型，反射等过程。为了弥补这个缺陷，java提供了包装类。

基本类型都有对应的包装类型，基本类型与其对应的包装类型之间的赋值使用自动装箱与拆箱完成。
```java
Integer x = 2;  //自动将基本类int转化为Integer装箱。
int y = x;      //自动将包装类Integer转化为int拆箱。
```
# 缓存池
new Integer(123)与Integer.valueOf(123)的区别在于：
- new Integer(123)每次都会新建一个对象；
- Integer.valueOf(123)会使用缓存池中的对象，多次调用会取得同一个对象的引用。
```java
Integer x = new Integer(123);
Integer y = new Integer(123);
System.out.println(x == y);    // false，因为此时x和y两个类的内存地址不同（非基本数据类型中“==”比较地址）。
Integer z = Integer.valueOf(123);
Integer k = Integer.valueOf(123);
System.out.println(z == k);   // true，此时z和k均指向缓存池中“123”地址，此时地址值相同。
```
valueOf()方法的实现比较简单，就是先判断值是否在缓存池中，如果在的话就直接返回缓存池的内容。
```java
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```
编译器会在自动装箱的过程中调用valueOf（）方法，因此多个Integer实例使用自动装箱来创建并且具有相同值时，那么就会引用相同的对象。
```java
Integer m = 123;
Integer n = 123;
System.out.println(m == n); // true,二者均指向缓存池中的“123”地址
```
在 Java 8 中，Integer 缓存池的大小默认为 -128~127。

Integer i = new Integer(xxx)和Integer i =xxx;这两种方式的区别。
- 第一种方式不会触发自动装箱的过程；而第二种方式会触发；
- 在执行效率和资源占用上的区别。当值不在（-128-127）之间时（注意这并不是绝对的），第二种方式的执行效率和资源占用在一般性情况下要优于第一种情况。
# 二、String
## 概览
String被声明为final，因此它不可被继承

在Java8中，String内部使用char数组存储数据
```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];
}
```
在Java9之后，String类的实现改用byte数组存储字符串，同时使用coder来标识使用了哪种编码。
```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final byte[] value;

    /** The identifier of the encoding used to encode the bytes in {@code value}. */
    private final byte coder;
}
```
## 不可变的好处
### 1.可以缓存hash值
因为String的hash值经常被使用，例如String用做HashMap的Key。不可变的特性可以使得hash值也不可辨，因此只需要进行一次计算。
### 2.String Pool的需要
如果一个String对象已经被创建过了，那么就会从String Pool中取得引用。只有String是不可变的，才可能使用String Pool。
### 3.安全性
String 经常作为参数，String 不可变性可以保证参数不可变。例如在作为网络连接参数的情况下如果 String 是可变的，那么在网络连接过程中，String 被改变，改变 String 对象的那一方以为现在连接的是其它主机，而实际情况却不一定是。
### 4.线程安全
String 不可变性天生具备线程安全，可以在多个线程中安全地使用。

## String，StringBuffer and StringBuilder
### 1.可变性
- String 不可变
- StringBuffer和StringBuilder可变
### 2.线程安全
- String 不可变，因此是线程安全的
- StringBuilder 不是线程安全的
- StringBuffer 是线程安全的，内部使用synchronized进行同步
## String Pool
字符串常量池（String Pool）保存着所有字符串字面量（literal strings），这些字面量再编译时期就确定，不仅如此，还可以使用String的intern()方法再运行过程中将字符串添加到String Pool中。

当一个字符串调用intern()方法时，如果String Pool中已经存在一个字符串和该字符串值相等（使用equals()方法进行确定），那么就会返回String Pool中的字符串的引用；否则，就会在String Pool中添加一个新的字符串，并返回这个新字符串的引用。

下面示例中，s1和s2采用new String()的方式新建了两个不用字符串，而s3和s4是通过s1.intern()方法取得一个字符串引用。intern()首先把s1引用的字符串放到String Pool中，然后返回这个字符串引用。因此s3和s4引用的是同一个字符串。
```java
String s1 = new String("aaa");
String s2 = new String("aaa");
System.out.println(s1 == s2);           // false，此时s1和s2分别为两个类，存放在堆中。
String s3 = s1.intern();                // 此时常量池中没有“aaa”，所以调用intern方法时会在常量池中创建一个“aaa”，并返回该值引用
String s4 = s1.intern();                // 此时常量池中包含“aaa”，所以直接获取常量池中“aaa”的引用
System.out.println(s3 == s4);           // true，因为s3和s4均指向常量池中的“aaa”
···
如果采用“aaa”这种字面量的形式创建字符串，会自动地将字符串放入String Pool中
```java
String s5 = "aaa";
String s6 = "aaa";
System.out.println(s5 == s6);  // true
```
## new String("abc")
使用这种方式一共会创建两个字符串对象（如果此时String Pool中不存在“abc”字符串对象）。
- “abc”属于字符串字面量，因此编译时期会在String Pool中创建一个字符串对象，指向这个“abc”字符串字面量；
- 而使用new的方式会在堆中创建一个字符串对象。
创建一个测试类，其main方法中使用这种方式来创建字符串对象。
```java
public class NewStringTest {
    public static void main(String[] args) {
        String s = new String("abc");
    }
}
```
使用javap -verbose进行反编译，得到以下内容
```java
// ...
Constant pool:
// ...
   #2 = Class              #18            // java/lang/String
   #3 = String             #19            // abc
// ...
  #18 = Utf8               java/lang/String
  #19 = Utf8               abc
// ...

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=2, args_size=1
         0: new           #2                  // class java/lang/String
         3: dup
         4: ldc           #3                  // String abc
         6: invokespecial #4                  // Method java/lang/String."<init>":(Ljava/lang/String;)V
         9: astore_1
// ...
```
在Constant Pool（常量池）中，#19存储着字符串字面量“abc”，#3是String Pool的字符串对象，它指向#19这个字符串字面量。在main方法中，0: 行使用 new #2 在堆中创建一个字符串对象，并且使用 ldc #3 将 String Pool 中的字符串对象作为 String 构造函数的参数。

以下是 String 构造函数的源码，可以看到，在将一个字符串对象作为另一个字符串对象的构造函数参数时，并不会完全复制 value 数组内容，而是都会指向同一个 value 数组。
```java
public String(String original) {
    this.value = original.value;
    this.hash = original.hash;
}
```
# 三、运算
## 参数传递
java的参数是以值传递的形式传入方法中，而不是引用传递。

以下代码中Dog dog的dog是一个指针，存储的是对象的地址。在将一个参数传入一个方法时，本质上时将对象的地址以值的方式传递到形参中。因此在方法中使用指针引用其他对象，那么这两个指针此时指向的是完全不同的对象，在一方改变其所指向对象的内容时对另一方没有影响。
```java
public class Dog {

    String name;

    Dog(String name) {
        this.name = name;
    }

    String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getObjectAddress() {
        return super.toString();
    }
}
```
```java
public class PassByValueExample {
    public static void main(String[] args) {
        Dog dog = new Dog("A");
        System.out.println(dog.getObjectAddress()); // Dog@4554617c
        func(dog);
        System.out.println(dog.getObjectAddress()); // Dog@4554617c
        System.out.println(dog.getName());          // A
    }

    private static void func(Dog dog) {
        System.out.println(dog.getObjectAddress()); // Dog@4554617c
        dog = new Dog("B");
        System.out.println(dog.getObjectAddress()); // Dog@74a14482
        System.out.println(dog.getName());          // B
    }
}
```
如果在方法中改变对象的字段值会改变原对象该字段值，因为改变的是同一个地址指向的内容。
```java
class PassByValueExample {
    public static void main(String[] args) {
        Dog dog = new Dog("A");
        func(dog);
        System.out.println(dog.getName());          // B
    }

    private static void func(Dog dog) {
        dog.setName("B");
    }
}
```
## float与double
java不能隐式执行向下转型，因为这会使得精度降低。
1.1字面量属于double类型，不能直接将1.1赋值给float变量，因为这是向下转型。
```java
//float f = 1.1;
```
1.1f字面量才是float类型。
```java
float f = 1.1f;
```
## 隐式类型转换
因为字面量1是int类型，它比short类型精度要高，因此不能隐式地将int类型向下转型为short类型。
```java
short s1 = 1;
// s1 = s1 + 1;
```
但是使用+=或者++运算符可以执行隐式类型转换。
```java
s1 += 1;
s1++;
```
上面的语句相当于将s1+1的计算结果进行了向下转型：
```java
s1 = (short)(s1+1);
```
## switch
从Java7开始，可以在switch条件判断语句中使用String对象。
```java
String s = "a";
switch (s) {
    case "a":
        System.out.println("aaa");
        break;
    case "b":
        System.out.println("bbb");
        break;
}
```
switch不支持long，是因为switch的设计初衷是对那些只有少数的几个值进行等值判断，如果值过于复杂，那么还是用if比较合适。
```java
// long x = 111;
// switch (x) { // Incompatible types. Found: 'long', required: 'char, byte, short, int, Character, Byte, Short, Integer, String, or an enum'
//     case 111:
//         System.out.println(111);
//         break;
//     case 222:
//         System.out.println(222);
//         break;
// }
```
# 四、继承
## 访问权限
Java中有三个访问权限修饰符：private、protected以及public，如果不加访问修饰符，表示包级可见。

可以对类或类中的成员（字段以及方法）加上访问修饰符。

- 类可见表示其他类可以用这个类创建实例对象。
- 成员可见表示其他类可以用这个类的实例对象访问到该成员；

protected用于修饰成员，表示在继承体系中成员对于子类可见，但是这个访问修饰符对于类没有意义。

设计良好的模块会隐藏所有的实现细节，把它的API与它的实现清晰地隔离开来。模块之间只通过它们的API进行通信，一个模块不需要知道其他模块的内部工作情况，这个概念被称为信息的隐藏或封装。因此访问权限应当尽可能地使每个类或者成员不被外界访问。

如果子类的方法重写的父类的方法，那么子类的访问级别不允许低于父类的访问级别。这是为了确保可以只用父类示例的地方都可以使用子类实例，也就是确保满足里氏替换原则。

字段绝不能是公有的，因为这么做的话就失去了对这个字段修改行为的控制，客户端可以对其随意修改。例如下面的例子中AccessExample拥有id公有字段，如果在某个时刻，我们想要使用int存储id字段，那么就需要修改所有的客户端代码。
```java
public class AccessExample {
    public String id;
}
```
可以使用公有的getter和setter方法来替换公有字段，这样的话就可以控制对字段的修改行为。

```java
public class AccessExample {

    private int id;

    public String getId() {
        return id + "";
    }

    public void setId(String id) {
        this.id = Integer.valueOf(id);
    }
}
```
但是也有例外，如果是包级私有的类或者私有的嵌套类，那么直接暴露成员不会有特别大的影响。
```java
public class AccessWithInnerClassExample {

    private class InnerClass {
        int x;
    }

    private InnerClass innerClass;

    public AccessWithInnerClassExample() {
        innerClass = new InnerClass();
    }

    public int getValue() {
        return innerClass.x;  // 直接访问
    }
}
```
## 抽象类与接口
### 1.抽象类
抽象类和抽象方法都使用abstract关键字进行声明。抽象类一般会包含抽象方法，抽象方法一定位于抽象类中。

抽象类和普通类最大的区别使，抽象类不能被实例化，需要继承抽象类才能实例化其子类。
```java
public abstract class AbstractClassExample {

    protected int x;
    private int y;

    public abstract void func1();

    public void func2() {
        System.out.println("func2");
    }
}
```
```java
public class AbstractExtendClassExample extends AbstractClassExample {
    @Override
    public void func1() {
        System.out.println("func1");
    }
}
```
```java
// AbstractClassExample ac1 = new AbstractClassExample(); // 'AbstractClassExample' is abstract; cannot be instantiated
AbstractClassExample ac2 = new AbstractExtendClassExample();
ac2.func1();
```
### 2.接口
接口是抽象类的延伸，在 Java 8 之前，它可以看成是一个完全抽象的类，也就是说它不能有任何的方法实现。

从 Java 8 开始，接口也可以拥有默认的方法实现，这是因为不支持默认方法的接口的维护成本太高了。在 Java 8 之前，如果一个接口想要添加新的方法，那么要修改所有实现了该接口的类。

接口的成员（字段 + 方法）默认都是 public 的，并且不允许定义为 private 或者 protected。

接口的字段默认都是 static 和 final 的。
```java
public interface InterfaceExample {

    void func1();

    default void func2(){
        System.out.println("func2");
    }

    int x = 123;
    // int y;               // “y”可能没有被初始化
    public int z = 0;       
    // private int k = 0;   // 不允许在此处使用private修饰词
    // protected int l = 0; // 不允许在此处使用private修饰词
    // private void fun3(); // 不允许在此处使用private修饰词
}
```

