# 一、概览
容器主要包括Collection和Map两种，Collection存储着对象的集合，而Map存储着键值对（两个对象）的映射表。

## Collection
### 1.Set
- TreeSet：基于红黑树实现，支持有序性操作，例如根据一个范围查找元素的操作。但是查找效率不如HashSet，HashSet查找的时间复杂度为O（1），TreeSet则为O（log）。
- HashSet：基于哈希表实现，支持快速查找，但不支持有序性操作。并且失去了元素的插入顺序信息，也就是说通过Iterator遍历HashSet得到的结果是不确定的。
- LinkedHashSet：具有HashSet的查找效率，且内部使用双向链表维护元素的插入顺序。
### 2.List
- ArrayList：基于动态数组实现，支持随机访问。
- Vector：和ArrayList类似，但它是线程安全的。
- LinkedList：基于双向链表实现，只能顺序访问，但是可以快速地在链表中间插入和删除元素。不仅如此，LinkedList还可以用作栈、队列和双向队列。
### 3.Queue
- LinkedList：可以用它来实现双向队列。
- PriorityQueue：基于堆结构实现，可以用它来实现优先队列。

## Map
- TreeMap：基于红黑树实现。
- HashMap：基于哈希表实现。
- HashTable：和HashMap类似，但它是线程安全的。但它也是遗留类，我们现在使用ConcurrentHashMap来支持线程安全。ConcurrentHashMap效率更高，因为它引入了分段锁。
- LinkedHashMap：使用双向链表来维护元素的顺序，顺序为插入顺序或者最近最少使用顺序。

# 二、容器中的设计模式
## 迭代器模式
Collection继承了Iterable接口，其中的iterator()方法能够产生一个Iterator()对象，通过这个对象就可以迭代遍历Collection中的元素。

从JDK1.5之后使用foreach方法来遍历实现了Iterable接口的聚合对象。

## 适配器模式
java.util.Arrays#asList() 可以把数组类型转换为 List 类型。
```
@SafeVarargs
public static <T> List<T> asList(T... a)
```
asList()的参数为泛型的变长参数，不能使用基本类型数组作为参数，只能使用相应的包装类型数组。
```
Integer[] arr = {1, 2, 3};
List list = Arrays.asList(arr);
```
也可以使用以下方式调用asList()
```
List list = Arrays.asList(1, 2, 3);
```

# 三、源码分析
如果没有特别说明，以下源码分析基于JDK1.8.
## ArrayList
### 1.概览
因为ArrayList是基于数组实现的，所以支持快速随机访问。RandomAccess接口标识着该类支持快速随机访问。
```
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```
数组的默认大小为10.
```
private static final int DEFAULT_CAPACITY = 10;
```
### 2.扩容
添加元素时使用 ensureCapacityInternal() 方法来保证容量足够，如果不够时，需要使用 grow() 方法进行扩容，新容量的大小为 oldCapacity + (oldCapacity >> 1)，也就是旧容量的 1.5 倍。

扩容操作需要调用 Arrays.copyOf() 把原数组整个复制到新数组中，这个操作代价很高，因此最好在创建 ArrayList 对象时就指定大概的容量大小，减少扩容操作的次数。

```
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}

private void ensureCapacityInternal(int minCapacity) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    ensureExplicitCapacity(minCapacity);
}

private void ensureExplicitCapacity(int minCapacity) {
    modCount++;
    // overflow-conscious code
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}

private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

### 3.删除元素
需要调用 System.arraycopy() 将 index+1 后面的元素都复制到 index 位置上，该操作的时间复杂度为 O(N)，可以看出 ArrayList 删除元素的代价是非常高的。
```
public E remove(int index) {
    rangeCheck(index);
    modCount++;
    E oldValue = elementData(index);
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    elementData[--size] = null; // clear to let GC do its work
    return oldValue;
}
```
### 4. Fail-Fast
modCount 用来记录 ArrayList 结构发生变化的次数。结构发生变化是指添加或者删除至少一个元素的所有操作，或者是调整内部数组的大小，仅仅只是设置元素的值不算结构发生变化。

在进行序列化或者迭代等操作时，需要比较操作前后 modCount 是否改变，如果改变了需要抛出 ConcurrentModificationException。

### 5. 序列化
ArrayList 基于数组实现，并且具有动态扩容特性，因此保存元素的数组不一定都会被使用，那么就没必要全部进行序列化。

保存元素的数组 elementData 使用 transient 修饰，该关键字声明数组默认不会被序列化

## Vector
### 1.同步
它的实现与ArrayList类似，但是使用了synchronized进行同步。
```
public synchronized boolean add(E e) {
    modCount++;
    ensureCapacityHelper(elementCount + 1);
    elementData[elementCount++] = e;
    return true;
}

