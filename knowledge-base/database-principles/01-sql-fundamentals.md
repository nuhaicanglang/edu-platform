# SQL基础与进阶

## 1. DDL — 数据定义语言

```sql
-- 创建表
CREATE TABLE student (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '学生ID',
    student_no  VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    name        VARCHAR(64) NOT NULL COMMENT '姓名',
    gender      TINYINT     DEFAULT 1 COMMENT '性别 1男 2女',
    age         INT         DEFAULT NULL COMMENT '年龄',
    class_id    BIGINT      DEFAULT NULL COMMENT '班级ID',
    email       VARCHAR(128) DEFAULT '' COMMENT '邮箱',
    gpa         DECIMAL(3,2) DEFAULT 0.00 COMMENT '绩点',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_class_id (class_id),
    INDEX idx_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 修改表
ALTER TABLE student ADD COLUMN phone VARCHAR(20) DEFAULT '' COMMENT '手机号';
ALTER TABLE student MODIFY COLUMN gpa DECIMAL(4,2) DEFAULT 0.00;
ALTER TABLE student DROP COLUMN email;
ALTER TABLE student ADD INDEX idx_name (name);

-- 删除表
DROP TABLE IF EXISTS temp_table;
```

## 2. DML — 数据操作语言

```sql
-- INSERT
INSERT INTO student (student_no, name, gender, class_id, gpa)
VALUES ('S20240001', '张三', 1, 1, 3.85);

INSERT INTO student (student_no, name, gender)
VALUES ('S20240002', '李四', 1),
       ('S20240003', '王五', 1),
       ('S20240004', '赵六', 2);

-- UPDATE
UPDATE student SET gpa = 3.90, updated_at = NOW() WHERE id = 1;
UPDATE student SET class_id = 2 WHERE student_no LIKE 'S2024%' AND class_id = 1;

-- DELETE
DELETE FROM student WHERE id = 5;
DELETE FROM student WHERE gpa < 1.0 AND class_id IS NULL;

-- TRUNCATE (清空表，不可回滚)
TRUNCATE TABLE temp_log;
```

## 3. DQL — 数据查询语言

### 基础查询
```sql
SELECT id, name, gpa
FROM student
WHERE class_id = 1
  AND gpa >= 3.5
  AND gender = 1
ORDER BY gpa DESC, name ASC
LIMIT 10 OFFSET 0;
```

### 聚合函数
```sql
SELECT
    class_id,
    COUNT(*)           AS total_count,
    AVG(gpa)           AS avg_gpa,
    MAX(gpa)           AS max_gpa,
    MIN(gpa)           AS min_gpa,
    SUM(gpa)           AS sum_gpa,
    COUNT(DISTINCT gender) AS gender_types
FROM student
WHERE gpa > 0
GROUP BY class_id
HAVING AVG(gpa) >= 3.0
ORDER BY avg_gpa DESC;
```

### 多表连接
```sql
-- INNER JOIN: 只返回两表都有匹配的行
SELECT s.name, s.student_no, c.class_name, t.real_name AS teacher_name
FROM student s
INNER JOIN class_group c ON s.class_id = c.id
INNER JOIN sys_user t    ON c.teacher_id = t.id
WHERE c.course_id = 1;

-- LEFT JOIN: 保留左表所有行
SELECT s.name, a.title, sub.score
FROM student s
LEFT JOIN class_student cs  ON cs.student_id = s.id
LEFT JOIN assignment a      ON a.class_id = cs.class_id
LEFT JOIN assignment_submission sub ON sub.student_id = s.id AND sub.assignment_id = a.id
WHERE cs.class_id = 1;
```

### 子查询
```sql
-- 标量子查询
SELECT name, gpa,
       (SELECT AVG(gpa) FROM student) AS avg_gpa,
       gpa - (SELECT AVG(gpa) FROM student) AS diff
FROM student;

-- IN子查询
SELECT * FROM student
WHERE id IN (
    SELECT DISTINCT student_id FROM assignment_submission WHERE score >= 90
);

-- EXISTS子查询（性能更好）
SELECT * FROM student s
WHERE EXISTS (
    SELECT 1 FROM assignment_submission sub
    WHERE sub.student_id = s.id AND sub.score >= 90
);
```

### 窗口函数（MySQL 8.0+）
```sql
SELECT
    name, class_id, gpa,
    RANK()       OVER (PARTITION BY class_id ORDER BY gpa DESC) AS rank_in_class,
    DENSE_RANK() OVER (PARTITION BY class_id ORDER BY gpa DESC) AS dense_rank,
    ROW_NUMBER() OVER (PARTITION BY class_id ORDER BY gpa DESC) AS row_num,
    AVG(gpa)     OVER (PARTITION BY class_id) AS class_avg,
    gpa - AVG(gpa) OVER (PARTITION BY class_id) AS vs_avg,
    LEAD(name, 1)  OVER (PARTITION BY class_id ORDER BY gpa DESC) AS next_student,
    LAG(name, 1)   OVER (PARTITION BY class_id ORDER BY gpa DESC) AS prev_student
FROM student;
```

## 4. 索引优化

```sql
-- 查看执行计划
EXPLAIN SELECT * FROM student WHERE name = '张三';
-- 关注 type: ALL(全表扫描) < index < range < ref < eq_ref < const

-- 创建索引
CREATE INDEX idx_gpa ON student(gpa);
CREATE UNIQUE INDEX uk_email ON student(email);
CREATE INDEX idx_class_gpa ON student(class_id, gpa);  -- 复合索引

-- 索引失效情况：
-- 1. 对索引列做运算: WHERE YEAR(created_at) = 2024  ← 失效
--    改为: WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01'
-- 2. 前缀模糊: WHERE name LIKE '%张'   ← 失效
--    改为: WHERE name LIKE '张%'        ← 有效
-- 3. 类型不匹配: WHERE student_no = 20240001  ← varchar列用整数
-- 4. 复合索引不符合最左原则: (class_id, gpa) 索引，只用gpa查询失效
```

## 5. 事务（ACID）
```sql
START TRANSACTION;

UPDATE accounts SET balance = balance - 1000 WHERE id = 1;
UPDATE accounts SET balance = balance + 1000 WHERE id = 2;

-- 检查结果
SELECT balance FROM accounts WHERE id IN (1, 2);

COMMIT;    -- 或 ROLLBACK;

-- 隔离级别
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
-- READ UNCOMMITTED → READ COMMITTED → REPEATABLE READ(默认) → SERIALIZABLE
```

## 知识点总结
- SELECT执行顺序：FROM→JOIN→WHERE→GROUP BY→HAVING→SELECT→ORDER BY→LIMIT
- 聚合函数不能出现在WHERE中，要用HAVING
- 左连接保留左表所有行，右表无匹配时为NULL
- 窗口函数不减少行数，在GROUP BY之后执行
- 索引加速查询但会降低写操作性能，选择性高的列适合建索引
