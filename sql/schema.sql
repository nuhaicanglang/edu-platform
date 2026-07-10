-- ============================================
-- 可嵌入式跨课程AI Agent通用架构平台 - 数据库初始化脚本
-- ============================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS edu_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE edu_platform;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(256) NOT NULL COMMENT '密码',
    real_name VARCHAR(64) DEFAULT '' COMMENT '真实姓名',
    email VARCHAR(128) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    avatar VARCHAR(512) DEFAULT '' COMMENT '头像URL',
    role VARCHAR(32) NOT NULL DEFAULT 'student' COMMENT '角色: admin/teacher/student',
    user_code VARCHAR(64) DEFAULT '' COMMENT '学号/工号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0正常 1禁用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 课程表
-- ============================================
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    course_name VARCHAR(128) NOT NULL COMMENT '课程名称',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    description TEXT COMMENT '课程描述',
    cover_url VARCHAR(512) DEFAULT '' COMMENT '封面图片URL',
    course_code VARCHAR(64) DEFAULT '' COMMENT '课程编码',
    category VARCHAR(32) DEFAULT '' COMMENT '课程类别: theory/practice/mixed',
    credit DOUBLE DEFAULT NULL COMMENT '学分',
    class_hours INT DEFAULT NULL COMMENT '学时',
    status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态: active/archived',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_teacher_status (teacher_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ============================================
