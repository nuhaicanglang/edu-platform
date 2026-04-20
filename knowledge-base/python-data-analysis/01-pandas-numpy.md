# Python数据分析：NumPy与Pandas

## 一、NumPy

### 1. 数组创建与操作
```python
import numpy as np

# 创建数组
a = np.array([1, 2, 3, 4, 5])
b = np.zeros((3, 4))           # 3行4列全零矩阵
c = np.ones((2, 3))
d = np.arange(0, 10, 2)        # [0,2,4,6,8]
e = np.linspace(0, 1, 5)       # [0, 0.25, 0.5, 0.75, 1.0]
f = np.random.randn(3, 3)      # 3×3标准正态随机矩阵

# 数组属性
print(a.shape)   # (5,)
print(b.shape)   # (3, 4)
print(a.dtype)   # int64
print(a.ndim)    # 1

# 索引与切片
arr = np.arange(12).reshape(3, 4)
print(arr[1, 2])      # 第2行第3列 = 6
print(arr[:, 1])      # 第2列 [1,5,9]
print(arr[0:2, 1:3])  # 子矩阵
```

### 2. 数学运算
```python
x = np.array([1, 2, 3, 4])
print(np.sum(x))          # 10
print(np.mean(x))         # 2.5
print(np.std(x))          # 标准差
print(np.max(x), np.min(x))

# 矩阵运算
A = np.array([[1,2],[3,4]])
B = np.array([[5,6],[7,8]])
print(A @ B)              # 矩阵乘法
print(np.dot(A, B))       # 等价
print(A.T)                # 转置
print(np.linalg.inv(A))   # 逆矩阵
print(np.linalg.det(A))   # 行列式
```

---

## 二、Pandas

### 1. Series与DataFrame
```python
import pandas as pd

# Series — 带标签的一维数组
s = pd.Series([90, 85, 78, 92], index=['张三', '李四', '王五', '赵六'])
print(s['张三'])   # 90
print(s[s > 85])   # 筛选

# DataFrame — 二维表格
df = pd.DataFrame({
    'name': ['张三', '李四', '王五', '赵六'],
    'age':  [20, 21, 19, 22],
    'score':[90, 85, 78, 92],
    'grade':['A', 'B', 'C', 'A']
})

print(df.head(3))          # 前3行
print(df.shape)            # (4, 4)
print(df.dtypes)           # 各列数据类型
print(df.describe())       # 统计摘要
```

### 2. 数据筛选与排序
```python
# 行列选取
df['name']                           # 选取列
df[['name', 'score']]                # 选取多列
df.loc[0]                            # 按标签选行
df.iloc[0:2]                         # 按位置选行
df.loc[df['score'] > 85]             # 条件筛选

# 多条件筛选
high_score = df[(df['score'] >= 85) & (df['grade'] == 'A')]

# 排序
df.sort_values('score', ascending=False)
df.sort_values(['grade', 'score'], ascending=[True, False])
```

### 3. 数据清洗
```python
# 缺失值处理
df.isnull().sum()                    # 每列缺失数量
df.dropna()                          # 删除含NaN的行
df.fillna(df.mean(numeric_only=True)) # 用均值填充
df['score'].fillna(df['score'].median(), inplace=True)

# 重复值
df.duplicated().sum()               # 重复行数量
df.drop_duplicates()                # 删除重复行

# 数据类型转换
df['age'] = df['age'].astype(int)
df['date'] = pd.to_datetime(df['date'])
```

### 4. 分组聚合
```python
# groupby
result = df.groupby('grade')['score'].agg(['mean', 'max', 'min', 'count'])
print(result)

# pivot_table
pivot = df.pivot_table(values='score', index='grade', aggfunc='mean')

# 自定义聚合
df.groupby('grade').agg({
    'score': ['mean', 'std'],
    'age': 'mean'
})
```

### 5. 数据合并
```python
# concat — 拼接
df_all = pd.concat([df1, df2], axis=0, ignore_index=True)

# merge — 类似SQL JOIN
merged = pd.merge(df_students, df_scores, on='student_id', how='left')

# 宽转长（melt）
long_df = df.melt(id_vars=['name'], value_vars=['math', 'english'], 
                   var_name='subject', value_name='score')
```

### 6. 读写文件
```python
# 读取
df = pd.read_csv('data.csv', encoding='utf-8')
df = pd.read_excel('data.xlsx', sheet_name='Sheet1')

# 写出
df.to_csv('output.csv', index=False, encoding='utf-8-sig')  # utf-8-sig避免乱码
df.to_excel('output.xlsx', index=False)
```

## 知识点总结
- NumPy提供高性能多维数组和数学运算，是Pandas的底层基础
- Pandas DataFrame类似Excel表格，支持灵活的数据操作
- 数据清洗是数据分析中最耗时的步骤，占70%以上工作量
- groupby+agg是数据聚合分析的核心方法
- merge/concat是多表关联的标准方式
