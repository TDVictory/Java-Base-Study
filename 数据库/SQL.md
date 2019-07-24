# MYSQL的SQL语言
- **SQL**：Structure Query Language，结构化查询语言
- **DDL**：数据定义语言，定义数据库，数据表它们的结构：create（创建）、drop（删除） alter（修改）
- **DML**：数据操纵语言，主要是用来操作数据 insert（插入） update（修改） delete（删除）
- **DCL**：数据控制语言，定义访问权限，取消访问权限，安全设置 grant
- **DQL**：数据查询语言，select（查询） from子句 where子句

# 数据库的CRUD操作
## 登陆账号
```
mysql -u用户名 -p用户密码;
```
## 创建数据库
如果不设置初始化指令，数据库将按照默认格式创建
```
create database 数据库名称;
create database 数据库名称 初始化指令;
```
## 查看数据库
查看所有数据库
```
show databases;
```

查看指定数据库
```
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
| char/string | char/varchar/char()/varchar() |
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
