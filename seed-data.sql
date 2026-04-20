-- ============================================================
-- 学情分析测试数据：为每个学生在主要课程插入作业提交及AI批改结果
-- 课程: Java程序设计(1), 数据结构与算法(2), 数据库原理与应用(15)
-- ============================================================

SET NAMES utf8mb4;

-- 删除旧的冲突数据（保留id<=14的真实数据）
DELETE FROM assignment_submission WHERE id > 14;

-- ============================================================
-- 帮助宏：student_profile
-- 学生分组：
--   优秀(4,12,14): 分数85-95, 全部提交
--   中等(5,13,15,17,19): 分数70-85, 基本提交
--   普通(6,11,16,18,20): 分数60-75, 部分提交
--   薄弱(7,9): 分数45-65, 少量提交，成绩下降趋势
-- ============================================================

-- ============================================================
-- Java程序设计(course_id=1) 作业: 1,2,12,13,14,15,48
-- ============================================================

-- 王小明(4) 优秀学生 - 稳定高分
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,4,'Java基础语法作业内容',92,'completed','{"score":92,"overallComment":"代码规范，逻辑清晰"}','代码规范，逻辑清晰，基础掌握扎实',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,4,'面向对象编程实践内容',89,'completed','{"score":89,"overallComment":"封装继承多态理解到位"}','封装继承多态理解到位，实践能力强',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,4,'Java基础语法练习2内容',91,'completed','{"score":91,"overallComment":"语法掌握熟练"}','语法掌握熟练，代码质量高',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,4,'面向对象编程实践2内容',94,'completed','{"score":94,"overallComment":"设计模式应用合理"}','设计模式应用合理，面向对象思想扎实',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,4,'集合框架应用内容',88,'completed','{"score":88,"overallComment":"集合使用熟练"}','集合框架使用熟练，泛型理解正确',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY),
(15,4,'多线程编程作业内容',93,'completed','{"score":93,"overallComment":"线程同步处理正确"}','线程同步处理正确，并发理解深入',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY),
(48,4,'Fix Test内容',90,'completed','{"score":90,"overallComment":"综合能力强"}','综合能力强，代码质量稳定',NOW()-INTERVAL 7 DAY,NOW()-INTERVAL 6 DAY);

-- 陈小红(5) 中等学生 - 成绩波动
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,5,'Java基础语法作业',78,'completed','{"score":78,"overallComment":"基础尚可，细节需加强"}','基础尚可，部分细节需要加强',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,5,'面向对象编程内容',72,'completed','{"score":72,"overallComment":"继承理解不够深入"}','继承理解不够深入，需多练习',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,5,'Java基础语法2',80,'completed','{"score":80,"overallComment":"有所进步"}','有所进步，基础知识掌握趋于稳定',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,5,'面向对象编程2',75,'completed','{"score":75,"overallComment":"多态概念模糊"}','多态概念仍有模糊，建议复习',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,5,'集合框架应用',82,'completed','{"score":82,"overallComment":"集合操作基本正确"}','集合操作基本正确，有进步',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY),
(15,5,'多线程编程',68,'completed','{"score":68,"overallComment":"线程安全问题理解不足"}','线程安全问题理解不足，需加强',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY);

-- 刘小华(6) 普通学生 - 分数偏低，部分未交
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,6,'Java基础语法',65,'completed','{"score":65,"overallComment":"基础薄弱，需补充"}','基础薄弱，建议补充基础知识',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,6,'面向对象编程',70,'completed','{"score":70,"overallComment":"勉强达标"}','勉强达标，面向对象概念需强化',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,6,'Java基础语法2',62,'completed','{"score":62,"overallComment":"退步明显，需警惕"}','有所退步，需要重视',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,6,'面向对象编程2',58,'completed','{"score":58,"overallComment":"不及格，需补考"}','不及格，核心概念掌握不足',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(15,6,'多线程编程',61,'completed','{"score":61,"overallComment":"勉强及格"}','勉强及格，继续努力',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY);

