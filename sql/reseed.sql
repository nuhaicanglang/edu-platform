-- ============================================================
-- Reseed Script: clean duplicates + diverse demo data
-- ============================================================
USE edu_platform;

-- -----------------------------------------------
-- 1. Remove duplicate "Java Programming" courses
-- -----------------------------------------------
DELETE FROM assignment_submission WHERE assignment_id IN (
  SELECT id FROM assignment WHERE course_id IN (4,6,7,8,9,10,11)
);
DELETE FROM assignment WHERE course_id IN (4,6,7,8,9,10,11);

DELETE cs FROM class_student cs
  INNER JOIN class_group cg ON cs.class_id = cg.id
  WHERE cg.course_id IN (4,6,7,8,9,10,11);
DELETE FROM class_group WHERE course_id IN (4,6,7,8,9,10,11);

DELETE kc FROM knowledge_chunk kc
  INNER JOIN knowledge_document kd ON kc.document_id = kd.id
  WHERE kd.course_id IN (4,6,7,8,9,10,11);
DELETE FROM knowledge_document WHERE course_id IN (4,6,7,8,9,10,11);

DELETE FROM course_knowledge_point WHERE course_id IN (4,6,7,8,9,10,11);
DELETE FROM course WHERE id IN (4,6,7,8,9,10,11);

-- Also clean up "Java Basics HW" duplicate assignments under course 1
DELETE FROM assignment_submission WHERE assignment_id IN (
  SELECT id FROM assignment WHERE course_id=1 AND title LIKE 'Java Basics%'
);
DELETE FROM assignment WHERE course_id=1 AND title LIKE 'Java Basics%';

-- -----------------------------------------------
-- 2. Add 5 diverse new courses (teacher1 = id 8, teacher2 = id 10)
-- -----------------------------------------------
INSERT INTO course (course_name, teacher_id, description, status) VALUES
('Python数据分析', 8, '使用Python进行数据处理与分析：NumPy、Pandas、Matplotlib可视化、数据清洗与统计分析', 'active'),
('Web前端开发', 8, 'HTML5、CSS3、JavaScript ES6+、React框架、响应式设计、前后端分离开发实践', 'active'),
('数据库原理与应用', 10, '关系数据库理论、SQL语言、数据库设计范式、事务处理、索引优化与MySQL实践', 'active'),
('操作系统原理', 8, '进程管理、内存管理、文件系统、I/O管理、死锁、Linux操作系统内核原理', 'active'),
('软件工程', 10, '软件开发生命周期、需求分析、UML建模、设计模式、敏捷开发、测试与质量保证', 'active');

