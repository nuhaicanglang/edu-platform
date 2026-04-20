-- ============================================
-- 演示数据初始化脚本
-- ============================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
USE edu_platform;

-- 管理员用户 (密码: 123456, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, email, role, status) VALUES
('admin', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '系统管理员', 'admin@edu.com', 'admin', 0);

-- 教师用户 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, email, role, status) VALUES
('teacher1', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '张教授', 'zhang@cjlu.edu.cn', 'teacher', 0),
('teacher2', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '李教授', 'li@cjlu.edu.cn', 'teacher', 0);

-- 学生用户 (密码: 123456)
INSERT INTO sys_user (username, password, real_name, email, role, status) VALUES
('student1', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '王小明', 'wang@stu.cjlu.edu.cn', 'student', 0),
('student2', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '陈小红', 'chen@stu.cjlu.edu.cn', 'student', 0),
('student3', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '刘小华', 'liu@stu.cjlu.edu.cn', 'student', 0),
('student4', '$2a$10$3G6Yl/KAYNgCLDZiMQorceSn2/DmeYWNx3ghtISvMsUZzx12uiNnq', '赵小刚', 'zhao@stu.cjlu.edu.cn', 'student', 0);

-- 课程
INSERT INTO course (course_name, teacher_id, description) VALUES
('Java程序设计', 2, '本课程系统讲解Java语言基础、面向对象编程、集合框架、多线程、网络编程等核心知识，培养学生的编程能力和软件开发思维。'),
('数据结构与算法', 2, '本课程讲解常用数据结构（数组、链表、栈、队列、树、图、哈希表）和经典算法（排序、查找、动态规划、贪心算法），培养学生的算法设计与分析能力。'),
('人工智能导论', 3, '本课程介绍人工智能的基本概念、发展历程、核心技术（机器学习、深度学习、自然语言处理、计算机视觉）及应用场景。');

-- 知识点 - Java程序设计
INSERT INTO course_knowledge_point (course_id, parent_id, name, description, sort_order) VALUES
(1, 0, 'Java基础语法', 'Java语言的基本语法规则', 1),
(1, 0, '面向对象编程', 'OOP的核心概念和实践', 2),
(1, 0, '集合框架', 'Java集合类库的使用', 3),
(1, 0, '多线程编程', 'Java并发编程基础', 4),
(1, 1, '变量与数据类型', '基本数据类型和引用类型', 1),
(1, 1, '运算符与表达式', '算术、逻辑、位运算', 2),
(1, 1, '流程控制', 'if/switch/for/while', 3),
(1, 2, '类与对象', '类的定义、对象的创建', 1),
(1, 2, '继承与多态', '继承机制和多态性', 2),
(1, 2, '接口与抽象类', '接口定义和抽象类', 3),
(1, 3, 'List接口', 'ArrayList和LinkedList', 1),
(1, 3, 'Map接口', 'HashMap和TreeMap', 2),
(1, 3, 'Set接口', 'HashSet和TreeSet', 3);

-- 知识点 - 数据结构与算法
INSERT INTO course_knowledge_point (course_id, parent_id, name, description, sort_order) VALUES
(2, 0, '线性结构', '数组、链表、栈、队列', 1),
(2, 0, '树形结构', '二叉树、AVL树、B树', 2),
(2, 0, '图结构', '图的表示和遍历', 3),
(2, 0, '排序算法', '常见排序算法', 4),
(2, 0, '查找算法', '常见查找算法', 5);

-- 知识点 - 人工智能导论
INSERT INTO course_knowledge_point (course_id, parent_id, name, description, sort_order) VALUES
(3, 0, '机器学习基础', '监督学习、无监督学习、强化学习', 1),
(3, 0, '深度学习', '神经网络、CNN、RNN、Transformer', 2),
(3, 0, '自然语言处理', 'NLP基础技术和应用', 3),
(3, 0, '大语言模型', 'LLM原理与应用', 4);

-- 班级
INSERT INTO class_group (course_id, teacher_id, class_name, student_count) VALUES
(1, 2, 'Java程序设计-2024秋季班', 4),
(2, 2, '数据结构-2024秋季班', 3),
(3, 3, 'AI导论-2024秋季班', 4);

-- 班级学生关联
INSERT INTO class_student (class_id, student_id) VALUES
(1, 4), (1, 5), (1, 6), (1, 7),
(2, 4), (2, 5), (2, 6),
(3, 4), (3, 5), (3, 6), (3, 7);

-- 作业
INSERT INTO assignment (course_id, class_id, teacher_id, title, description, type, status) VALUES
(1, 1, 2, 'Java基础语法练习', '请完成以下Java基础语法题目，包括变量定义、类型转换和流程控制。', 'text', 'published'),
(1, 1, 2, '面向对象编程实践', '设计一个学生管理系统，要求使用类、继承、接口等OOP特性。', 'code', 'published'),
(2, 2, 2, '链表操作实现', '实现单链表的增删改查操作，并分析时间复杂度。', 'code', 'published'),
(3, 3, 3, '机器学习概念理解', '请简述监督学习和无监督学习的区别，并各举两个应用实例。', 'text', 'published');

-- 作业提交示例
INSERT INTO assignment_submission (assignment_id, student_id, content, status, score) VALUES
(1, 4, 'Java基础语法答案：\n1. int类型占4字节...\n2. 自动类型转换规则...', 'graded', 85),
(1, 5, 'Java变量定义：使用数据类型 变量名 = 值的形式...', 'submitted', NULL),
(4, 4, '监督学习需要标注数据，通过学习输入到输出的映射关系；无监督学习不需要标注数据，通过发现数据的内在结构...', 'submitted', NULL);
