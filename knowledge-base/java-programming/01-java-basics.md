# Java基础语法

## 1. 变量与数据类型

Java是强类型语言，每个变量必须声明类型。

### 基本数据类型（8种）

| 类型 | 大小 | 范围 | 默认值 |
|------|------|------|--------|
| byte | 1字节 | -128 ~ 127 | 0 |
| short | 2字节 | -32768 ~ 32767 | 0 |
| int | 4字节 | -2^31 ~ 2^31-1 | 0 |
| long | 8字节 | -2^63 ~ 2^63-1 | 0L |
| float | 4字节 | 约±3.4×10^38 | 0.0f |
| double | 8字节 | 约±1.8×10^308 | 0.0 |
| char | 2字节 | 0 ~ 65535 | '\u0000' |
| boolean | 1字节 | true/false | false |

```java
int age = 20;
double salary = 8500.50;
char grade = 'A';
boolean isActive = true;
```

## 2. 运算符

### 算术运算符
```java
int a = 10, b = 3;
System.out.println(a + b);  // 13
System.out.println(a - b);  // 7
System.out.println(a * b);  // 30
System.out.println(a / b);  // 3 (整数除法)
System.out.println(a % b);  // 1
```

### 比较与逻辑运算符
```java
boolean result = (a > b) && (b != 0);  // true
boolean either = (a < 0) || (b > 0);   // true
boolean neg = !true;                     // false
```

## 3. 流程控制

### if-else
```java
int score = 85;
if (score >= 90) {
    System.out.println("优秀");
} else if (score >= 70) {
    System.out.println("良好");
} else {
    System.out.println("需要努力");
}
```

### switch
```java
int day = 3;
switch (day) {
    case 1: System.out.println("周一"); break;
    case 2: System.out.println("周二"); break;
    case 3: System.out.println("周三"); break;
    default: System.out.println("其他");
}
```

### 循环
```java
// for循环
for (int i = 0; i < 5; i++) {
    System.out.println(i);
}

// while循环
int n = 0;
while (n < 5) {
    System.out.println(n++);
}

// 增强for循环（foreach）
int[] arr = {1, 2, 3, 4, 5};
for (int x : arr) {
    System.out.println(x);
}
```

## 4. 数组

```java
// 声明与初始化
int[] nums = new int[5];
int[] scores = {90, 85, 78, 92, 88};

// 二维数组
int[][] matrix = {{1,2,3},{4,5,6},{7,8,9}};

// 数组长度
System.out.println(scores.length);  // 5
```

## 5. 类型转换

```java
// 自动类型转换（小→大）
int i = 100;
long l = i;       // int → long
double d = l;     // long → double

// 强制类型转换（大→小，可能丢失精度）
double pi = 3.14;
int intPi = (int) pi;  // 3
```

## 知识点总结
- Java基本数据类型共8种，引用类型包括类、接口、数组
- 整数默认int，浮点数默认double，长整型需加L
- 自动类型转换方向：byte→short→int→long→float→double
- 强制类型转换可能导致精度丢失，需谨慎使用
