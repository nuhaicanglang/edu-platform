# 软件设计模式

## 一、创建型模式

### 单例模式（Singleton）
确保类只有一个实例，提供全局访问点。

```java
// 双重检查锁（线程安全）
public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private Connection conn;

    private DatabaseConnection() {
        // 初始化数据库连接
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
}
// 使用场景：配置管理、连接池、日志记录器、线程池
```

### 工厂方法模式（Factory Method）
定义创建对象的接口，由子类决定实例化哪个类。

```java
public interface Notification {
    void send(String message, String recipient);
}

public class EmailNotification implements Notification {
    @Override
    public void send(String message, String recipient) {
        System.out.println("发送邮件给 " + recipient + ": " + message);
    }
}

public class SMSNotification implements Notification {
    @Override
    public void send(String message, String recipient) {
        System.out.println("发送短信给 " + recipient + ": " + message);
    }
}

// 工厂类
public class NotificationFactory {
    public static Notification create(String type) {
        return switch (type.toLowerCase()) {
            case "email" -> new EmailNotification();
            case "sms"   -> new SMSNotification();
            default -> throw new IllegalArgumentException("未知通知类型: " + type);
        };
    }
}

// 使用
Notification n = NotificationFactory.create("email");
n.send("作业已批改", "student@edu.com");
```

### 建造者模式（Builder）
分步骤构建复杂对象，支持链式调用。

```java
public class QueryBuilder {
    private String table;
    private List<String> columns = new ArrayList<>();
    private String condition;
    private String orderBy;
    private int limit = -1;

    public QueryBuilder from(String table) { this.table = table; return this; }
    public QueryBuilder select(String... cols) { columns.addAll(Arrays.asList(cols)); return this; }
    public QueryBuilder where(String condition) { this.condition = condition; return this; }
    public QueryBuilder orderBy(String col) { this.orderBy = col; return this; }
    public QueryBuilder limit(int n) { this.limit = n; return this; }

    public String build() {
        String sql = "SELECT " + (columns.isEmpty() ? "*" : String.join(",", columns))
                + " FROM " + table;
        if (condition != null) sql += " WHERE " + condition;
        if (orderBy != null)   sql += " ORDER BY " + orderBy;
        if (limit > 0)         sql += " LIMIT " + limit;
        return sql;
    }
}

// 链式调用
String sql = new QueryBuilder()
    .from("student")
    .select("name", "gpa")
    .where("class_id = 1")
    .orderBy("gpa DESC")
    .limit(10)
    .build();
```

---

## 二、结构型模式

### 装饰器模式（Decorator）
动态给对象添加职责，比继承更灵活。

```java
public interface TextProcessor {
    String process(String text);
}

public class PlainText implements TextProcessor {
    @Override public String process(String text) { return text; }
}

// 抽象装饰器
public abstract class TextDecorator implements TextProcessor {
    protected TextProcessor wrapped;
    public TextDecorator(TextProcessor wrapped) { this.wrapped = wrapped; }
}

public class TrimDecorator extends TextDecorator {
    public TrimDecorator(TextProcessor p) { super(p); }
    @Override public String process(String text) {
        return wrapped.process(text).trim();
    }
}

public class UpperCaseDecorator extends TextDecorator {
    public UpperCaseDecorator(TextProcessor p) { super(p); }
    @Override public String process(String text) {
        return wrapped.process(text).toUpperCase();
    }
}

// 叠加装饰
TextProcessor processor = new UpperCaseDecorator(new TrimDecorator(new PlainText()));
System.out.println(processor.process("  hello world  ")); // "HELLO WORLD"
```

### 代理模式（Proxy）
控制对对象的访问，可用于缓存、权限控制、日志。

```java
public interface UserService {
    User getUserById(Long id);
}

// 带缓存的代理
public class CachedUserServiceProxy implements UserService {
    private final UserService realService;
    private final Map<Long, User> cache = new HashMap<>();

    public CachedUserServiceProxy(UserService realService) {
        this.realService = realService;
    }

    @Override
    public User getUserById(Long id) {
        return cache.computeIfAbsent(id, realService::getUserById);
    }
}
```