-- 赵小刚(7) 薄弱学生 - 成绩持续下降，缺交
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,7,'Java基础语法',72,'completed','{"score":72,"overallComment":"开始尚可"}','开始尚可，需保持',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,7,'面向对象编程',65,'completed','{"score":65,"overallComment":"开始下滑"}','成绩开始下滑，需关注',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,7,'Java基础语法2',58,'completed','{"score":58,"overallComment":"继续下降，不及格"}','继续下降，出现不及格风险',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,7,'面向对象编程2',52,'completed','{"score":52,"overallComment":"严重下降，需干预"}','严重下降，建议立即干预',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY);

-- Li Student(9) 薄弱-中等，部分课程已有数据保留

-- Wang Student(11) 中等学生
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,11,'Java基础语法',76,'completed','{"score":76,"overallComment":"基础稳定"}','基础稳定，保持即可',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,11,'面向对象编程',79,'completed','{"score":79,"overallComment":"理解较好"}','理解较好，继续加油',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,11,'Java基础语法2',74,'completed','{"score":74,"overallComment":"小幅波动"}','小幅波动，整体稳定',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,11,'面向对象编程2',81,'completed','{"score":81,"overallComment":"有所提升"}','有所提升，继续保持',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,11,'集合框架',77,'completed','{"score":77,"overallComment":"集合理解良好"}','集合理解良好',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY),
(15,11,'多线程',73,'completed','{"score":73,"overallComment":"基本掌握"}','基本掌握多线程概念',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY),
(48,11,'综合作业',78,'completed','{"score":78,"overallComment":"整体发挥稳定"}','整体发挥稳定',NOW()-INTERVAL 7 DAY,NOW()-INTERVAL 6 DAY);

-- 李伟(12) 优秀学生
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,12,'Java基础语法',88,'completed','{"score":88,"overallComment":"掌握扎实"}','掌握扎实，代码风格好',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,12,'面向对象编程',91,'completed','{"score":91,"overallComment":"设计合理"}','设计合理，展现出良好的面向对象思想',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,12,'Java基础语法2',87,'completed','{"score":87,"overallComment":"稳定优秀"}','稳定优秀',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,12,'面向对象编程2',93,'completed','{"score":93,"overallComment":"非常出色"}','非常出色，建议参加竞赛',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,12,'集合框架',90,'completed','{"score":90,"overallComment":"集合运用灵活"}','集合运用灵活',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY),
(15,12,'多线程',89,'completed','{"score":89,"overallComment":"并发理解深入"}','并发理解深入',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY),
(48,12,'综合作业',92,'completed','{"score":92,"overallComment":"综合能力突出"}','综合能力突出',NOW()-INTERVAL 7 DAY,NOW()-INTERVAL 6 DAY);

-- 王芳(13) 中等偏上
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,13,'Java基础语法',82,'completed','{"score":82,"overallComment":"良好"}','良好，基础扎实',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,13,'面向对象编程',78,'completed','{"score":78,"overallComment":"理解较好"}','理解较好',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,13,'Java基础语法2',84,'completed','{"score":84,"overallComment":"进步明显"}','进步明显，保持势头',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,13,'面向对象编程2',80,'completed','{"score":80,"overallComment":"稳中有进"}','稳中有进',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,13,'集合框架',83,'completed','{"score":83,"overallComment":"集合掌握良好"}','集合掌握良好',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY),
(15,13,'多线程',79,'completed','{"score":79,"overallComment":"线程概念理解到位"}','线程概念理解到位',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY);

