package com.eduplatform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.exception.BusinessException;
import com.eduplatform.common.utils.RedisCacheService;
import com.eduplatform.system.domain.entity.Course;
import com.eduplatform.system.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 课程业务服务
 * <p>
 * 单个课程查询通过 {@link RedisCacheService#getOrLoadWithLock} 缓存（互斥锁防击穿 + 空值防穿透 + 随朼TTL防雪崩），
 * 列表查询直接走 DB（MyBatis-Plus 连接池，速度足够）。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final RedisCacheService cache;

    private static final String KEY_COURSE     = "course:id:";
    private static final long   TTL_SECONDS    = 600;
    private static final int    TTL_JITTER     = 120;

    /** 分页查询课程（支持课程名模糊搜索 + 教师ID精确过滤） */
    public PageResult<Course> page(int pageNum, int pageSize, String courseName, Long teacherId) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(courseName), Course::getCourseName, courseName);
        wrapper.eq(teacherId != null, Course::getTeacherId, teacherId);
        wrapper.orderByDesc(Course::getCreateTime);
        Page<Course> result = courseMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    /** 根据 ID 查询课程详情（带互斥锁缓存，防击穿 + 防穿透） */
    public Course getById(Long id) {
        // 带互斥锁缓存，防击穿；返回 null 时缓存空值，防穿透
        Course course = cache.getOrLoadWithLock(
                KEY_COURSE + id,
                () -> courseMapper.selectById(id),
                TTL_SECONDS, TTL_JITTER);
        if (course == null) throw new BusinessException("课程不存在");
        return course;
    }

    /** 创建课程 */
    public void create(Course course) {
        courseMapper.insert(course);
    }

    /** 更新课程（同时清除该课程的 Redis 缓存） */
    public void update(Course course) {
        courseMapper.updateById(course);
        cache.evict(KEY_COURSE + course.getId());
    }

    /** 删除课程（逻辑删除 + 清除缓存） */
    public void delete(Long id) {
        courseMapper.deleteById(id);
        cache.evict(KEY_COURSE + id);
    }

    public List<Course> listByTeacher(Long teacherId) {
        // 列表查询直接走 DB（MyBatis-Plus 有连接池，速度足够快）
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, teacherId)
                        .orderByDesc(Course::getCreateTime));
    }

    /** 根据 ID 列表批量查询课程 */
    public List<Course> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return courseMapper.selectBatchIds(ids);
    }

    /** 查询所有课程列表（无分页，直接走 DB） */
    public List<Course> listAll() {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>().orderByDesc(Course::getCreateTime));
    }
}
