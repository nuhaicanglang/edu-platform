# 面向对象编程（OOP）核心概念

## 1. 类与对象

类是对象的模板，对象是类的实例。

```java
// 定义类
public class Student {
    // 成员变量（属性）
    private String name;
    private int age;
    private double gpa;

    // 构造方法
    public Student(String name, int age, double gpa) {
        this.name = name;
        this.age = age;
        this.gpa = gpa;
    }

    // 成员方法
    public void introduce() {
        System.out.println("我是" + name + "，今年" + age + "岁，GPA：" + gpa);
    }

    // Getter/Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// 创建对象
Student s1 = new Student("张三", 20, 3.8);
s1.introduce();
```

## 2. 封装（Encapsulation）

将数据和行为封装在类中，通过访问修饰符控制访问权限。

| 修饰符 | 同类 | 同包 | 子类 | 其他 |
|--------|------|------|------|------|
| private | ✓ | ✗ | ✗ | ✗ |
| 默认 | ✓ | ✓ | ✗ | ✗ |
| protected | ✓ | ✓ | ✓ | ✗ |
| public | ✓ | ✓ | ✓ | ✓ |

## 3. 继承（Inheritance）

子类继承父类的属性和方法，使用 `extends` 关键字。

```java
// 父类
public class Animal {
    protected String name;
    protected int age;

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void speak() {
        System.out.println(name + "发出声音");
    }
}

// 子类
public class Dog extends Animal {
    private String breed;

    public Dog(String name, int age, String breed) {
        super(name, age);  // 调用父类构造方法
        this.breed = breed;
    }

    @Override
    public void speak() {
        System.out.println(name + "：汪汪汪！");
    }

    public void fetch() {
        System.out.println(name + "去捡球了");
    }
}
```

## 4. 多态（Polymorphism）

同一方法调用，不同对象表现不同行为。

```java
Animal a1 = new Dog("旺财", 3, "拉布拉多");
Animal a2 = new Cat("咪咪", 2);

a1.speak();  // 旺财：汪汪汪！
a2.speak();  // 咪咪：喵喵喵！

// 向下转型
if (a1 instanceof Dog) {
    Dog d = (Dog) a1;
    d.fetch();
}
```

## 5. 抽象类与接口

### 抽象类
```java
public abstract class Shape {
    protected String color;

    public Shape(String color) {
        this.color = color;
    }

    // 抽象方法，子类必须实现
    public abstract double area();
    public abstract double perimeter();

    // 普通方法
    public void describe() {
        System.out.println("颜色：" + color + "，面积：" + area());
    }
}

public class Circle extends Shape {
    private double radius;

    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double area() { return Math.PI * radius * radius; }

    @Override
    public double perimeter() { return 2 * Math.PI * radius; }
}
```

### 接口
```java
public interface Flyable {
    int MAX_HEIGHT = 10000;  // 隐式 public static final

    void fly();              // 隐式 public abstract
    void land();

    // Java 8+ 默认方法
    default void hover() {
        System.out.println("悬停中...");
    }
}

public class Airplane implements Flyable {
    @Override
    public void fly() { System.out.println("飞机起飞！"); }

    @Override
    public void land() { System.out.println("飞机降落！"); }
}
```

## 6. Object类常用方法

```java
// toString()
@Override
public String toString() {
    return "Student{name='" + name + "', age=" + age + "}";
}

// equals() - 比较内容
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Student)) return false;
    Student other = (Student) obj;
    return age == other.age && name.equals(other.name);
}

// hashCode() - 与equals保持一致
@Override
public int hashCode() {
    return Objects.hash(name, age);
}
```

## 知识点总结
- **封装**：隐藏实现细节，提供公共接口，提高安全性
- **继承**：代码复用，建立类层次结构，Java只支持单继承
- **多态**：编译时多态（方法重载），运行时多态（方法重写）
- **抽象类**：可有普通方法，不能实例化，适合模板方法模式
- **接口**：纯抽象规范，支持多实现，Java 8后可有default方法
