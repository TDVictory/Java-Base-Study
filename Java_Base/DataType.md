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

