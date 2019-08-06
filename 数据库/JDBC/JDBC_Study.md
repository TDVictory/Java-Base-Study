#JDBC

> JAVA Database Connectivity java 数据库连接

* **为什么会出现JDBC**

> SUN公司提供的一种数据库访问规则、规范, 由于数据库种类较多，并且java语言使用比较广泛，sun公司就提供了一种规范，让其他的数据库提供商去实现底层的访问规则。 我们的java程序只要使用sun公司提供的jdbc驱动即可。


#一、使用JDBC的基本步骤

## 1.1 注册驱动

```java
//1.注册驱动，此时Driver为 com.mysql.jdbc.Driver
DriverManager.registerDriver(new Driver());
```

## 1.2 建立连接

conn = DriverManager.getConnection("jdbc:mysql://localhost/student", "root", "root");

```java
//2.建立连接
// 建立连接 参数： url
DriverManager.getConnection("jdbc:mysql://localhost/test?user=monty&password=greatsqldb");
// 建立连接 参数一： 协议 + 访问的数据库 ， 参数二： 用户名 ， 参数三： 密码。
DriverManager.getConnection("jdbc:mysql://localhost/test","monty","greatsqldb");
```

注意，这里MySQL如果是直接通导入的，直接执行上述语句会报错，因为需要指定服务器的时区。通过在url后面加上**?serverTimezone=UTC **后解决。

```java
connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/day02?serverTimezone=UTC","root","vivedu");
```

## 1.3 创建statement

创建statement ， 跟数据库打交道，一定需要这个对象

```java
st = conn.createStatement();
```

我们对数据库进行操作，都是通过statement下的方法

## 1.4 执行查询 

执行sql ，得到ResultSet

```java
String sql = "select * from t_stu";
ResultSet rs = st.executeQuery(sql);
```

## 1.5 遍历结果集

遍历查询每一条记录

```java
while (rs.next())
{
    int id = rs.getInt("pid");
    String name = rs.getString("pname");
    double price = rs.getDouble("price");

    System.out.println(id + " " + name + " " + price);
}
```

​				

## 1.6 释放资源

我们在最后应该要释放ResultSet、Statement、Connection三个资源


		if (rs != null) {
	        try {
	            rs.close();
	        } catch (SQLException sqlEx) { } // ignore 
	        rs = null;
	    }
	
		...


#二、JDBC 工具类构建

## 2.1 资源释放工作的整合

## 2.2 驱动防二次注册


   	DriverManager.registerDriver(new com.mysql.jdbc.Driver());

   	Driver 这个类继承自**com.mysql.cj.jdbc.Driver**

```java
public class Driver extends com.mysql.cj.jdbc.Driver
```

​	而在**com.mysql.cj.jdbc.Driver**里面有静态代码块，一上来就执行了

```java
public class Driver extends NonRegisteringDriver implements java.sql.Driver {
    public Driver() throws SQLException {
    }
	//已经注册了Driver
    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException var1) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
```

​	所以等同于我们注册了两次驱动。 其实没这个必要的。

   	//静态代码块 ---> 类加载了，就执行。 java.sql.DriverManager.registerDriver(new Driver());


```java
//最后形成以下代码即可。
Class.forName("com.mysql.cj.jdbc.Driver");	
```

## 2.3 使用properties配置文件

1. 在src底下声明一个文件 xxx.properties ，里面的内容如下：

    ```properties
    driverClass=com.mysql.jdbc.Driver
    url=jdbc:mysql://localhost:3306/day02?serverTimezone=UTC
    name=root
    password=vivedu
    
    ```

    

2. 在工具类里面，使用静态代码块，读取属性


```java
	static{
		try {
			//1. 创建一个属性配置对象
			Properties properties = new Properties();
			InputStream is = new FileInputStream("jdbc.properties"); //对应文件位于工程根目录
			 
			//使用类加载器，去读取src底下的资源文件。 后面在servlet  //对应文件位于src目录底下
			//InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
			//导入输入流。
			properties.load(is);
			
			//读取属性
			driverClass = properties.getProperty("driverClass");
			url = properties.getProperty("url");
			name = properties.getProperty("name");
			password = properties.getProperty("password");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
```

​	

#数据库的CRUD sql

* insert

