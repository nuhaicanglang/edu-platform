# 机器学习基础

## 1. 机器学习的分类

### 监督学习（Supervised Learning）
训练数据有标签，模型学习输入→输出的映射关系。

| 任务类型 | 输出 | 典型算法 | 应用场景 |
|---------|------|---------|---------|
| 分类 | 离散标签 | 决策树、SVM、随机森林 | 垃圾邮件检测、图像分类 |
| 回归 | 连续数值 | 线性回归、岭回归 | 房价预测、股价预测 |

### 无监督学习（Unsupervised Learning）
训练数据无标签，模型自动发现数据的内在结构。

| 任务类型 | 典型算法 | 应用场景 |
|---------|---------|---------|
| 聚类 | K-Means、DBSCAN、层次聚类 | 用户分群、文档聚类 |
| 降维 | PCA、t-SNE、AutoEncoder | 特征压缩、可视化 |
| 密度估计 | GMM | 异常检测 |

### 强化学习（Reinforcement Learning）
智能体在环境中通过试错学习最优策略，最大化累积奖励。
- 应用：游戏AI（AlphaGo）、机器人控制、推荐系统

## 2. 模型评估

### 分类评估指标
```
混淆矩阵：
              预测正  预测负
实际正    TP        FN
实际负    FP        TN

Accuracy  = (TP+TN) / (TP+TN+FP+FN)
Precision = TP / (TP+FP)   — 预测为正中真正为正的比例
Recall    = TP / (TP+FN)   — 实际为正中被正确预测的比例
F1        = 2 × Precision × Recall / (Precision + Recall)
```

### 回归评估指标
- **MAE**（平均绝对误差）= mean(|y - ŷ|)
- **MSE**（均方误差）= mean((y - ŷ)²)
- **RMSE**（均方根误差）= √MSE
- **R²**（决定系数）= 1 - SS_res/SS_tot，越接近1越好

### 交叉验证
```python
from sklearn.model_selection import cross_val_score
from sklearn.ensemble import RandomForestClassifier

model = RandomForestClassifier(n_estimators=100)
scores = cross_val_score(model, X, y, cv=5, scoring='accuracy')
print(f"准确率: {scores.mean():.3f} ± {scores.std():.3f}")
```

## 3. 过拟合与正则化

### 过拟合症状
- 训练集误差很低，测试集误差很高
- 模型在训练数据上表现好，泛化能力差

### 解决方法

**L1正则化（Lasso）**：添加 λΣ|wᵢ|，产生稀疏解（某些权重变为0）

**L2正则化（Ridge）**：添加 λΣwᵢ²，权重均匀变小，不产生稀疏解

```python
from sklearn.linear_model import Ridge, Lasso

ridge = Ridge(alpha=1.0)
lasso = Lasso(alpha=0.1)
```

**Dropout**（深度学习）：训练时随机丢弃一定比例的神经元

**数据增强**：旋转、翻转、裁剪图像来增加训练样本多样性

## 4. 线性回归原理

目标：找到最优参数 w 和 b，使得：ŷ = wX + b

**最小二乘法**：最小化残差平方和
```
Loss = Σ(yᵢ - ŷᵢ)² = ||y - Xw||²
解析解：w* = (XᵀX)⁻¹Xᵀy
```

**梯度下降**：
```
∂Loss/∂w = -2Xᵀ(y - Xw)
w := w - α × ∂Loss/∂w    （α为学习率）
```

```python
import numpy as np
from sklearn.linear_model import LinearRegression

X = np.array([[1,1],[1,2],[2,2],[2,3]])
y = np.dot(X, np.array([1,2])) + 3  # y = 1*x1 + 2*x2 + 3

reg = LinearRegression().fit(X, y)
print(f"系数: {reg.coef_}, 截距: {reg.intercept_}")
print(f"预测: {reg.predict([[3,5]])}")
```

## 知识点总结
- 监督学习需要标注数据，无监督学习不需要
- 分类用准确率/F1评估，回归用MAE/RMSE评估
- 交叉验证是评估模型泛化能力的标准方法
- 正则化通过惩罚大权重来防止过拟合
- 梯度下降是大多数ML算法的优化基础