-- -----------------------------------------------
-- 3. Add 20 demo students (password: 123456, BCrypt加密)
-- -----------------------------------------------
INSERT IGNORE INTO sys_user (username, password, real_name, role, status) VALUES
('stu_liwei',    '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '李伟',   'student', 0),
('stu_wangfang', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '王芳',   'student', 0),
('stu_zhangmin', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '张敏',   'student', 0),
('stu_chenyu',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '陈宇',   'student', 0),
('stu_liujun',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '刘军',   'student', 0),
('stu_yanglu',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '杨露',   'student', 0),
('stu_zhaolei',  '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '赵磊',   'student', 0),
('stu_huangyue', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '黄月',   'student', 0),
('stu_zhoujie',  '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '周洁',   'student', 0),
('stu_wugang',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '吴刚',   'student', 0),
('stu_xuhao',    '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '徐浩',   'student', 0),
('stu_sunjing',  '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '孙静',   'student', 0),
('stu_maling',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '马玲',   'student', 0),
('stu_zhuting',  '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '朱婷',   'student', 0),
('stu_heping',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '何平',   'student', 0),
('stu_guojun',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '郭俊',   'student', 0),
('stu_linhua',   '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '林华',   'student', 0),
('stu_heyue',    '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '贺悦',   'student', 0),
('stu_gaofeng',  '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '高峰',   'student', 0),
('stu_luochen',  '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '罗晨',   'student', 0);

-- -----------------------------------------------
-- 4. Create classes for ALL courses (teacher1 & teacher2)
--    Use subquery to get course IDs dynamically
-- -----------------------------------------------
INSERT INTO class_group (course_id, teacher_id, class_name, student_count) VALUES
-- Original courses: reassign to teacher1 (or keep original, add new classes for teacher1)
(1, 8, 'Java程序设计-2025春季班', 18),
(2, 8, '数据结构与算法-2025春季班', 15),
(3, 8, '人工智能导论-2025春季班', 20);

-- New courses classes (using last inserted IDs)
SET @py_id   = (SELECT id FROM course WHERE course_name='Python数据分析' LIMIT 1);
SET @web_id  = (SELECT id FROM course WHERE course_name='Web前端开发' LIMIT 1);
SET @db_id   = (SELECT id FROM course WHERE course_name='数据库原理与应用' LIMIT 1);
SET @os_id   = (SELECT id FROM course WHERE course_name='操作系统原理' LIMIT 1);
SET @se_id   = (SELECT id FROM course WHERE course_name='软件工程' LIMIT 1);

INSERT INTO class_group (course_id, teacher_id, class_name, student_count) VALUES
(@py_id,  8,  'Python数据分析-2025秋季班', 16),
(@web_id, 8,  'Web前端开发-2025秋季班',    14),
(@db_id,  10, '数据库原理-2025秋季班',     17),
(@os_id,  8,  '操作系统-2025秋季班',       12),
(@se_id,  10, '软件工程-2025秋季班',       19);

-- -----------------------------------------------
-- 5. Enroll student1(9), student2(11), and new students in classes
-- -----------------------------------------------
-- Get class IDs for the new classes
SET @cls_java25  = (SELECT id FROM class_group WHERE class_name='Java程序设计-2025春季班' LIMIT 1);
SET @cls_ds25    = (SELECT id FROM class_group WHERE class_name='数据结构与算法-2025春季班' LIMIT 1);
SET @cls_ai25    = (SELECT id FROM class_group WHERE class_name='人工智能导论-2025春季班' LIMIT 1);
SET @cls_py25    = (SELECT id FROM class_group WHERE class_name='Python数据分析-2025秋季班' LIMIT 1);
SET @cls_web25   = (SELECT id FROM class_group WHERE class_name='Web前端开发-2025秋季班' LIMIT 1);
SET @cls_db25    = (SELECT id FROM class_group WHERE class_name='数据库原理-2025秋季班' LIMIT 1);
SET @cls_os25    = (SELECT id FROM class_group WHERE class_name='操作系统-2025秋季班' LIMIT 1);
SET @cls_se25    = (SELECT id FROM class_group WHERE class_name='软件工程-2025秋季班' LIMIT 1);

-- Enroll student1(9), student2(11), and existing test students in Java class
INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_java25, id FROM sys_user WHERE role='student' AND deleted=0;

-- Enroll subset in other classes
INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_ds25, id FROM sys_user WHERE role='student' AND deleted=0 LIMIT 15;

INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_ai25, id FROM sys_user WHERE role='student' AND deleted=0 LIMIT 20;

INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_py25, id FROM sys_user WHERE role='student' AND deleted=0 LIMIT 16;

INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_web25, id FROM sys_user WHERE role='student' AND deleted=0 LIMIT 14;

INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_db25, id FROM sys_user WHERE role='student' AND deleted=0 LIMIT 17;

INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_os25, id FROM sys_user WHERE role='student' AND deleted=0 LIMIT 12;

INSERT IGNORE INTO class_student (class_id, student_id)
SELECT @cls_se25, id FROM sys_user WHERE role='student' AND deleted=0 LIMIT 19;

-- Update student_count
UPDATE class_group SET student_count = (
  SELECT COUNT(*) FROM class_student WHERE class_id = class_group.id AND deleted=0
);

-- -----------------------------------------------
-- 6. Diverse assignments for all courses
-- -----------------------------------------------
-- Java程序设计 (course 1, teacher1=8)
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score) VALUES
(1, 8, 'Java基础语法练习', '完成变量定义、类型转换、流程控制等Java基础编程练习，提交.java源文件', 'code', 'published', 100),
(1, 8, '面向对象编程实践', '设计一个学生管理系统，使用继承、多态、接口等OOP特性，实现增删改查', 'code', 'published', 120),
(1, 8, '集合框架应用', '使用ArrayList、HashMap等集合类实现图书管理系统的数据存储与检索', 'code', 'published', 100),
(1, 8, '多线程编程作业', '实现一个多线程的生产者-消费者模型，使用synchronized或Lock机制', 'code', 'published', 100);

-- 数据结构与算法 (course 2, teacher1=8)
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score) VALUES
(2, 8, '链表操作实现', '实现单链表的增删改查操作，分析时间复杂度，对比数组的优劣', 'code', 'published', 100),
(2, 8, '二叉树遍历算法', '实现二叉搜索树的前序、中序、后序遍历，并对比递归与迭代实现', 'code', 'published', 100),
(2, 8, '排序算法比较实验', '实现冒泡、快速、归并、堆排序，对10万数据测试性能并绘制对比图表', 'text', 'published', 80),
(2, 8, '图的最短路径', '用Dijkstra算法求解城市交通路网的最短路径，给出实现代码与分析报告', 'code', 'published', 100);

-- 人工智能导论 (course 3, teacher1=8)
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score) VALUES
(3, 8, '机器学习概念理解', '阐述监督学习与无监督学习的区别，各举三个实际应用场景并进行分析', 'text', 'published', 60),
(3, 8, '神经网络原理报告', '描述前馈神经网络的结构、激活函数选择及反向传播算法的数学推导', 'text', 'published', 80),
(3, 8, 'AI应用案例分析', '选择一个实际AI应用（如推荐系统、目标检测），深度分析其技术路线与挑战', 'text', 'published', 100);

-- Python数据分析
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score)
SELECT @py_id, 8, title, description, type, 'published', max_score FROM (
  SELECT 'Pandas数据清洗实验' title, '使用Pandas处理缺失值、异常值、重复值，完成某城市房价数据集的清洗与统计分析' description, 'code' type, 100 max_score UNION ALL
  SELECT 'Matplotlib可视化作业', '针对给定的学生成绩数据集，绘制柱状图、折线图、散点图和热力图，并写出分析结论', 'code', 80 UNION ALL
  SELECT '数据分析综合项目', '从Kaggle下载真实数据集，完成数据探索、清洗、分析、可视化全流程，提交Jupyter Notebook', 'file', 150
) t;

-- Web前端开发
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score)
SELECT @web_id, 8, title, description, type, 'published', max_score FROM (
  SELECT 'HTML+CSS页面重构' title, '使用HTML5和CSS3还原给定的UI设计稿，要求响应式布局，兼容主流浏览器' description, 'code' type, 80 max_score UNION ALL
  SELECT 'JavaScript交互开发', '实现一个带增删改查的待办事项应用，使用原生JS操作DOM，不使用任何框架', 'code', 100 UNION ALL
  SELECT 'React组件开发实践', '使用React开发一个课程管理前端页面，包含列表、搜索、分页、表单等组件', 'code', 120
) t;

-- 数据库原理与应用
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score)
SELECT @db_id, 10, title, description, type, 'published', max_score FROM (
  SELECT 'SQL查询练习' title, '完成包含多表连接、子查询、聚合函数、窗口函数的30道SQL练习题' description, 'code' type, 100 max_score UNION ALL
  SELECT '数据库设计大作业', '为电商系统设计E-R图，完成第三范式建模，编写完整的DDL脚本和测试数据', 'file', 120 UNION ALL
  SELECT '事务与锁机制分析', '设计实验验证MySQL的四种隔离级别对并发事务的影响，提交实验报告', 'text', 80
) t;

-- 操作系统原理
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score)
SELECT @os_id, 8, title, description, type, 'published', max_score FROM (
  SELECT '进程调度算法模拟' title, '用代码模拟FCFS、SJF、RR三种进程调度算法，比较平均等待时间' description, 'code' type, 100 max_score UNION ALL
  SELECT '内存管理实验报告', '分析分页式与分段式内存管理的工作原理，对比优缺点，分析地址转换过程', 'text', 80 UNION ALL
  SELECT 'Linux系统调用实验', '使用C语言调用fork、exec、pipe等系统调用，实现一个简单的Shell解释器', 'code', 120
) t;

-- 软件工程
INSERT INTO assignment (course_id, teacher_id, title, description, type, status, max_score)
SELECT @se_id, 10, title, description, type, 'published', max_score FROM (
  SELECT '需求分析文档' title, '为"图书馆管理系统"编写完整的需求规格说明书，包含用例图、用例描述、非功能性需求' description, 'file' type, 80 max_score UNION ALL
  SELECT '设计模式应用实践', '从工厂、观察者、策略、装饰器模式中选3种，用Java实现并给出适用场景分析', 'code', 100 UNION ALL
  SELECT '软件测试计划与报告', '为给定的用户登录模块设计测试用例（边界值、等价类），执行测试并输出缺陷报告', 'text', 100
) t;

-- -----------------------------------------------
-- 7. Verify counts
-- -----------------------------------------------
SELECT '=== Final Count ===' AS info;
SELECT COUNT(*) AS total_courses FROM course WHERE deleted=0;
SELECT COUNT(*) AS total_classes FROM class_group WHERE deleted=0;
SELECT COUNT(*) AS total_students FROM sys_user WHERE role='student' AND deleted=0;
SELECT COUNT(*) AS total_assignments FROM assignment WHERE deleted=0;
SELECT id, course_name, status FROM course WHERE deleted=0 ORDER BY id;
