package com.eduplatform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.exception.BusinessException;
import com.eduplatform.common.utils.RedisCacheService;
import com.eduplatform.system.domain.entity.ClassGroup;
import com.eduplatform.system.domain.entity.ClassStudent;
import com.eduplatform.system.mapper.ClassGroupMapper;
import com.eduplatform.system.mapper.ClassStudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 班级业务服务
 * <p>
 * 班级详情通过 {@link RedisCacheService#getOrLoadWithLock} 缓存。
 * 添加学生使用 SELECT FOR UPDATE 悲观锁防止并发重复插入（check-then-act 竞态）。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ClassGroupService {

    private final ClassGroupMapper classGroupMapper;
    private final ClassStudentMapper classStudentMapper;
    private final JdbcTemplate jdbc;
    private final RedisCacheService cache;

    private static final String KEY_CLASS  = "class:id:";
    private static final long   TTL        = 300;  // 5 分钟基础
    private static final int    JITTER     = 60;   // ±1 分钟随机（防雪崩）

    /** 分页查询班级（支持课程ID、教师ID过滤） */
    public PageResult<ClassGroup> page(int pageNum, int pageSize, Long courseId, Long teacherId) {
        Page<ClassGroup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ClassGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(courseId != null, ClassGroup::getCourseId, courseId);
        wrapper.eq(teacherId != null, ClassGroup::getTeacherId, teacherId);
        wrapper.orderByDesc(ClassGroup::getCreateTime);
        Page<ClassGroup> result = classGroupMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    /** 根据 ID 查询班级详情（带互斥锁缓存） */
    public ClassGroup getById(Long id) {
        // 互斥锁防击穿 + 空值防穿透
        ClassGroup cg = cache.getOrLoadWithLock(
                KEY_CLASS + id,
                () -> classGroupMapper.selectById(id),
                TTL, JITTER);
        if (cg == null) throw new BusinessException("班级不存在");
        return cg;
    }

    /** 创建班级 */
    public void create(ClassGroup classGroup) {
        classGroupMapper.insert(classGroup);
    }

    /** 更新班级（同时清除缓存） */
    public void update(ClassGroup classGroup) {
        classGroupMapper.updateById(classGroup);
        cache.evict(KEY_CLASS + classGroup.getId());
    }

    /** 删除班级（逻辑删除 + 清除缓存） */
    public void delete(Long id) {
        classGroupMapper.deleteById(id);
        cache.evict(KEY_CLASS + id);
    }

    /**
     * 添加学生到班级（事务 + 悲观锁 + 原子更新计数）
     * <p>
     * 使用 SELECT ... FOR UPDATE 锁住该行/间隙，防止两个并发请求同时通过
     * 存在性检查导致重复插入（check-then-act 竞态条件）。
     * student_count 使用原子 SQL (count = count + 1) 避免并发脏写。
     * </p>
     */
    @Transactional
    public void addStudent(Long classId, Long studentId) {
        // SELECT FOR UPDATE：悲观锁，事务结束前锁住该行/间隙，防止并发重复插入
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM class_student WHERE class_id=? AND student_id=? AND deleted=0 FOR UPDATE",
                Long.class, classId, studentId);
        if (count != null && count > 0) throw new BusinessException("该学生已在班级中");

        ClassStudent cs = new ClassStudent();
        cs.setClassId(classId);
        cs.setStudentId(studentId);
        cs.setJoinTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        classStudentMapper.insert(cs);

        // 原子更新 student_count（避免多学生并发加入时的 read-modify-write 脏写）
        jdbc.update(
                "UPDATE class_group SET student_count = COALESCE(student_count, 0) + 1 WHERE id = ?",
                classId);

        // 清除班级缓存（student_count 已变更）
        cache.evict(KEY_CLASS + classId);
    }

    /** 从班级移除学生（同步更新计数 + 清缓存） */
    @Transactional
    public void removeStudent(Long classId, Long studentId) {
        int rows = classStudentMapper.delete(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getClassId, classId)
                        .eq(ClassStudent::getStudentId, studentId));

        // 只有真的删了才减计数，避免重复请求导致计数负数
        if (rows > 0) {
            jdbc.update(
                    "UPDATE class_group SET student_count = GREATEST(COALESCE(student_count, 0) - 1, 0) WHERE id = ?",
                    classId);
            cache.evict(KEY_CLASS + classId);
        }
    }

    /** 查询班级学生列表（批量查询用户信息填充学号+姓名） */
    public List<ClassStudent> getStudents(Long classId) {
        List<ClassStudent> list = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassId, classId));
        // 批量查询用户信息（学号+姓名）
        if (!list.isEmpty()) {
            List<Long> ids = list.stream().map(ClassStudent::getStudentId).toList();
            String placeholders = String.join(",", ids.stream().map(String::valueOf).toList());
            List<Map<String, Object>> users = jdbc.queryForList(
                "SELECT id, username, real_name FROM sys_user WHERE id IN (" + placeholders + ")");
            Map<Long, Map<String, Object>> userMap = new java.util.HashMap<>();
            users.forEach(u -> userMap.put(((Number) u.get("id")).longValue(), u));
            list.forEach(cs -> {
                Map<String, Object> u = userMap.get(cs.getStudentId());
                if (u != null) {
                    cs.setUsername((String) u.get("username"));
                    cs.setRealName((String) u.get("real_name"));
                }
            });
        }
        return list;
    }

    /** 搜索不在指定班级中的学生（按学号或姓名模糊匹配） */
    public List<Map<String, Object>> searchAvailableStudents(Long classId, String keyword) {
        // 当前班级已有学生 ID
        List<ClassStudent> existing = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassId, classId));
        List<Long> existingIds = existing.stream().map(ClassStudent::getStudentId).toList();

        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String excludeSql = existingIds.isEmpty() ? "" :
            " AND id NOT IN (" + String.join(",", existingIds.stream().map(String::valueOf).toList()) + ")";

        return jdbc.queryForList(
            "SELECT id, username, real_name FROM sys_user WHERE role='student'" +
            " AND (username LIKE ? OR real_name LIKE ?)" + excludeSql + " LIMIT 20",
            kw, kw);
    }

    /** 查询学生加入的所有班级 */
    public List<ClassGroup> listByStudent(Long studentId) {
        List<ClassStudent> relations = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getStudentId, studentId));
        if (relations.isEmpty()) return List.of();
        List<Long> classIds = relations.stream().map(ClassStudent::getClassId).toList();
        return classGroupMapper.selectBatchIds(classIds);
    }
}
