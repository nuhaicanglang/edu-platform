# 线性数据结构

## 1. 数组（Array）

数组是最基本的数据结构，元素在内存中连续存储。

```java
// 时间复杂度：
// 访问: O(1) — 直接通过下标
// 搜索: O(n) — 未排序；O(log n) — 已排序+二分
// 插入: O(n) — 需移动元素
// 删除: O(n) — 需移动元素

int[] arr = {5, 3, 8, 1, 9, 2, 7};
// 二分查找（有序数组）
Arrays.sort(arr);
int idx = Arrays.binarySearch(arr, 7);  // 返回索引
```

## 2. 链表（Linked List）

### 单链表实现
```java
public class LinkedList<T> {
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    private Node<T> head;
    private int size;

    // 头插
    public void addFirst(T data) {
        Node<T> node = new Node<>(data);
        node.next = head;
        head = node;
        size++;
    }

    // 尾插
    public void addLast(T data) {
        Node<T> node = new Node<>(data);
        if (head == null) { head = node; }
        else {
            Node<T> cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = node;
        }
        size++;
    }

    // 删除指定值
    public boolean remove(T data) {
        if (head == null) return false;
        if (head.data.equals(data)) { head = head.next; size--; return true; }
        Node<T> cur = head;
        while (cur.next != null) {
            if (cur.next.data.equals(data)) {
                cur.next = cur.next.next;
                size--;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    // 反转链表（经典算法）
    public void reverse() {
        Node<T> prev = null, cur = head;
        while (cur != null) {
            Node<T> next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
        head = prev;
    }
}
```

## 3. 栈（Stack）

**后进先出（LIFO）** — 只能在栈顶操作。

```java
// 用数组实现栈
public class ArrayStack<T> {
    private Object[] data;
    private int top = -1;

    public ArrayStack(int capacity) {
        data = new Object[capacity];
    }

    public void push(T item) {
        if (top == data.length - 1) throw new RuntimeException("栈满");
        data[++top] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (top == -1) throw new RuntimeException("栈空");
        return (T) data[top--];
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (top == -1) throw new RuntimeException("栈空");
        return (T) data[top];
    }

    public boolean isEmpty() { return top == -1; }
}

// 应用：括号匹配
public boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    for (char c : s.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') {
            stack.push(c);
        } else {
            if (stack.isEmpty()) return false;
            char top = stack.pop();
            if (c == ')' && top != '(') return false;
            if (c == ']' && top != '[') return false;
            if (c == '}' && top != '{') return false;
        }
    }
    return stack.isEmpty();
}
```

## 4. 队列（Queue）

**先进先出（FIFO）** — 队尾入队，队头出队。

```java
// 循环队列实现
public class CircularQueue<T> {
    private Object[] data;
    private int front, rear, size;

    public CircularQueue(int capacity) {
        data = new Object[capacity + 1];
    }

    public boolean enqueue(T item) {
        if ((rear + 1) % data.length == front) return false;  // 队满
        data[rear] = item;
        rear = (rear + 1) % data.length;
        size++;
        return true;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (front == rear) throw new RuntimeException("队空");
        T item = (T) data[front];
        front = (front + 1) % data.length;
        size--;
        return item;
    }
}

// 应用：BFS（广度优先搜索）
public void bfs(int[][] grid, int startRow, int startCol) {
    Queue<int[]> queue = new LinkedList<>();
    queue.offer(new int[]{startRow, startCol});
    while (!queue.isEmpty()) {
        int[] pos = queue.poll();
        // 处理当前节点
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int[] d : dirs) {
            int r = pos[0] + d[0], c = pos[1] + d[1];
            if (r >= 0 && r < grid.length && c >= 0 && c < grid[0].length)
                queue.offer(new int[]{r, c});
        }
    }
}
```

## 复杂度对比

| 操作 | 数组 | 链表 | 栈 | 队列 |
|------|------|------|-----|------|
| 访问 | O(1) | O(n) | O(n) | O(n) |
| 头部插入 | O(n) | O(1) | — | O(1) |
| 尾部插入 | O(1)* | O(n)/O(1)** | O(1) | O(1) |
| 中间插入 | O(n) | O(n) | — | — |
| 搜索 | O(n) | O(n) | O(n) | O(n) |

\* 均摊；** 维护尾指针时O(1)