public synchronized E get(int index) {
    if (index >= elementCount)
        throw new ArrayIndexOutOfBoundsException(index);

    return elementData(index);
}
```
### 2.与ArrayList的比较
- Vector 是同步的，因此开销就比 ArrayList 要大，访问速度更慢。最好使用 ArrayList 而不是 Vector，因为同步操作完全可以由程序员自己来控制
- Vector 每次扩容请求其大小的 2 倍空间，而 ArrayList 是 1.5 倍。

### 3.替代方案
可以使用Collections.synchronizedList(); 得到一个线程安全的 ArrayList。
```
List<String> list = new ArrayList<>();
List<String> synList = Collections.synchronizedList(list);
```
也可以使用 concurrent 并发包下的 CopyOnWriteArrayList 类。
```
List<String> list = new CopyOnWriteArrayList<>();
```

## CopyOnWriteArrayList
### 读写分离
写操作在一个复制的数组上进行，读操作还是在原始数组中进行，读写分离，互不影响。

写操作需要加锁，防止并发写入时导致写入数据丢失。

写操作结束之后需要把原始数组指向新的复制数组。

### 适用场景
CopyOnWriteArrayList在写操作的同时允许读操作，大大提高了读操作的性能，因此很适合读多写少的应用场景。
但是CopyOnWriteArrayList也有其缺陷：
- 内存占用：在写操作时需要复制一个新的数组，使得内存占用为原来的两倍左右。
- 数据不一致：读操作不能读取实时性的数据，因为部分写操作的数据还未同步到读数组中。
所以CopyOnWriteArrayList不适用于内存敏感以及对实时性要求很高的场景。

## LinkedList
### 1.概览
基于双向链表实现，使用Node存储链表节点信息。
```
private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;
}
```
### 2.与ArrayList的比较
- ArrayList基于动态数组实现，LinkedList基于双向链表实现
- ArrayList支持随机访问，LinkedList不支持
LinkedList在任意位置添加删除元素更快，ArrayList在查找任意元素更快
 
## HashMap
以下源码为JDK1.7
### 1.存储结构
内部包含了一个Entry类型的数组table
```
transient Entry[] table;
```
 Entry存储着键值对，它包含了四个字段，从next字段我们可以看出Entry是一个链表。即数组中的每个位置被当成一个桶，一个桶存放一个链表。HashMap使用拉链法来解决冲突，同一个链表中存放哈希值和散列桶取模运算结果相同的Entry。

```
static class Entry<K,V> implements Map.Entry<K,V> {
    final K key;
    V value;
    Entry<K,V> next;
    int hash;

    Entry(int h, K k, V v, Entry<K,V> n) {
        value = v;
        next = n;
        key = k;
        hash = h;
    }

    public final K getKey() {
        return key;
    }

    public final V getValue() {
        return value;
    }

    public final V setValue(V newValue) {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }

    public final boolean equals(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry e = (Map.Entry)o;
        Object k1 = getKey();
        Object k2 = e.getKey();
        if (k1 == k2 || (k1 != null && k1.equals(k2))) {
            Object v1 = getValue();
            Object v2 = e.getValue();
            if (v1 == v2 || (v1 != null && v1.equals(v2)))
                return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }

    public final String toString() {
        return getKey() + "=" + getValue();
    }
}
```
### 2.拉链法的工作原理
```
HashMap<String, String> map = new HashMap<>();
map.put("K1", "V1");
map.put("K2", "V2");
map.put("K3", "V3");
```
- 新建一个HashMap，默认大小为16；
- 插入<K1，V1>键值对，先计算K1的hashCode为115，使用除留余数法得到所在的桶下标115 % 16 = 3。
- 插入 <K2,V2> 键值对，先计算 K2 的 hashCode 为 118，使用除留余数法得到所在的桶下标 118 % 16 = 6。
- 插入 <K3,V3> 键值对，先计算 K3 的 hashCode 为 118，使用除留余数法得到所在的桶下标 118 % 16 = 6，插在 <K2,V2> 前面。

应该注意到链表的插入是以头插法方式进行的，例如上面的 <K3,V3> 不是插在 <K2,V2> 后面，而是插入在链表头部。

查找需要分成两步进行：

- 计算键值对所在的桶；
- 在链表上顺序查找，时间复杂度显然和链表的长度成正比。

### 3. put 操作
```
public V put(K key, V value) {
    if (table == EMPTY_TABLE) {
        inflateTable(threshold);
    }
    // 键为 null 单独处理
    if (key == null)
        return putForNullKey(value);
    int hash = hash(key);
    // 确定桶下标
    int i = indexFor(hash, table.length);
    // 先找出是否已经存在键为 key 的键值对，如果存在的话就更新这个键值对的值为 value
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }

    modCount++;
    // 插入新键值对
    addEntry(hash, key, value, i);
    return null;
}
```
HashMap 允许插入键为 null 的键值对。但是因为无法调用 null 的 hashCode() 方法，也就无法确定该键值对的桶下标，只能通过强制指定一个桶下标来存放。HashMap 使用第 0 个桶存放键为 null 的键值对。
```
private V putForNullKey(V value) {
    for (Entry<K,V> e = table[0]; e != null; e = e.next) {
        if (e.key == null) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
    modCount++;
    addEntry(0, null, value, 0);
    return null;
}
```
使用链表的头插法，也就是新的键值对插在链表的头部，而不是链表的尾部。
```
void addEntry(int hash, K key, V value, int bucketIndex) {
    if ((size >= threshold) && (null != table[bucketIndex])) {
        resize(2 * table.length);
        hash = (null != key) ? hash(key) : 0;
        bucketIndex = indexFor(hash, table.length);
    }

    createEntry(hash, key, value, bucketIndex);
}

void createEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K,V> e = table[bucketIndex];
    // 头插法，链表头部指向新的键值对
    table[bucketIndex] = new Entry<>(hash, key, value, e);
    size++;
}
```
```
Entry(int h, K k, V v, Entry<K,V> n) {
    value = v;
    next = n;
    key = k;
    hash = h;
}
```
### 4. 确定桶下标
很多操作都需要先确定一个键值对所在的桶下标。
```
int hash = hash(key);
int i = indexFor(hash, table.length);
```
#### 4.1 计算 hash 值
```
final int hash(Object k) {
    int h = hashSeed;
    if (0 != h && k instanceof String) {
        return sun.misc.Hashing.stringHash32((String) k);
    }

    h ^= k.hashCode();

    // This function ensures that hashCodes that differ only by
    // constant multiples at each bit position have a bounded
    // number of collisions (approximately 8 at default load factor).
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
```
```
public final int hashCode() {
    return Objects.hashCode(key) ^ Objects.hashCode(value);
}
```
#### 4.2 取模
令 x = 1<<4，即 x 为 2 的 4 次方，它具有以下性质：
```
x   : 00010000
x-1 : 00001111
```
令一个数 y 与 x-1 做与运算，可以去除 y 位级表示的第 4 位以上数：
```
y       : 10110010
x-1     : 00001111
y&(x-1) : 00000010
```
这个性质和 y 对 x 取模效果是一样的：
```
y   : 10110010
x   : 00010000
y%x : 00000010
```
我们知道，位运算的代价比求模运算小的多，因此在进行这种计算时用位运算的话能带来更高的性能。

确定桶下标的最后一步是将 key 的 hash 值对桶个数取模：hash%capacity，如果能保证 capacity 为 2 的 n 次方，那么就可以将这个操作转换为位运算。
```
static int indexFor(int h, int length) {
    return h & (length-1);
}
```
### 5.扩容-基本原理
我们可以知道，当我们需要寻找一个长度为M，存储键值对数量为N的HashMap里的一个数时候，我们首先需要知道其桶下标，然后在桶下标下面找到该数。也就是说如果哈希函数满足均匀性的要求时，每个桶下标下的链表长度为N/M，因此平均查找次数的复杂度为O(N/M)。

为了查找的足够快，我们需要N/M足够小，因此我们需要保证M尽可能大，也即所说的Table要尽可能大。HashMap会采用动态扩容来根据当前的N值来调整M值，使得空间效率和时间效率都能得到保证。

和扩容相关的残烛主要有：capacity、size、threshold和load_dactor。

| 参数 | 含义 |
| :--: | :-- |
| capacity | table的容量大小，默认16.需要注意的是capacity必须保证为2的n次方，我们在4.2里面学到的取模需要2的n次方。 |
| size | 键值对数量 |
| threshold | size的临界值，当size大于等于threhold就必须进行扩容操作 |
| loadFactor | 装载因子，table能够使用的比例，threshold = capacity * loadFactor。 |
```
static final int DEFAULT_INITIAL_CAPACITY = 16;

static final int MAXIMUM_CAPACITY = 1 << 30;

static final float DEFAULT_LOAD_FACTOR = 0.75f;

transient Entry[] table;

transient int size;

int threshold;

final float loadFactor;

transient int modCount;
```
从下面的添加Entry代码中可以看出，一旦触发扩容（size大于等于threshold时）
```
void addEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K,V> e = table[bucketIndex];
    table[bucketIndex] = new Entry<>(hash, key, value, e);
    if (size++ >= threshold)
        resize(2 * table.length);
}
```
扩容使用 resize() 实现，需要注意的是，扩容操作同样需要把 oldTable 的所有键值对重新插入 newTable 中，因此这一步是很费时的。
```
void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
        threshold = Integer.MAX_VALUE;
        return;
    }
    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable);
    table = newTable;
    threshold = (int)(newCapacity * loadFactor);
}
```
transfer则是将原先table中的所有内容复制到新的table中，并将原table清空。
```
void transfer(Entry[] newTable) {
    Entry[] src = table;
    int newCapacity = newTable.length;
    for (int j = 0; j < src.length; j++) {
        Entry<K,V> e = src[j];
        if (e != null) {
            src[j] = null;
            do {
                Entry<K,V> next = e.next;
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            } while (e != null);
        }
    }
}
```
### 6. 扩容-重新计算桶下标
在进行扩容时，需要把键值对重新放到对应的桶上。HashMap 使用了一个特殊的机制，可以降低重新计算桶下标的操作。

假设原数组长度 capacity 为 16，扩容之后 new capacity 为 32：
```
capacity     : 00010000
new capacity : 00100000
```
即我们在进行取模的时候会需要多判定一位，那也即我们可以只判断多出来的那一位即可：

对于一个Key（16扩容到32）
- 它的哈希值如果在第 5 位上为 0，那么取模得到的结果和之前一样；
- 如果为 1，那么得到的结果为原来的结果 +16。
### 7. 计算数组容量
HashMap的构造函数允许我们传入不是2的n次方的容量设置。但是我们之前说过了，在确定桶下标的时候由于取模是位运算，非2的n次方是无法使用这种方法的，所以HashMap就后台偷偷地将我们传入的容量转换成了2的n次方。

对于任何一个输入的数，首先将其转换成二进制。假设输入参数为10010000，我们看看它是怎么变换的：
```
mask |= mask >> 1    11011000
mask |= mask >> 2    11111110
mask |= mask >> 4    11111111
mask += 1           100000000
```
我们可以看到HashMap通过每次对输入值或运算它的右移2的n-1次位，每次保证前2的n次位均为1.最后将输入参数的所有位都填为1，再加一就可以得到新的容量了。

下面是HashMap中计算数组容量的代码：
```
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```
在java中，int是32位，所以这里最多只需要右移16位就可以全部覆盖了=。=

### 8.链表转红黑树
从JDK1.8开始，一个桶存储的链表长度大于8时会将链表转换位红黑树。

### 9.与HashTable的比较
- HashTable使用synchronized来进行同步，所以相对于HashTable来说HashMap是线程不安全的。
- HashTable不可以插入键位null的Entry。
- HashMap的迭代器是fail-fast迭代器。（因为HashMap是线程不安全的，在线程1读取内容时，线程2改变了HashMap的结构，fail-fast迭代器会抛出ConcurrentModificationException的异常）
- HashMap 不能保证随着时间的推移 Map 中的元素次序是不变的。

## ConcurrentHashMap
我们知道因为HashMap因为是线程不安全的，所以在读写速度上超过HashTable。但是如果需要保证线程安全，Java5以后提供了ConcurrentHashMap这个结构，此后HashTable就淡出了我们的视线。

### 1 JDK1.7底层实现
在JDK1.7中，ConcurrentHashMap通过“锁分段”来实现线程安全。ConcurrentHashMap将哈希表分成许多片段（segments），每一个片段（table）都类似于HashMap，它有一个HashEntry数组，数组的每项又是HashEntry组成的链表。每个片段都是Segment类型的，Segment继承了ReentrantLock，所以Segment本质上是一个可重入的互斥锁。这样每个片段都有了一个锁，这就是“锁分段”。线程如想访问某一key-value键值对，需要先获取键值对所在的segment的锁，获取锁后，其他线程就不能访问此segment了，但可以访问其他的segment。
```
static final class HashEntry<K,V> {
    final int hash;
    final K key;
    volatile V value;
    volatile HashEntry<K,V> next;
}
```
ConcurrentHashMap 和 HashMap 实现上类似，最主要的差别是 ConcurrentHashMap 采用了分段锁（Segment），每个分段锁维护着几个桶（HashEntry），多个线程可以同时访问不同分段锁上的桶，从而使其并发度更高（并发度就是 Segment 的个数）。

Segment 继承自 ReentrantLock。
```
static final class Segment<K,V> extends ReentrantLock implements Serializable {

