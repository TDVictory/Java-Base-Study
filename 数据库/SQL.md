# MYSQL的SQL语言
- **SQL**：Structure Query Language，结构化查询语言
- **DDL**：数据定义语言，定义数据库，数据表它们的结构：create（创建）、drop（删除） alter（修改）
- **DML**：数据操纵语言，主要是用来操作数据 insert（插入） update（修改） delete（删除）
- **DCL**：数据控制语言，定义访问权限，取消访问权限，安全设置 grant
- **DQL**：数据查询语言，select（查询） from子句 where子句

# 数据库的CRUD操作
## 登陆账号
```sql
mysql -u用户名 -p用户密码;
```
## 创建数据库
如果不设置初始化指令，数据库将按照默认格式创建
```sql
create database 数据库名称;
create database 数据库名称 初始化指令;
```
## 查看数据库
查看所有数据库
```sql
show databases;
```

查看指定数据库
```sql
show create database 指定数据库名称;
```

## 修改数据库
```
alter databases 修改指令;
```

## 删除数据库
```
drop database 数据库名称;
```
## 使用（进入）数据库
```
use 数据库名称;
```

# 表的CRUD操作
## 创建表
> create database 数据库的名字
```
create table 表名(
  列名 列的类型 约束，
  列名2 列2的类型 约束
);
```

### 列的类型

| java | sql |
| --- | --- |
| int | int |
| char/string | char/char()/varchar() |
| double | double |
| float | float |
| boolean | boolean |
| date | date/time/datetime/timestamp |
| 额外 | text：存放文本 / blob：存放二进制 |

### 列的约束
- 主键约束：primary key
- 唯一约束：unique
- 非空约束：not null

### 简单示例
创建一个实体为学生的数据库，学生的属性有学生ID、姓名、性别、年龄
```
create table student(
  sid int primary key,
  sname varchar(10),
  sex int,
  age int
);
```

## 查看表
查看所有的表
```
show tables;
```
查看表的创建过程
```
show create table 表名;
```
查看表结构
```
desc 表名;
```

## 修改表
- 添加列：add
- 修改列属性：modify
- 修改列名：change
- 删除列：drop
- 修改表名：rename

### 添加列
> alter table 表名 add 列名 列的类型 列的约束
```
alter table student add grade int not null;
```

### 修改列属性
> alter table 表名 modify 列名 列的类型 列的约束
```
alter table student modify sex varchar(2);
```

### 修改列名
> alter table 表名 change 列名 新列名 新列的类型 新列的约束
```
alter table student change sex ssex int not null;
```

### 删除列
> alter table 表名 drop 列名
```
alter table student drop grade;
```

### 修改表名
> rename table 表名 to 新表名
修改表名这种可能会带来较大框架变动的行为尽可能少做
```
rename table student to Student;
```

## 删除表
> drop table 表名
```
drop table student;
```

# 表中数据的CRUD操作
## 插入数据
> insert into 表名(列名1，列名2，列名3) values(值1，值2，值3)
```
insert into student(sid,sname,ssex,age) values(1,'zhangsan',1,23);
```

### 简单写法
表名结构可以省略，但省略时是默认所有列信息都必须插入。即如果插入的时全列名的数据，表名后的列名可以省略
```
insert into student values(1,'zhangsan',1,23);
```
如果时插入部分列的话，列名不能省略。
```
insert into student(sid,name) values(3,'lisi');
```

### 批量插入
```
insert into student values
(1,'zhangsan',1,23),
(2,'zhangsan',1,23),
(3,'zhangsan',1,23),
...;
```
## 删除数据
> delete from 表名 where 条件
```
delete from student where sid=10;
```
如果不给条件则是删除表内所有数据
```
delete from student;
```
### delete和truncate
- delete：DML，一条条删除表中的数据
- truncate：DDL，先删除表再重建表

因此，如果表内数据量较少，delete速度快于truncate。

## 更新数据
> update 表名 set 列名=列的值，列名2=列的值2 where 条件
```
update student set sname='李四' where sid=2;
```
如果不加条件，则是更新表内所有数据
```
update student set sname='李四',sex=1;
```
## 查询数据
> select \[distinct\] \[\*\] \[列名，列名2\] from 表名 \[where 条件\]
distinct：去除重复的数据

### 查询所有数据
```
select * from category;
```

### 查询特定列
```
select sname,sex from student;
```

### 别名查询
以as作为关键字，as关键字可以省略。

- 表别名：```select s.sname,s.sex from student as s;```
- 列别名：```select sname as 学生姓名,sex 学生性别 from student;```

### 去掉重复的值
```
select distinct sex from student;
```
使用distinct可以将所有重复的值剔除掉。（只是展示所有不重复的值，并不会影响数据库的数据）

### select运算查询
```
select *,price*0.8 from product;
select *,price*0.8 as 折后价 from product;
```
此操作后会展示出一列price\*0.8的数据，使用as做别名后该列的列名将会从price\*0.8改成折后价。此操作同样不会影响数据库的数据。

### 条件查询
指定条件，确定要操作的记录。下述语句描述的是查看product表内所有价格大于50的商品数据。
```
select * from product where price > 50;
```
#### 关系运算符
- 大于：>
- 小于：<
- 不大于：<=
- 不小于：>=
- 不等于：<>（标准SQL语法），!=（非标准SQL语法）
- 范围内：between...and...（必须前者不大于后者）
#### 逻辑运算符
- and：与运算
- or：或运算
- not：非运算

#### 模糊查询
> select * from 表名 where 查询列名 like 关键字
- %：代表多个字符
- \_：代表单个字符
查询所有带有"饼"字的商品
```sql
select * from product where pname like '%饼%';
```
查询所有第二个字符为"饼"字的商品
```sql
select * from product where pname like '_饼';
```
#### 聚合函数
- sum()：求和
- avg()：求平均值
- count()：统计数量
- max()：最大值
- min()：最小值
```sql
select sum(price) from product;
``` 
注意：where后不能接聚合函数