-- 3. 课程知识点表
-- ============================================
DROP TABLE IF EXISTS course_knowledge_point;
CREATE TABLE course_knowledge_point (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父知识点ID',
    name VARCHAR(256) NOT NULL COMMENT '知识点名称',
    description TEXT COMMENT '知识点描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    point_code VARCHAR(64) DEFAULT '' COMMENT '知识点编码',
    difficulty INT DEFAULT NULL COMMENT '难度等级 1-5',
    level INT DEFAULT NULL COMMENT '层级',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_parent_id (parent_id),
    KEY idx_course_sort (course_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程知识点表';

-- ============================================
-- 4. 班级表
-- ============================================
DROP TABLE IF EXISTS class_group;
CREATE TABLE class_group (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '班级ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    class_name VARCHAR(128) NOT NULL COMMENT '班级名称',
    semester VARCHAR(32) DEFAULT '' COMMENT '学期',
    student_count INT NOT NULL DEFAULT 0 COMMENT '学生人数',
    status INT DEFAULT 0 COMMENT '状态: 0进行中 1已结束',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_course_teacher (course_id, teacher_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ============================================
-- 5. 班级学生关联表
-- ============================================
DROP TABLE IF EXISTS class_student;
CREATE TABLE class_student (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    join_time VARCHAR(64) DEFAULT '' COMMENT '加入时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_class_student (class_id, student_id),
    KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级学生关联表';

-- ============================================
-- 6. 作业表
-- ============================================
DROP TABLE IF EXISTS assignment;
CREATE TABLE assignment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '作业ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    class_id BIGINT DEFAULT NULL COMMENT '班级ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    title VARCHAR(256) NOT NULL COMMENT '作业标题',
    description TEXT COMMENT '作业描述/要求',
    type VARCHAR(32) DEFAULT 'text' COMMENT '作业类型: text/code/file',
    reference_answer TEXT COMMENT '参考答案',
    max_score INT DEFAULT 100 COMMENT '满分',
    deadline DATETIME DEFAULT NULL COMMENT '截止时间',
    knowledge_point_ids VARCHAR(512) DEFAULT '' COMMENT '关联知识点IDs(JSON数组)',
    ai_grading_enabled TINYINT DEFAULT 0 COMMENT '是否启用AI批改',
    attachment_url VARCHAR(512) DEFAULT '' COMMENT '作业附件URL',
    attachment_name VARCHAR(256) DEFAULT '' COMMENT '作业附件原始文件名',
    status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/published/closed',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_class_id (class_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_course_class_deleted (course_id, class_id, deleted),
    KEY idx_course_deleted (course_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

-- ============================================
-- 7. 作业提交表
-- ============================================
DROP TABLE IF EXISTS assignment_submission;
CREATE TABLE assignment_submission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    content TEXT COMMENT '提交内容',
    file_url VARCHAR(512) DEFAULT '' COMMENT '附件URL',
    score INT DEFAULT NULL COMMENT '得分',
    feedback TEXT COMMENT '教师/AI反馈',
    ai_grading_result TEXT COMMENT 'AI批改详细结果(JSON)',
    file_name VARCHAR(256) DEFAULT '' COMMENT '原始文件名',
    annotated_file_url VARCHAR(512) DEFAULT '' COMMENT '带批注文档URL',
    ai_comment TEXT COMMENT 'AI总评',
    status VARCHAR(32) NOT NULL DEFAULT 'submitted' COMMENT '状态: submitted/graded/returned',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    grade_time DATETIME DEFAULT NULL COMMENT '批改时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_assignment_id (assignment_id),
    KEY idx_student_id (student_id),
    UNIQUE KEY uk_assignment_student (assignment_id, student_id),
    KEY idx_student_deleted_score (student_id, deleted, score),
    KEY idx_assignment_deleted (assignment_id, deleted, submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交表';

-- ============================================
-- 8. 知识库文档表
-- ============================================
DROP TABLE IF EXISTS knowledge_document;
CREATE TABLE knowledge_document (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    title VARCHAR(256) NOT NULL COMMENT '文档标题',
    file_name VARCHAR(256) DEFAULT '' COMMENT '原始文件名',
    file_url VARCHAR(512) DEFAULT '' COMMENT '文件存储路径',
    file_type VARCHAR(32) DEFAULT '' COMMENT '文件类型',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    content MEDIUMTEXT COMMENT '提取的文本内容',
    status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/parsing/completed/failed',
    upload_user_id BIGINT DEFAULT NULL COMMENT '上传用户ID',
    chunk_count INT DEFAULT 0 COMMENT '分块数量',
    index_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '向量索引状态: pending/processing/ready/failed',
    index_error VARCHAR(1000) DEFAULT NULL COMMENT '最近一次索引错误摘要',
    embedding_model VARCHAR(128) DEFAULT NULL COMMENT '向量模型名称',
    embedding_dimension INT DEFAULT NULL COMMENT '向量维度',
    indexed_at DATETIME DEFAULT NULL COMMENT '最近成功索引时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_course_status (course_id, status, deleted),
    KEY idx_index_status (index_status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- ============================================
-- 9. 知识块表
-- ============================================
DROP TABLE IF EXISTS knowledge_chunk;
CREATE TABLE knowledge_chunk (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分块ID',
    document_id BIGINT NOT NULL COMMENT '文档ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    chunk_index INT NOT NULL DEFAULT 0 COMMENT '块索引',
    content TEXT NOT NULL COMMENT '块内容',
    keywords VARCHAR(512) DEFAULT '' COMMENT '关键词(逗号分隔)',
    knowledge_points VARCHAR(512) DEFAULT '' COMMENT '关联知识点(逗号分隔)',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_document_id (document_id),
    KEY idx_course_id (course_id),
    FULLTEXT KEY ft_content (content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识块表';

-- ============================================
-- 10. AI对话记录表
-- ============================================
DROP TABLE IF EXISTS ai_chat_record;
CREATE TABLE ai_chat_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT DEFAULT NULL COMMENT '课程ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    role VARCHAR(32) NOT NULL COMMENT '角色: user/assistant/system',
    content TEXT NOT NULL COMMENT '消息内容',
    model VARCHAR(64) DEFAULT '' COMMENT '使用的模型',
    tokens_used INT DEFAULT 0 COMMENT '消耗的token数',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_session_id (session_id),
    KEY idx_course_id (course_id),
    KEY idx_user_time (user_id, create_time DESC),
    KEY idx_user_deleted_time (user_id, deleted, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

-- ============================================
-- 11. 学习记录表（用于学情分析）
-- ============================================
DROP TABLE IF EXISTS learning_record;
CREATE TABLE learning_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT DEFAULT 0 COMMENT '课程ID',
    knowledge_point_id BIGINT DEFAULT NULL COMMENT '知识点ID',
    action_type VARCHAR(32) NOT NULL COMMENT '行为类型: view/practice/submit/qa',
    action_detail TEXT COMMENT '行为详情',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '得分(如有)',
    duration INT DEFAULT 0 COMMENT '时长(秒)',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_kp_id (knowledge_point_id),
    KEY idx_student_deleted_time (student_id, deleted, create_time DESC),
    KEY idx_student_course (student_id, course_id, action_type),
    KEY idx_action_time (action_type, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';
