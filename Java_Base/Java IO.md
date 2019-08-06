# 一、Java IO概览

java内的 IO 大概可以分成以下几类：

- 磁盘操作：File
- 字节操作：InputStream和OutputStream
- 字符操作：Reader和Writer
- 对象操作：Serializable
- 网络操作：Socket
- 新的输入/输出：NIO

# 二、磁盘操作

File类可以用于表示文件和目录的信息，但是它不能显示文件的内容。

递归地列出一个目录下所有文件：

```java
public static void listAllFiles(File dir) {
    if (dir == null || !dir.exists()) {
        return;
    }
    //通过判断当前内容是文件（File）还是文件夹（Directory）选择打印名称还是递归查找
    if (dir.isFile()) {
        System.out.println(dir.getName());
        return;
    }
    for (File file : dir.listFiles()) {
        listAllFiles(file);
    }
}
```

从 Java7 开始，可以使用 Paths 和 Files 代替 File。

# 三、字节操作

## 3.1 实现文件复制

```java
public static void copyFile(String src, String dist) throws IOException {
    FileInputStream in = new FileInputStream(src);
    FileOutputStream out = new FileOutputStream(dist);

    byte[] buffer = new byte[20 * 1024];
    int cnt;

    // read() 最多读取 buffer.length 个字节
    // 返回的是实际读取的个数
    // 返回 -1 的时候表示读到 eof，即文件尾
    while ((cnt = in.read(buffer, 0, buffer.length)) != -1) {
        out.write(buffer, 0, cnt);
    }

    in.close();
    out.close();
}
```

## 3.2 字节操作中的设计模式——装饰者模式

Java I/O 使用了装饰者模式来实现。以 InputStream 为例，

- InputStream 是抽象组件；
- FileInputStream 是 InputStream 的子类，属于具体组件，提供了字节流的输入操作；
- FilterInputStream 属于抽象装饰者，**装饰者用于装饰组件，为组件提供额外的功能**。例如 BufferedInputStream 为 FileInputStream 提供缓存的功能。

实例化一个具有缓存功能的字节流对象时，只需要在 FileInputStream 对象上再套一层 BufferedInputStream 对象即可。

```java
FileInputStream fileInputStream = new FileInputStream(filePath);
BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
```

# 四、字符操作

## 4.1 编码与解码

编码就是把字符转换为字节（因为在很多传输过程中，都是以字节流进行数据传输，所以需要使用编码转换），同理，解码就是把字节重新组合成字符。

如果编码和解码过程中使用了不同的编码方式，那么就出现了乱码。

- GBK编码中，中文字符占2个字节，英文字符占一个字节；
- UTF-8 编码中，中文字符占3个字节，英文字符占一个字节；
- UTF-16be 编码中，中文字符和英文字符都占 2 个字节。

UTF-16be 中的 be 指的是 Big Endian，也就是大端。相应地也有 UTF-16le，le 指的是 Little Endian，也就是小端。

Java 的内存编码使用双字节编码 UTF-16be，这不是指 Java 只支持这一种编码方式，而是说 char 这种类型使用 UTF-16be 进行编码。char 类型占 16 位，也就是两个字节，Java 使用这种双字节编码是为了让一个中文或者一个英文都能使用一个 char 来存储。

### String 的编码方式

String 可以看成一个字符序列，可以指定一个编码方式将它编码为字节序列，也可以指定一个编码方式将一个字节序列解码为 String。

```java
String str1 = "中文";
byte[] bytes = str1.getBytes("UTF-8");
String str2 = new String(bytes, "UTF-8");
System.out.println(str2);
```

在调用无参数 getBytes() 方法时，默认的编码方式不是 UTF-16be。双字节编码的好处是可以使用一个 char 存储中文和英文，而将 String 转为 bytes[] 字节数组就不再需要这个好处，因此也就不再需要双字节编码。getBytes() 的默认编码方式与平台有关，一般为 UTF-8。

```java
byte[] bytes = str1.getBytes();
```

## 4.2 Reader 与 Writer

我们知道，不管是磁盘还是网络传输，最小的存储单元都是字节而不是字符。但是对于我们来说我们更愿意操作能够看懂理解的字符形式的数据而非字节数据，因此需要提供对字符进行操作的方法。

- InputStreamReader 实现从字节流解码成字符流
- OutputStreamReader 实现从字符流编码成字节流

### 实现逐行输出文本文件的内容

```java
public static void readFileContent(String filePath) throws IOException {

    FileReader fileReader = new FileReader(filePath);
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    String line;
    while ((line = bufferedReader.readLine()) != null) {
        System.out.println(line);
    }

    // 装饰者模式使得 BufferedReader 组合了一个 Reader 对象
    // 在调用 BufferedReader 的 close() 方法时会去调用 Reader 的 close() 方法
    // 因此只要一个 close() 调用即可
    bufferedReader.close();
}
```

# 五、对象操作

## 5.1 序列化

序列化就是将一个对象转换成字节序列，方便存储和传输

- 序列化：ObjectOutPutStream.writeObject()
- 反序列化：ObjectInputStream.readObject()

不会对静态变量进行序列化，因为序列化只是保存对象的状态，静态变量属于类的状态。

## 5.2 Serializable

序列化的类需要实现 Serializable 接口，Serializable只是一个标准（源码中该接口无任何方法需要实现），但是如果需要序列化的类没有实现它就无法进行序列化。

```java
public static void main(String[] args) throws IOException, ClassNotFoundException {
    A a1 = new A(123, "abc");
    String objectFile = "a1";

    //把a1写进文件目录下的a1文件中
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(objectFile));
    objectOutputStream.writeObject(a1);
    objectOutputStream.close();

    //读取a1文件
    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(objectFile));
    A a2 = (A) objectInputStream.readObject();
    objectInputStream.close();
    System.out.println(a2);
}

//实现了接口Serializable
private static class A implements Serializable {
    private int x;
    private String y;

    A(int x, String y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "x = " + x + "  " + "y = " + y;
    }
}
```

```java
//输出结果
x = 123  y = abc
```

## 5.3 transient

transient 关键字可以使一些属性不会被序列化。

ArrayList 中存储数据的数组 elementData 是用 transient 修饰的，因为这个数组是动态扩展的，并不是所有的空间都被使用，因此就不需要所有的内容都被序列化。通过重写序列化和反序列化方法，使得可以只序列化数组中有内容的那部分数据。

```java
private transient Object[] elementData;
```

 如果上述A类中我们将x和y分别以transient修饰

```java
private transient int x;
private transient String y;
```

那么输出结果将会是系统默认值

```java
x = 0  y = null
```

# 六、网络操作

java中的网络支持

- InetAdress：用于表示网络上的硬件资源，即IP地址
- URL：统一资源定位符
- Sockets：使用TCP协议实现网络通信；
- Datagram：使用UDP协议实现网络通信。

