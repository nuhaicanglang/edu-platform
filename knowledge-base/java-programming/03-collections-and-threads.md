# Java集合框架与多线程

## 一、集合框架

### 1. Collection体系

```
Collection
├── List（有序、可重复）
│   ├── ArrayList  —— 底层数组，查询快O(1)，增删慢O(n)
│   ├── LinkedList —— 底层双向链表，增删快O(1)，查询慢O(n)
│   └── Vector     —— 线程安全的ArrayList（已过时）
├── Set（无序、不重复）
│   ├── HashSet    —— 基于HashMap，无序
│   ├── LinkedHashSet —— 保持插入顺序
│   └── TreeSet    —— 自然排序或比较器排序
└── Queue（队列）
    ├── LinkedList
    ├── PriorityQueue —— 优先队列
    └── ArrayDeque    —— 双端队列
```

### 2. ArrayList 常用操作

```java
List<String> list = new ArrayList<>();
list.add("Java");
list.add("Python");
list.add("Go");
list.add(1, "C++");        // 指定位置插入

System.out.println(list.get(0));   // Java
System.out.println(list.size());   // 4
list.remove("Python");
list.remove(0);                    // 按索引删除

// 遍历
for (String s : list) System.out.println(s);
list.forEach(System.out::println);  // Lambda
```

### 3. HashMap 常用操作

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("张三", 90);
scores.put("李四", 85);
scores.put("王五", 92);

scores.get("张三");          // 90
scores.getOrDefault("赵六", 0);  // 0（不存在时默认值）
scores.containsKey("李四"); // true
scores.remove("王五");

// 遍历
for (Map.Entry<String, Integer> entry : scores.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
}

// Java 8 操作
scores.forEach((k, v) -> System.out.println(k + "：" + v));
scores.putIfAbsent("赵六", 88);
scores.merge("张三", 5, Integer::sum);  // 90+5=95
```

### 4. Collections工具类

```java
List<Integer> nums = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);
Collections.sort(nums);                  // 升序
Collections.sort(nums, Comparator.reverseOrder()); // 降序
Collections.max(nums);   // 9
Collections.min(nums);   // 1
Collections.shuffle(nums); // 随机打乱
```

---

## 二、多线程编程

### 1. 创建线程的方式

```java
// 方式一：继承Thread
class MyThread extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(getName() + ": " + i);
        }
    }
}
new MyThread().start();

// 方式二：实现Runnable（推荐）
Runnable task = () -> System.out.println("Task running: " + Thread.currentThread().getName());
new Thread(task).start();

// 方式三：实现Callable（有返回值）
Callable<Integer> callable = () -> {
    Thread.sleep(1000);
    return 42;
};
FutureTask<Integer> future = new FutureTask<>(callable);
new Thread(future).start();
Integer result = future.get();  // 阻塞直到完成
```

### 2. synchronized 同步

```java
public class Counter {
    private int count = 0;

    // 同步方法
    public synchronized void increment() {
        count++;
    }

    // 同步代码块
    public void decrement() {
        synchronized (this) {
            count--;
        }
    }

    public int getCount() { return count; }
}
```

### 3. 生产者-消费者模式

```java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProducerConsumer {
    static BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);

    static class Producer implements Runnable {
        @Override
        public void run() {
            for (int i = 1; i <= 20; i++) {
                try {
                    queue.put(i);
                    System.out.println("生产: " + i);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Integer item = queue.take();
                    System.out.println("消费: " + item);
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Producer()).start();
        new Thread(new Consumer()).start();
    }
}
```

### 4. 线程池（ExecutorService）

```java
ExecutorService pool = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    final int taskId = i;
    pool.submit(() -> System.out.println("任务" + taskId + "由" + Thread.currentThread().getName() + "执行"));
}
pool.shutdown();
```

## 知识点总结
- ArrayList适合随机访问，LinkedList适合频繁插删
- HashMap键唯一，基于哈希表，查找O(1)；TreeMap按键排序
- 线程生命周期：新建→就绪→运行→阻塞→死亡
- synchronized保证原子性和可见性，避免竞态条件
- BlockingQueue是生产者消费者模式的最佳选择
