# 数据库设计与范式

## 1. E-R模型

### 核心概念
- **实体（Entity）**：现实世界中独立存在的事物，如学生、课程
- **属性（Attribute）**：实体的特征，如学号、姓名
- **关系（Relationship）**：实体之间的联系

### 联系类型
```
1:1  一个老师只有一个工号，一个工号对应一个老师
1:N  一个班级有多个学生，一个学生只属于一个班级
M:N  一个学生选多门课，一门课被多个学生选
```

### 电商系统E-R示例
```
用户(user_id, username, email, phone)
    |  1:N
订单(order_id, user_id, total, status, created_at)
    |  1:N
订单明细(detail_id, order_id, product_id, qty, price)
    |  N:1
商品(product_id, name, price, stock, category_id)
    |  N:1
分类(category_id, name, parent_id)
```

---

## 2. 数据库范式

### 第一范式（1NF）— 原子性
**要求**：每列不可再分，不能有重复的列组。

❌ 违反1NF：
```
student_id | name | courses
1          | 张三 | Java,Python,算法  ← courses列可再分
```

✅ 满足1NF：
```
student_id | name | course
1          | 张三 | Java
1          | 张三 | Python
1          | 张三 | 算法
```

### 第二范式（2NF）— 消除部分函数依赖
**要求**：满足1NF，且非主属性完全依赖于主键（不存在部分依赖）。

❌ 违反2NF（联合主键(学号,课程号)，但姓名只依赖学号）：
```
(student_id, course_id) | student_name | score
(1, C01)                | 张三         | 90
```

✅ 满足2NF（拆分为两表）：
```
student(student_id, student_name)
score(student_id, course_id, score)
```

### 第三范式（3NF）— 消除传递依赖
**要求**：满足2NF，且非主属性不传递依赖于主键。

❌ 违反3NF（dept_name通过dept_id传递依赖student_id）：
```
student_id | name | dept_id | dept_name
1          | 张三 | D01     | 计算机系
```

✅ 满足3NF：
```
student(student_id, name, dept_id)
department(dept_id, dept_name)
```

---

## 3. 反范式化

在以下场景可以适当违反3NF以提升性能：
- 高频查询需要大量JOIN
- 数据量大，JOIN代价高
- 历史数据快照（冗余存储价格等随时间变化的数据）

```sql
-- 反范式化：订单明细冗余存储商品名称和价格（防止商品修改影响历史订单）
CREATE TABLE order_item (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    product_name VARCHAR(256) NOT NULL,   -- 冗余：商品名快照
    product_price DECIMAL(10,2) NOT NULL, -- 冗余：下单时价格快照
    quantity    INT NOT NULL,
    subtotal    DECIMAL(10,2) NOT NULL    -- 冗余：qty×price
);
```

---

## 4. 实战：学生管理系统设计

```sql
-- 完整DDL示例
CREATE TABLE department (
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(16) NOT NULL UNIQUE
) COMMENT='院系';

CREATE TABLE class_info (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id     BIGINT NOT NULL,
    class_name  VARCHAR(64) NOT NULL,
    grade       YEAR NOT NULL COMMENT '入学年份',
    FOREIGN KEY (dept_id) REFERENCES department(id)
) COMMENT='班级';

CREATE TABLE student (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no  VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(64) NOT NULL,
    class_id    BIGINT NOT NULL,
    gpa         DECIMAL(3,2) DEFAULT 0.00,
    FOREIGN KEY (class_id) REFERENCES class_info(id)
) COMMENT='学生';

CREATE TABLE course (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(16) NOT NULL UNIQUE,
    name        VARCHAR(128) NOT NULL,
    credits     TINYINT NOT NULL DEFAULT 3 COMMENT '学分',
    dept_id     BIGINT NOT NULL
) COMMENT='课程';

CREATE TABLE enrollment (  -- 选课表（M:N中间表）
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id  BIGINT NOT NULL,
    course_id   BIGINT NOT NULL,
    semester    VARCHAR(16) NOT NULL COMMENT '如2024-1',
    score       DECIMAL(4,1) DEFAULT NULL,
    UNIQUE KEY uk_stu_course_sem (student_id, course_id, semester),
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (course_id)  REFERENCES course(id)
) COMMENT='选课成绩';
```

---

## 5. 常见设计模式

### 软删除
```sql
-- 不物理删除，用deleted字段标记
ALTER TABLE student ADD COLUMN deleted TINYINT DEFAULT 0;
SELECT * FROM student WHERE deleted = 0;  -- 查询时过滤
```

### 树形结构（分类、菜单）
```sql
-- 方案1：邻接表（查询子节点需递归）
CREATE TABLE category (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,  -- 0表示根节点
    name      VARCHAR(64) NOT NULL
);

-- 方案2：路径枚举（查询快但维护成本高）
-- path: '0/1/3/7' 表示节点7的路径

-- 方案3：闭包表（最灵活，空间换时间）
CREATE TABLE category_closure (
    ancestor   BIGINT NOT NULL,
    descendant BIGINT NOT NULL,
    depth      INT NOT NULL,
    PRIMARY KEY (ancestor, descendant)
);
```

### 乐观锁（并发控制）
```sql
CREATE TABLE inventory (
    id      BIGINT PRIMARY KEY,
    stock   INT NOT NULL,
    version INT NOT NULL DEFAULT 0  -- 版本号
);

-- 更新时检查版本号，版本不匹配则更新失败（重试）
UPDATE inventory
SET stock = stock - 1, version = version + 1
WHERE id = ? AND version = ? AND stock > 0;
-- 检查affected rows: 0表示并发冲突，需要重试
```

## 知识点总结
- E-R图是数据库设计的起点，明确实体、属性、联系
- 三范式逐步消除冗余：1NF原子性→2NF消部分依赖→3NF消传递依赖
- 实际项目中常用反范式化平衡查询性能
- 软删除、树形结构、乐观锁是高频设计模式
- 外键约束保证数据完整性，但大量使用会降低写性能
