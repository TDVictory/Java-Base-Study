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

## 三、源码分析
如果没有特别说明，以下源码分析基于JDK1.8.
### ArrayList
#### 1.概览
因为ArrayList是基于数组实现的，所以支持快速随机访问。RandomAccess接口标识着该类支持快速随机访问。
```
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```
数组的默认大小为10.
```
private static final int DEFAULT_CAPACITY = 10;
```
#### 2.扩容
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

#### 3.删除元素
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
#### 4. Fail-Fast
modCount 用来记录 ArrayList 结构发生变化的次数。结构发生变化是指添加或者删除至少一个元素的所有操作，或者是调整内部数组的大小，仅仅只是设置元素的值不算结构发生变化。

在进行序列化或者迭代等操作时，需要比较操作前后 modCount 是否改变，如果改变了需要抛出 ConcurrentModificationException。

#### 5. 序列化
ArrayList 基于数组实现，并且具有动态扩容特性，因此保存元素的数组不一定都会被使用，那么就没必要全部进行序列化。

保存元素的数组 elementData 使用 transient 修饰，该关键字声明数组默认不会被序列化

### Vector
#### 1.同步
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
#### 2.与ArrayList的比较
- Vector 是同步的，因此开销就比 ArrayList 要大，访问速度更慢。最好使用 ArrayList 而不是 Vector，因为同步操作完全可以由程序员自己来控制
- Vector 每次扩容请求其大小的 2 倍空间，而 ArrayList 是 1.5 倍。

#### 3.替代方案
可以使用Collections.synchronizedList(); 得到一个线程安全的 ArrayList。
```
List<String> list = new ArrayList<>();
List<String> synList = Collections.synchronizedList(list);
```
也可以使用 concurrent 并发包下的 CopyOnWriteArrayList 类。
```
List<String> list = new CopyOnWriteArrayList<>();
```

### CopyOnWriteArrayList
#### 读写分离
写操作在一个复制的数组上进行，读操作还是在原始数组中进行，读写分离，互不影响。

写操作需要加锁，防止并发写入时导致写入数据丢失。

写操作结束之后需要把原始数组指向新的复制数组。

#### 适用场景
CopyOnWriteArrayList在写操作的同时允许读操作，大大提高了读操作的性能，因此很适合读多写少的应用场景。
但是CopyOnWriteArrayList也有其缺陷：
- 内存占用：在写操作时需要复制一个新的数组，使得内存占用为原来的两倍左右。
- 数据不一致：读操作不能读取实时性的数据，因为部分写操作的数据还未同步到读数组中。
所以CopyOnWriteArrayList不适用于内存敏感以及对实时性要求很高的场景。

### LinkedList
#### 1.概览
基于双向链表实现，使用Node存储链表节点信息。
```
private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;
}
```
#### 2.与ArrayList的比较
- ArrayList基于动态数组实现，LinkedList基于双向链表实现
- ArrayList支持随机访问，LinkedList不支持
LinkedList在任意位置添加删除元素更快，ArrayList在查找任意元素更快
 
### HashMap
以下源码为JDK1.7
#### 1.存储结构
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
#### 2.拉链法的工作原理
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