-- 张敏(14) 优秀学生 - 持续上升
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,14,'Java基础语法',80,'completed','{"score":80,"overallComment":"起点良好"}','起点良好，有潜力',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,14,'面向对象编程',84,'completed','{"score":84,"overallComment":"进步明显"}','进步明显',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,14,'Java基础语法2',87,'completed','{"score":87,"overallComment":"持续上升"}','持续上升，态度积极',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,14,'面向对象编程2',90,'completed','{"score":90,"overallComment":"优秀，成长很快"}','优秀，成长很快',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,14,'集合框架',92,'completed','{"score":92,"overallComment":"表现突出"}','表现突出',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY),
(15,14,'多线程',94,'completed','{"score":94,"overallComment":"优异"}','优异表现',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY),
(48,14,'综合作业',95,'completed','{"score":95,"overallComment":"近乎满分，优秀"}','近乎满分，非常优秀',NOW()-INTERVAL 7 DAY,NOW()-INTERVAL 6 DAY);

-- 陈宇(15) 中等，缺交几次
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,15,'Java基础语法',75,'completed','{"score":75,"overallComment":"中规中矩"}','中规中矩',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,15,'面向对象编程',71,'completed','{"score":71,"overallComment":"理解一般"}','理解一般，需加强',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(13,15,'面向对象编程2',69,'completed','{"score":69,"overallComment":"勉强及格"}','勉强及格',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(15,15,'多线程',66,'completed','{"score":66,"overallComment":"线程理解较弱"}','线程理解较弱，需补习',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY);

-- 刘军(16) 普通，成绩起伏
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,16,'Java基础语法',68,'completed','{"score":68,"overallComment":"基础薄弱"}','基础薄弱',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,16,'面向对象编程',74,'completed','{"score":74,"overallComment":"有改善"}','有改善',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,16,'Java基础语法2',70,'completed','{"score":70,"overallComment":"基本及格"}','基本及格',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,16,'面向对象编程2',65,'completed','{"score":65,"overallComment":"有所下滑"}','有所下滑，需重视',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,16,'集合框架',72,'completed','{"score":72,"overallComment":"集合使用基本正确"}','集合使用基本正确',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY);

-- 杨露(17) 中等偏上，稳定
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,17,'Java基础语法',81,'completed','{"score":81,"overallComment":"稳定良好"}','稳定良好',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,17,'面向对象编程',83,'completed','{"score":83,"overallComment":"面向对象掌握好"}','面向对象掌握好',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,17,'Java基础语法2',80,'completed','{"score":80,"overallComment":"保持稳定"}','保持稳定',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,17,'面向对象编程2',84,'completed','{"score":84,"overallComment":"小有进步"}','小有进步',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(14,17,'集合框架',82,'completed','{"score":82,"overallComment":"集合掌握良好"}','集合掌握良好',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY),
(15,17,'多线程',85,'completed','{"score":85,"overallComment":"多线程理解较好"}','多线程理解较好',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY);

-- 赵磊(18) 普通
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,18,'Java基础语法',66,'completed','{"score":66,"overallComment":"基础一般"}','基础一般',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,18,'面向对象编程',63,'completed','{"score":63,"overallComment":"理解较弱"}','理解较弱',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,18,'Java基础语法2',70,'completed','{"score":70,"overallComment":"有改善"}','有改善',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(14,18,'集合框架',67,'completed','{"score":67,"overallComment":"集合使用不够灵活"}','集合使用不够灵活',NOW()-INTERVAL 22 DAY,NOW()-INTERVAL 21 DAY);

-- 黄月(19) 中等
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,19,'Java基础语法',77,'completed','{"score":77,"overallComment":"良好"}','良好',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,19,'面向对象编程',80,'completed','{"score":80,"overallComment":"理解到位"}','理解到位',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,19,'Java基础语法2',75,'completed','{"score":75,"overallComment":"稳定"}','稳定',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,19,'面向对象编程2',78,'completed','{"score":78,"overallComment":"表现稳定"}','表现稳定',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY),
(15,19,'多线程',74,'completed','{"score":74,"overallComment":"基本掌握"}','基本掌握',NOW()-INTERVAL 15 DAY,NOW()-INTERVAL 14 DAY);

