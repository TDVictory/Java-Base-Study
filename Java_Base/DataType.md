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