```sql
INSERT INTO USER VALUES (null,'zhaoliu',345,13744448888)
```



```java
@Test
    public void testInsert(){
        Connection connection = null;
        Statement statement = null;

        try {
            connection = JDBCUtil.getConnection();
            statement = connection.createStatement();
            int result = statement.executeUpdate("INSERT INTO USER VALUES (null,'zhaoliu',345,13744448888)");
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
```

* delete

```sql
DELETE FROM user WHERE uid = 4
```




```java
 @Test
    public void testDelete(){
        Connection connection = null;
        Statement statement = null;

        try {
            connection = JDBCUtil.getConnection();
            statement = connection.createStatement();
            int result = statement.executeUpdate("DELETE FROM user WHERE uid = 4");
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

```

* query

```sql
select * from user
```


```java
@Test
    public void testQuery(){
        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;
        String sql = "select * from product";

        try {
            connection = JDBCUtil.getConnection();
            st = connection.createStatement();           
            rs = st.executeQuery("select * from user");
            while (rs.next()){
                int id = rs.getInt("uid");
                String str = rs.getString("username");

                System.out.println(id + " " + str);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.release(connection,st,rs);
        }
    }
```

* update

```sql
UPDATE user SET username = 'wangwu' WHERE uid = 1
```


```java
    @Test
    public void testUpdate(){
        Connection connection = null;
        Statement statement = null;

        try {
            connection = JDBCUtil.getConnection();
            statement = connection.createStatement();
            int result = statement.executeUpdate("UPDATE user SET username = 'wangwu' WHERE uid = 1");
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
```


#三、使用单元测试，测试代码

1. 定义一个类， TestXXX , 里面定义方法 testXXX.

2. 添加junit的支持。 

    我这里使用了Maven管理依赖

    ```xml
    <!-- https://mvnrepository.com/artifact/junit/junit -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
        <scope>test</scope>
    </dependency>
    ```

3. 在方法的上面加上注解 ， 其实就是一个标记。

    ```java
    
    @Test
    
    public void testQuery() {
    
        ...
    
    }
    
    ```

4. 执行对应的单元测试


#四、Dao模式

> Data Access Object 数据访问对象

1. 新建一个dao的接口， 里面声明数据库访问规则


```java
public interface UserDao {
    void query();
}
```


2. 新建一个dao的实现类，具体实现早前定义的规则

```java
public class UserDaoImpl implements UserDao {
    @Override
    public void query() {
        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;
        String sql = "select * from product";

        try {
            connection = JDBCUtil.getConnection();
            st = connection.createStatement();
            rs = st.executeQuery("select * from user");
            while (rs.next()){
                int id = rs.getInt("uid");
                String str = rs.getString("username");

                System.out.println(id + " " + str);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtil.release(connection,st,rs);
        }
    }
}
```

3. 直接使用实现

```java
@Test
public void testFindAll(){
    UserDao dao = new UserDaoImpl();
    dao.query();
}
```



# 五、Statement安全问题

1. Statement执行 ，其实是拼接sql语句的。  先拼接sql语句，然后在一起执行。 

```java
	String sql = "select * from t_user where username='"+ username  +"' and password='"+ password +"'";

	UserDao dao = new UserDaoImpl();
	dao.login("admin", "100234khsdf88' or '1=1");

	SELECT * FROM t_user WHERE username='admin' AND PASSWORD='100234khsdf88' or '1=1' 

	//前面先拼接sql语句， 如果变量里面带有了 数据库的关键字，那么一并认为是关键字。 不认为是普通的字符串。 
	rs = st.executeQuery(sql);
```

## PrepareStatement

> 该对象就是替换前面的statement对象。

相比较以前的statement， 预先处理给定的sql语句，对其执行语法检查。 在sql语句里面使用 ? 占位符来替代后续要传递进来的变量。 后面进来的变量值，将会被看成是字符串，不会产生任何的关键字。

```java
connection = JDBCUtil.getConnection();
PreparedStatement ps = connection.prepareStatement("select * from user where username =? and password=?");
//st = connection.createStatement();
//rs = st.executeQuery(sql);
ps.setString(1,username);
ps.setString(2,password);
rs = ps.executeQuery();
```

​同理可以使用PrepareStatement进行增删改查操作。