    private static final long serialVersionUID = 2249069246763182397L;

    static final int MAX_SCAN_RETRIES =
        Runtime.getRuntime().availableProcessors() > 1 ? 64 : 1;

    transient volatile HashEntry<K,V>[] table;

    transient int count;

    transient int modCount;

    transient int threshold;

    final float loadFactor;
}
```
```
final Segment<K,V>[] segments;
```
默认的并发级别为16，也就是说默认创建16个Segment。
```
static final int DEFAULT_CONCURRENCY_LEVEL = 16;
```

### 2.JDK1.8底层实现
在JDK1.8中，ConcurrentHashMap放弃了“锁分段”，取而代之的是类似于HashMap的数组+链表+红黑树结构，使用CAS算法和synchronized实现线程安全。
#### 2.1 CAS算法和volatile简单介绍
**CAS（比较与交换，Compare and swap）** 是一种无锁算法。

CAS有3个操作数
- 内存值V
- 旧的预期值A
- 要修改的新值B
当且仅当预期值A和内存值V相同时，将内存值V修改为B，否则什么都不做

- 当多个线程尝试使用CAS同时更新同一个变量时，只有其中一个线程能更新变量的值(A和内存值V相同时，将内存值V修改为B)，而其它线程都失败，失败的线程并不会被挂起，而是被告知这次竞争中失败，并可以再次尝试(否则什么都不做)

看了上面的描述应该就很容易理解了，先比较是否相等，如果相等则替换(CAS算法)

**volatile关键字**
volatile经典总结：volatile仅仅用来保证该变量对所有线程的可见性，但不保证原子性。

- 保证该变量对所有线程的可见性
        - 在多线程的环境下：当这个变量修改时，所有的线程都会知道该变量被修改了，也就是所谓的“可见性”
- 不保证原子性
        - 修改变量(赋值)实质上是在JVM中分了好几步，而在这几步内(从装载变量到修改)，它是不安全的。
        
### 3 JDK 1.8 的改动
JDK 1.7 使用分段锁机制来实现并发更新操作，核心类为 Segment，它继承自重入锁 ReentrantLock，并发度与 Segment 数量相等。

JDK 1.8 使用了 CAS 操作来支持更高的并发度，在 CAS 操作失败时使用内置锁 synchronized。

并且 JDK 1.8 的实现也在链表过长时会转换为红黑树。

### 4 总结
- 底层结构是散列表(数组+链表)+红黑树，这一点和HashMap是一样的。
- Hashtable是将所有的方法进行同步，效率低下。而ConcurrentHashMap作为一个高并发的容器，它是通过部分锁定+CAS算法来进行实现线程安全的。CAS算法也可- - 以认为是乐观锁的一种~
- 在高并发环境下，统计数据(计算size...等等)其实是无意义的，因为在下一时刻size值就变化了。
- get方法是非阻塞，无锁的。重写Node类，通过volatile修饰next来实现每次获取都是最新设置的值
- ConcurrentHashMap的key和Value都不能为null
## LinkedHashMap
### 存储结构
继承自HashMap，因此具有和HashMap一样的快速查找特性
```
public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>
```
内部维护了一个双向链表，用来维护插入顺序或者LRU顺序。
```
/**
 * The head (eldest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> head;

/**
 * The tail (youngest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> tail;
```
