# 数据可视化：Matplotlib与Seaborn

## 一、Matplotlib基础

```python
import matplotlib.pyplot as plt
import numpy as np

# 基本折线图
x = np.linspace(0, 2*np.pi, 100)
plt.figure(figsize=(10, 4))
plt.plot(x, np.sin(x), label='sin(x)', color='blue', linewidth=2)
plt.plot(x, np.cos(x), label='cos(x)', color='red', linestyle='--')
plt.xlabel('x轴'); plt.ylabel('y轴')
plt.title('三角函数图像'); plt.legend(); plt.grid(True)
plt.tight_layout(); plt.savefig('trig.png', dpi=150); plt.show()

# 柱状图
categories = ['语文', '数学', '英语', '物理', '化学']
scores     = [85, 92, 78, 88, 75]
colors     = ['#4CAF50' if s >= 85 else '#FF9800' for s in scores]
plt.figure(figsize=(8, 5))
bars = plt.bar(categories, scores, color=colors, edgecolor='white', linewidth=1.5)
for bar, score in zip(bars, scores):
    plt.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.5,
             str(score), ha='center', va='bottom', fontsize=11)
plt.ylim(0, 110); plt.title('各科成绩对比')
plt.axhline(y=85, color='red', linestyle='--', alpha=0.5, label='优秀线85')
plt.legend(); plt.show()

# 散点图
np.random.seed(42)
hours_study = np.random.normal(5, 2, 100).clip(0, 12)
exam_score  = 50 + 5*hours_study + np.random.normal(0, 5, 100)
plt.figure(figsize=(8, 5))
plt.scatter(hours_study, exam_score, alpha=0.6, c=exam_score, cmap='RdYlGn')
# 趋势线
z = np.polyfit(hours_study, exam_score, 1)
p = np.poly1d(z)
x_line = np.linspace(0, 12, 100)
plt.plot(x_line, p(x_line), 'r--', linewidth=2, label=f'趋势线 y={z[0]:.1f}x+{z[1]:.1f}')
plt.xlabel('学习时长(小时)'); plt.ylabel('考试成绩')
plt.title('学习时长与成绩的关系'); plt.legend(); plt.colorbar(label='成绩')
plt.show()

# 子图布局
fig, axes = plt.subplots(2, 2, figsize=(12, 8))
# 饼图
sizes = [35, 25, 20, 15, 5]
labels = ['Java', 'Python', 'JavaScript', 'C++', '其他']
axes[0,0].pie(sizes, labels=labels, autopct='%1.1f%%', startangle=90)
axes[0,0].set_title('编程语言使用占比')
# 直方图
data = np.random.normal(75, 15, 1000)
axes[0,1].hist(data, bins=30, color='steelblue', edgecolor='white', alpha=0.7)
axes[0,1].set_title('成绩分布直方图')
# 箱线图
data_groups = [np.random.normal(70+i*5, 10, 100) for i in range(5)]
axes[1,0].boxplot(data_groups, labels=[f'班级{i+1}' for i in range(5)])
axes[1,0].set_title('各班级成绩箱线图')
# 热力图
corr = np.random.rand(5, 5)
im = axes[1,1].imshow(corr, cmap='coolwarm', vmin=0, vmax=1)
plt.colorbar(im, ax=axes[1,1])
axes[1,1].set_title('相关性热力图')
plt.tight_layout(); plt.show()
```

## 二、Seaborn高级可视化

```python
import seaborn as sns
import pandas as pd

# 加载内置数据集
tips = sns.load_dataset('tips')

# 分组柱状图
plt.figure(figsize=(10, 5))
sns.barplot(data=tips, x='day', y='total_bill', hue='sex',
            palette='Set2', capsize=0.1)
plt.title('各日期男女账单金额对比（含置信区间）')
plt.show()

# 小提琴图（结合箱线图和核密度）
plt.figure(figsize=(10, 6))
sns.violinplot(data=tips, x='day', y='tip', hue='smoker',
               split=True, palette='muted', inner='quart')
plt.title('不同日期吸烟/非吸烟人群小费分布')
plt.show()

# 对角线图（多变量关系）
iris = sns.load_dataset('iris')
sns.pairplot(iris, hue='species', diag_kind='kde',
             plot_kws={'alpha': 0.6}, height=2.5)
plt.suptitle('鸢尾花数据集多变量关系', y=1.02)
plt.show()

# 热力图（相关矩阵）
plt.figure(figsize=(8, 6))
numeric_df = tips.select_dtypes(include='number')
sns.heatmap(numeric_df.corr(), annot=True, fmt='.2f',
            cmap='RdYlGn', center=0, square=True,
            linewidths=0.5, cbar_kws={'shrink': 0.8})
plt.title('数值变量相关性矩阵')
plt.show()
```

## 三、可视化选择指南

| 目标 | 推荐图表 | 示例 |
|------|---------|------|
| 分布形态 | 直方图、核密度图 | 成绩分布 |
| 类别比较 | 柱状图、条形图 | 各科成绩 |
| 趋势变化 | 折线图、面积图 | 股价走势 |
| 比例组成 | 饼图、堆叠柱状图 | 市场占比 |
| 相关关系 | 散点图、热力图 | 身高体重 |
| 统计分布 | 箱线图、小提琴图 | 薪资分布 |
| 多变量关系 | 气泡图、对角线图 | 数据探索 |

## 知识点总结
- plt.figure/subplot是Matplotlib基本框架，控制画布和子图
- 颜色映射(cmap)和透明度(alpha)是提升图表美观性的关键
- Seaborn基于Matplotlib，提供统计图表的高级封装
- 选择合适的图表类型是数据可视化的第一步
- savefig保存时dpi≥150保证打印质量
