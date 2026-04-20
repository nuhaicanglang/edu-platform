# 树形结构与图

## 一、二叉树

### 1. 定义与遍历

```java
public class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

// 前序遍历：根→左→右
public void preOrder(TreeNode root) {
    if (root == null) return;
    System.out.print(root.val + " ");
    preOrder(root.left);
    preOrder(root.right);
}

// 中序遍历：左→根→右（BST中序 = 升序）
public void inOrder(TreeNode root) {
    if (root == null) return;
    inOrder(root.left);
    System.out.print(root.val + " ");
    inOrder(root.right);
}

// 后序遍历：左→右→根
public void postOrder(TreeNode root) {
    if (root == null) return;
    postOrder(root.left);
    postOrder(root.right);
    System.out.print(root.val + " ");
}

// 层序遍历（BFS）
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        int size = queue.size();
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
    }
    return result;
}
```

### 2. 二叉搜索树（BST）

BST性质：左子树所有节点 < 根 < 右子树所有节点

```java
public class BST {
    private TreeNode root;

    public void insert(int val) {
        root = insert(root, val);
    }

    private TreeNode insert(TreeNode node, int val) {
        if (node == null) return new TreeNode(val);
        if (val < node.val) node.left = insert(node.left, val);
        else if (val > node.val) node.right = insert(node.right, val);
        return node;
    }

    public boolean search(int val) {
        TreeNode cur = root;
        while (cur != null) {
            if (val == cur.val) return true;
            cur = val < cur.val ? cur.left : cur.right;
        }
        return false;
    }
}
```

### 3. 常见算法题

```java
// 树的最大深度
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
}

// 判断是否为平衡二叉树
public boolean isBalanced(TreeNode root) {
    return height(root) != -1;
}

private int height(TreeNode node) {
    if (node == null) return 0;
    int l = height(node.left);
    int r = height(node.right);
    if (l == -1 || r == -1 || Math.abs(l - r) > 1) return -1;
    return 1 + Math.max(l, r);
}
```

---

## 二、图（Graph）

### 1. 图的表示

```java
// 邻接矩阵（适合稠密图）
int[][] matrix = new int[n][n];
matrix[u][v] = 1;  // u→v 有边

// 邻接表（适合稀疏图）
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
adj.get(u).add(v);  // u→v 有边
```

### 2. 深度优先搜索（DFS）

```java
boolean[] visited = new boolean[n];

public void dfs(List<List<Integer>> adj, int node) {
    visited[node] = true;
    System.out.print(node + " ");
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) dfs(adj, neighbor);
    }
}
```

### 3. 广度优先搜索（BFS）

```java
public void bfs(List<List<Integer>> adj, int start) {
    boolean[] visited = new boolean[adj.size()];
    Queue<Integer> queue = new LinkedList<>();
    queue.offer(start);
    visited[start] = true;
    while (!queue.isEmpty()) {
        int node = queue.poll();
        System.out.print(node + " ");
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                queue.offer(neighbor);
            }
        }
    }
}
```

### 4. Dijkstra最短路径

```java
public int[] dijkstra(int[][] graph, int src) {
    int n = graph.length;
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;
    boolean[] visited = new boolean[n];

    PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
    pq.offer(new int[]{src, 0});

    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int u = curr[0];
        if (visited[u]) continue;
        visited[u] = true;
        for (int v = 0; v < n; v++) {
            if (graph[u][v] > 0 && !visited[v]) {
                int newDist = dist[u] + graph[u][v];
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pq.offer(new int[]{v, newDist});
                }
            }
        }
    }
    return dist;
}
```

## 复杂度总结

| 算法 | 时间复杂度 | 空间复杂度 |
|------|-----------|-----------|
| BST查找/插入 | O(log n)平均, O(n)最差 | O(h) |
| DFS | O(V+E) | O(V) |
| BFS | O(V+E) | O(V) |
| Dijkstra（优先队列）| O((V+E)log V) | O(V) |
