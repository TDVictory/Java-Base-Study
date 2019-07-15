# 一、基础概念
## URI
URI（Uniform Resource Identifier）统一资源标识符
URI包括URL和URN
- URL（Uniform Resource Locator）：统一资源定位符
- URN（Uniform Resource Name）：统一资源名称

## 请求和响应报文
### 1.请求报文
- 请求行（Request Line）
- 请求头文件（Request Headers）
- 用于划分请求消息头文件（Request Message Headers，包含请求行和请求头文件）和请求消息主体的空行
- 请求消息主体（Request Message Body）

### 2.响应报文
- 状态行（Status Line）
- 响应头文件（Response Headers）
- 用于划分请求响应头文件（Response Message Headers，包含状态行和响应头文件）和响应消息主体的空行
- 响应消息主体（Response Message Body）

# 二、HTTP方法
客户端发送的 请求报文 第一行为请求行，包含了方法字段。
## GET
> 获取资源
GET方法主要用于获取对应资源。
## POST
> 传输实体主体
POST主要用来传输数据，GET主要用来获取资源。
## HEAD
> 获取报文首部
和GET方法类似，但是不返回报文实体主体部分。

主要用于确认URL的有效性和资源更新的日期时间等
## PUT
> 上传文件
由于自身不带验证机制，任何人都可以上传文件，因此存在安全性问题，一般不使用该方法。
```
PUT /new.html HTTP/1.1
Host: example.com
Content-type: text/html
Content-length: 16

<p>New File</p>
```

## PATCH
> 对资源进行部分修改
PUT 也可以用于修改资源，但是只能完全替代原始资源，PATCH 允许部分修改。
```
PATCH /file.txt HTTP/1.1
Host: www.example.com
Content-Type: application/example
If-Match: "e0023aa4e"
Content-Length: 100

[description of changes]
```
## DELETE
> 删除文件

与 PUT 功能相反，并且同样不带验证机制。
```
DELETE /file.html HTTP/1.1
```
## OPTION
> 查询支持的方法

查询指定的 URL 能够支持的方法。

会返回 Allow: GET, POST, HEAD, OPTIONS 这样的内容。

## CONNECT
> 要求在与代理服务器通信时建立隧道

使用 SSL（Secure Sockets Layer，安全套接层）和 TLS（Transport Layer Security，传输层安全）协议把通信内容加密后经网络隧道传输。
```
CONNECT www.example.com:443 HTTP/1.1
```
## TRACE
> 追踪路径

服务器会将通信路径返回给客户端。

发送请求时，在 Max-Forwards 首部字段中填入数值，每经过一个服务器就会减 1，当数值为 0 时就停止传输。

通常不会使用 TRACE，并且它容易受到 XST 攻击（Cross-Site Tracing，跨站追踪）。