-- 周洁(20) 普通
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(1,20,'Java基础语法',71,'completed','{"score":71,"overallComment":"基本及格"}','基本及格',NOW()-INTERVAL 50 DAY,NOW()-INTERVAL 49 DAY),
(2,20,'面向对象编程',68,'completed','{"score":68,"overallComment":"理解有限"}','理解有限，多加练习',NOW()-INTERVAL 43 DAY,NOW()-INTERVAL 42 DAY),
(12,20,'Java基础语法2',73,'completed','{"score":73,"overallComment":"稍有进步"}','稍有进步',NOW()-INTERVAL 36 DAY,NOW()-INTERVAL 35 DAY),
(13,20,'面向对象编程2',69,'completed','{"score":69,"overallComment":"勉强及格"}','勉强及格',NOW()-INTERVAL 29 DAY,NOW()-INTERVAL 28 DAY);

-- ============================================================
-- 数据结构与算法(course_id=2) 作业: 3,16,17,18,19
-- ============================================================

-- 王小明(4)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,4,'链表操作',91,'completed','{"score":91,"overallComment":"链表操作熟练"}','链表操作熟练',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(16,4,'链表操作实现2',89,'completed','{"score":89,"overallComment":"实现完整正确"}','实现完整正确',NOW()-INTERVAL 40 DAY,NOW()-INTERVAL 39 DAY),
(17,4,'二叉树遍历',93,'completed','{"score":93,"overallComment":"遍历算法掌握扎实"}','遍历算法掌握扎实',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,4,'排序算法',90,'completed','{"score":90,"overallComment":"多种排序实现正确"}','多种排序实现正确',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY),
(19,4,'图最短路径',88,'completed','{"score":88,"overallComment":"Dijkstra实现正确"}','Dijkstra实现正确',NOW()-INTERVAL 16 DAY,NOW()-INTERVAL 15 DAY);

-- 陈小红(5)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,5,'链表操作',74,'completed','{"score":74,"overallComment":"基本正确"}','基本正确',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(16,5,'链表操作实现2',71,'completed','{"score":71,"overallComment":"有待改进"}','有待改进',NOW()-INTERVAL 40 DAY,NOW()-INTERVAL 39 DAY),
(17,5,'二叉树遍历',78,'completed','{"score":78,"overallComment":"递归理解到位"}','递归理解到位',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,5,'排序算法',75,'completed','{"score":75,"overallComment":"主要排序掌握"}','主要排序掌握',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY);

-- 刘小华(6)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,6,'链表操作',60,'completed','{"score":60,"overallComment":"勉强及格"}','勉强及格，需强化',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(17,6,'二叉树遍历',58,'completed','{"score":58,"overallComment":"递归理解不足"}','递归理解不足',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,6,'排序算法',63,'completed','{"score":63,"overallComment":"仅掌握简单排序"}','仅掌握简单排序',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY);

-- 赵小刚(7)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,7,'链表操作',55,'completed','{"score":55,"overallComment":"链表操作错误较多"}','链表操作错误较多',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(16,7,'链表操作实现2',50,'completed','{"score":50,"overallComment":"未及格，问题严重"}','未及格，需要重点辅导',NOW()-INTERVAL 40 DAY,NOW()-INTERVAL 39 DAY);

-- Li Student(9)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,9,'链表操作',69,'completed','{"score":69,"overallComment":"基本掌握"}','基本掌握',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(17,9,'二叉树遍历',72,'completed','{"score":72,"overallComment":"递归基本正确"}','递归基本正确',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,9,'排序算法',65,'completed','{"score":65,"overallComment":"排序理解一般"}','排序理解一般',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY);

-- Wang Student(11)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,11,'链表操作',77,'completed','{"score":77,"overallComment":"良好"}','良好',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(16,11,'链表操作实现2',74,'completed','{"score":74,"overallComment":"实现基本正确"}','实现基本正确',NOW()-INTERVAL 40 DAY,NOW()-INTERVAL 39 DAY),
(17,11,'二叉树遍历',80,'completed','{"score":80,"overallComment":"理解到位"}','理解到位',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,11,'排序算法',76,'completed','{"score":76,"overallComment":"主要排序实现正确"}','主要排序实现正确',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY),
(19,11,'图最短路径',73,'completed','{"score":73,"overallComment":"基本思路正确"}','基本思路正确',NOW()-INTERVAL 16 DAY,NOW()-INTERVAL 15 DAY);