---

## 三、行为型模式

### 观察者模式（Observer）
一对多依赖关系，状态改变时自动通知所有观察者。

```java
public interface Observer {
    void update(String event, Object data);
}

public class EventBus {
    private Map<String, List<Observer>> listeners = new HashMap<>();

    public void subscribe(String event, Observer observer) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(observer);
    }

    public void publish(String event, Object data) {
        listeners.getOrDefault(event, Collections.emptyList())
                 .forEach(o -> o.update(event, data));
    }
}

// 使用
EventBus bus = new EventBus();
bus.subscribe("assignment.graded", (event, data) ->
    System.out.println("发送批改通知: " + data));
bus.subscribe("assignment.graded", (event, data) ->
    System.out.println("更新学情分析: " + data));

bus.publish("assignment.graded", "作业《Java基础》已批改完成");
```

### 策略模式（Strategy）
定义算法族，使算法可以相互替换。

```java
@FunctionalInterface
public interface GradingStrategy {
    int grade(String studentAnswer, String referenceAnswer);
}

public class ExactMatchStrategy implements GradingStrategy {
    @Override
    public int grade(String answer, String reference) {
        return answer.trim().equalsIgnoreCase(reference.trim()) ? 100 : 0;
    }
}

public class KeywordStrategy implements GradingStrategy {
    private List<String> keywords;
    public KeywordStrategy(List<String> keywords) { this.keywords = keywords; }
    @Override
    public int grade(String answer, String reference) {
        long matched = keywords.stream().filter(answer::contains).count();
        return (int)(matched * 100 / keywords.size());
    }
}

public class AssignmentGrader {
    private GradingStrategy strategy;
    public void setStrategy(GradingStrategy strategy) { this.strategy = strategy; }
    public int grade(String answer, String reference) {
        return strategy.grade(answer, reference);
    }
}

// 使用
AssignmentGrader grader = new AssignmentGrader();
grader.setStrategy(new KeywordStrategy(List.of("递归", "时间复杂度", "O(n)")));
int score = grader.grade(studentAnswer, reference);
```

### 模板方法模式（Template Method）
定义算法骨架，子类实现具体步骤。

```java
public abstract class DataExporter {
    // 模板方法（final防止被重写）
    public final void export(String filename) {
        List<Object> data = fetchData();     // 抽象步骤
        String formatted = format(data);     // 抽象步骤
        writeToFile(formatted, filename);    // 通用步骤
        sendNotification(filename);          // 钩子方法
    }

    protected abstract List<Object> fetchData();
    protected abstract String format(List<Object> data);

    private void writeToFile(String content, String filename) {
        // 通用文件写入逻辑
    }

    protected void sendNotification(String filename) {
        // 默认空实现，子类可选择覆盖
    }
}

public class ExcelExporter extends DataExporter {
    @Override
    protected List<Object> fetchData() { return queryDatabase(); }
    @Override
    protected String format(List<Object> data) { return convertToExcel(data); }
}
```

---

## 设计原则（SOLID）

| 原则 | 描述 | 违反示例 |
|------|------|---------|
| **S**ingle Responsibility | 一个类只负责一件事 | UserService同时处理业务和发邮件 |
| **O**pen/Closed | 对扩展开放，对修改关闭 | 每加新功能就修改现有代码 |
| **L**iskov Substitution | 子类可替代父类 | 重写方法抛出新异常 |
| **I**nterface Segregation | 接口应小而专 | 一个接口包含20个方法 |
| **D**ependency Inversion | 依赖抽象，不依赖具体 | 直接new具体类而非注入接口 |

## 知识点总结
- 单例用于全局唯一对象，工厂用于解耦对象创建
- 装饰器通过组合替代继承，灵活添加功能
- 观察者实现事件驱动架构，降低耦合度
- 策略模式将算法封装为可替换的对象
- SOLID原则是指导面向对象设计的基本准则
