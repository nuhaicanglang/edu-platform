# 常见排序算法

## 算法对比总览

| 算法 | 平均时间 | 最坏时间 | 空间 | 稳定性 |
|------|---------|---------|------|--------|
| 冒泡排序 | O(n²) | O(n²) | O(1) | 稳定 |
| 选择排序 | O(n²) | O(n²) | O(1) | 不稳定 |
| 插入排序 | O(n²) | O(n²) | O(1) | 稳定 |
| 希尔排序 | O(n log n) | O(n²) | O(1) | 不稳定 |
| 归并排序 | O(n log n) | O(n log n) | O(n) | 稳定 |
| 快速排序 | O(n log n) | O(n²) | O(log n) | 不稳定 |
| 堆排序 | O(n log n) | O(n log n) | O(1) | 不稳定 |
| 计数排序 | O(n+k) | O(n+k) | O(k) | 稳定 |

## 1. 快速排序（最常用）

```java
public void quickSort(int[] arr, int low, int high) {
    if (low < high) {
        int pivot = partition(arr, low, high);
        quickSort(arr, low, pivot - 1);
        quickSort(arr, pivot + 1, high);
    }
}

private int partition(int[] arr, int low, int high) {
    int pivot = arr[high];
    int i = low - 1;
    for (int j = low; j < high; j++) {
        if (arr[j] <= pivot) {
            i++;
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
    }
    int tmp = arr[i+1]; arr[i+1] = arr[high]; arr[high] = tmp;
    return i + 1;
}
// 快排最坏情况：数组已有序，每次pivot都是最小/最大元素
// 优化：三数取中法选pivot
```

## 2. 归并排序（稳定）

```java
public void mergeSort(int[] arr, int left, int right) {
    if (left >= right) return;
    int mid = left + (right - left) / 2;
    mergeSort(arr, left, mid);
    mergeSort(arr, mid + 1, right);
    merge(arr, left, mid, right);
}

private void merge(int[] arr, int left, int mid, int right) {
    int[] tmp = Arrays.copyOfRange(arr, left, right + 1);
    int i = 0, j = mid - left + 1, k = left;
    while (i <= mid - left && j <= right - left) {
        if (tmp[i] <= tmp[j]) arr[k++] = tmp[i++];
        else arr[k++] = tmp[j++];
    }
    while (i <= mid - left) arr[k++] = tmp[i++];
    while (j <= right - left) arr[k++] = tmp[j++];
}
```

## 3. 堆排序

```java
public void heapSort(int[] arr) {
    int n = arr.length;
    // 建堆（从最后一个非叶节点开始）
    for (int i = n / 2 - 1; i >= 0; i--) heapify(arr, n, i);
    // 逐步提取最大值
    for (int i = n - 1; i > 0; i--) {
        int tmp = arr[0]; arr[0] = arr[i]; arr[i] = tmp;
        heapify(arr, i, 0);
    }
}

private void heapify(int[] arr, int n, int i) {
    int largest = i, l = 2*i+1, r = 2*i+2;
    if (l < n && arr[l] > arr[largest]) largest = l;
    if (r < n && arr[r] > arr[largest]) largest = r;
    if (largest != i) {
        int tmp = arr[i]; arr[i] = arr[largest]; arr[largest] = tmp;
        heapify(arr, n, largest);
    }
}
```

## 4. 选择建议

- **小数据量（n<50）**：插入排序（代码简单，缓存友好）
- **通用场景**：快速排序（JDK默认，平均最快）
- **稳定性要求**：归并排序（TimSort基础，Python/Java对象排序）
- **内存受限**：堆排序（O(1)额外空间）
- **近乎有序**：插入排序（接近O(n)）
- **整数、范围小**：计数排序/基数排序（O(n)）