-- 李伟(12)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,12,'链表操作',90,'completed','{"score":90,"overallComment":"优秀"}','优秀实现',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(16,12,'链表操作实现2',88,'completed','{"score":88,"overallComment":"代码简洁高效"}','代码简洁高效',NOW()-INTERVAL 40 DAY,NOW()-INTERVAL 39 DAY),
(17,12,'二叉树遍历',92,'completed','{"score":92,"overallComment":"三种遍历均正确"}','三种遍历均正确',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,12,'排序算法',91,'completed','{"score":91,"overallComment":"算法分析到位"}','算法分析到位',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY),
(19,12,'图最短路径',89,'completed','{"score":89,"overallComment":"图算法掌握扎实"}','图算法掌握扎实',NOW()-INTERVAL 16 DAY,NOW()-INTERVAL 15 DAY);

-- 王芳(13)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,13,'链表操作',83,'completed','{"score":83,"overallComment":"良好"}','良好',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(16,13,'链表操作实现2',80,'completed','{"score":80,"overallComment":"实现正确"}','实现正确',NOW()-INTERVAL 40 DAY,NOW()-INTERVAL 39 DAY),
(17,13,'二叉树遍历',82,'completed','{"score":82,"overallComment":"遍历正确"}','遍历正确',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,13,'排序算法',79,'completed','{"score":79,"overallComment":"排序掌握较好"}','排序掌握较好',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY);

-- 张敏(14) 持续上升
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(3,14,'链表操作',82,'completed','{"score":82,"overallComment":"表现良好"}','表现良好',NOW()-INTERVAL 48 DAY,NOW()-INTERVAL 47 DAY),
(16,14,'链表操作实现2',86,'completed','{"score":86,"overallComment":"进步明显"}','进步明显',NOW()-INTERVAL 40 DAY,NOW()-INTERVAL 39 DAY),
(17,14,'二叉树遍历',89,'completed','{"score":89,"overallComment":"遍历理解深刻"}','遍历理解深刻',NOW()-INTERVAL 32 DAY,NOW()-INTERVAL 31 DAY),
(18,14,'排序算法',91,'completed','{"score":91,"overallComment":"算法优秀"}','算法优秀',NOW()-INTERVAL 24 DAY,NOW()-INTERVAL 23 DAY),
(19,14,'图最短路径',93,'completed','{"score":93,"overallComment":"图算法掌握出色"}','图算法掌握出色',NOW()-INTERVAL 16 DAY,NOW()-INTERVAL 15 DAY);

-- ============================================================
-- 数据库原理与应用(course_id=15) 作业: 29,30,31,47
-- ============================================================

-- 王小明(4)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(29,4,'SQL查询练习',88,'completed','{"score":88,"overallComment":"SQL熟练"}','SQL熟练，查询优化意识好',NOW()-INTERVAL 46 DAY,NOW()-INTERVAL 45 DAY),
(30,4,'数据库设计',85,'completed','{"score":85,"overallComment":"设计规范"}','设计规范，范式理解正确',NOW()-INTERVAL 38 DAY,NOW()-INTERVAL 37 DAY),
(31,4,'事务与锁',87,'completed','{"score":87,"overallComment":"事务理解到位"}','事务理解到位',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- 陈小红(5)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(29,5,'SQL查询练习',74,'completed','{"score":74,"overallComment":"基本SQL掌握"}','基本SQL掌握',NOW()-INTERVAL 46 DAY,NOW()-INTERVAL 45 DAY),
(30,5,'数据库设计',70,'completed','{"score":70,"overallComment":"范式理解有误"}','范式理解有误，需补充',NOW()-INTERVAL 38 DAY,NOW()-INTERVAL 37 DAY),
(31,5,'事务与锁',72,'completed','{"score":72,"overallComment":"事务概念基本清楚"}','事务概念基本清楚',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- 刘小华(6)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(29,6,'SQL查询练习',62,'completed','{"score":62,"overallComment":"SQL基础薄弱"}','SQL基础薄弱',NOW()-INTERVAL 46 DAY,NOW()-INTERVAL 45 DAY),
(31,6,'事务与锁',58,'completed','{"score":58,"overallComment":"事务理解不足"}','事务理解不足，需重点补习',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- Li Student(9)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(30,9,'数据库设计',68,'completed','{"score":68,"overallComment":"设计基本合理"}','设计基本合理',NOW()-INTERVAL 38 DAY,NOW()-INTERVAL 37 DAY),
(31,9,'事务与锁',71,'completed','{"score":71,"overallComment":"事务理解基本正确"}','事务理解基本正确',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- Wang Student(11)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(29,11,'SQL查询练习',79,'completed','{"score":79,"overallComment":"SQL掌握良好"}','SQL掌握良好',NOW()-INTERVAL 46 DAY,NOW()-INTERVAL 45 DAY),
(30,11,'数据库设计',76,'completed','{"score":76,"overallComment":"设计较规范"}','设计较规范',NOW()-INTERVAL 38 DAY,NOW()-INTERVAL 37 DAY),
(31,11,'事务与锁',74,'completed','{"score":74,"overallComment":"事务基本理解"}','事务基本理解',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- 李伟(12)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(29,12,'SQL查询练习',91,'completed','{"score":91,"overallComment":"SQL高级查询熟练"}','SQL高级查询熟练',NOW()-INTERVAL 46 DAY,NOW()-INTERVAL 45 DAY),
(30,12,'数据库设计',89,'completed','{"score":89,"overallComment":"设计优秀"}','设计优秀，符合第三范式',NOW()-INTERVAL 38 DAY,NOW()-INTERVAL 37 DAY),
(31,12,'事务与锁',88,'completed','{"score":88,"overallComment":"并发控制理解深刻"}','并发控制理解深刻',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- 张敏(14)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(29,14,'SQL查询练习',85,'completed','{"score":85,"overallComment":"SQL掌握扎实"}','SQL掌握扎实',NOW()-INTERVAL 46 DAY,NOW()-INTERVAL 45 DAY),
(30,14,'数据库设计',88,'completed','{"score":88,"overallComment":"设计合理规范"}','设计合理规范',NOW()-INTERVAL 38 DAY,NOW()-INTERVAL 37 DAY),
(31,14,'事务与锁',90,'completed','{"score":90,"overallComment":"事务机制理解深入"}','事务机制理解深入',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- 杨露(17)
INSERT IGNORE INTO assignment_submission (assignment_id,student_id,content,score,status,ai_grading_result,ai_comment,submit_time,grade_time) VALUES
(29,17,'SQL查询练习',80,'completed','{"score":80,"overallComment":"SQL良好"}','SQL良好',NOW()-INTERVAL 46 DAY,NOW()-INTERVAL 45 DAY),
(30,17,'数据库设计',78,'completed','{"score":78,"overallComment":"设计基本合理"}','设计基本合理',NOW()-INTERVAL 38 DAY,NOW()-INTERVAL 37 DAY),
(31,17,'事务与锁',82,'completed','{"score":82,"overallComment":"事务理解较好"}','事务理解较好',NOW()-INTERVAL 20 DAY,NOW()-INTERVAL 19 DAY);

-- ============================================================
-- 验证
-- ============================================================
SELECT '=== 数据插入完成 ===' AS info;
SELECT course_name, COUNT(DISTINCT s.student_id) AS 学生数, COUNT(*) AS 提交总数, ROUND(AVG(s.score),1) AS 平均分
FROM assignment_submission s
JOIN assignment a ON s.assignment_id=a.id
JOIN course c ON a.course_id=c.id
WHERE s.score IS NOT NULL
GROUP BY course_name;
