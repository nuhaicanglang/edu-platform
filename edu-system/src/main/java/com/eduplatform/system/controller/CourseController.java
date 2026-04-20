package com.eduplatform.system.controller;

import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.core.domain.R;
import com.eduplatform.system.domain.entity.Course;
import com.eduplatform.system.domain.entity.ClassGroup;
import com.eduplatform.system.service.ClassGroupService;
import com.eduplatform.system.service.CourseService;
import com.eduplatform.system.service.LearningRecordService;
import com.eduplatform.common.annotation.Log;
import com.eduplatform.common.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程管理控制器
 * <p>
 * 提供课程的 CRUD、分页查询、教师“我的课程”等接口。
 * 单个课程查询通过 {@link com.eduplatform.common.utils.RedisCacheService} 缓存，
 * 列表查询直接走数据库。
 * </p>
 */
@RestController
@RequestMapping("/system/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final ClassGroupService classGroupService;
    private final LearningRecordService learningRecordService;

    /** 分页查询课程（支持按课程名称、教师ID过滤） */
    @GetMapping("/page")
    public R<PageResult<Course>> page(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "courseName", required = false) String courseName,
            @RequestParam(value = "teacherId", required = false) Long teacherId) {
        return R.ok(courseService.page(pageNum, pageSize, courseName, teacherId));
    }

    /** 根据 ID 查询课程详情（带 Redis 缓存） */
    @GetMapping("/{id}")
    public R<Course> getById(@PathVariable Long id,
                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Course course = courseService.getById(id);
        if (userId != null) {
            learningRecordService.recordView(userId, id, "浏览课程: " + (course != null ? course.getCourseName() : id));
        }
        return R.ok(course);
    }

    /** 查询所有课程列表（无分页） */
    @GetMapping("/list")
    public R<List<Course>> listAll() {
        return R.ok(courseService.listAll());
    }

    /** 获取当前用户关联的课程列表（教师→自己创建的课程，学生→已加入班级的课程） */
    @GetMapping("/my")
    public R<List<Course>> myList(@RequestHeader("X-User-Id") Long userId,
                                  @RequestHeader(value = "X-User-Role", required = false) String role) {
        if ("student".equals(role)) {
            List<ClassGroup> myClasses = classGroupService.listByStudent(userId);
            if (myClasses.isEmpty()) return R.ok(List.of());
            List<Long> courseIds = myClasses.stream().map(ClassGroup::getCourseId).distinct().collect(Collectors.toList());
            return R.ok(courseService.listByIds(courseIds));
        }
        return R.ok(courseService.listByTeacher(userId));
    }

    /** 创建课程（自动绑定当前教师ID） */
    @Log(module = "课程管理", value = "创建课程")
    @RequireRole({"teacher", "admin"})
    @PostMapping
    public R<Void> create(@RequestBody Course course, @RequestHeader("X-User-Id") Long userId) {
        course.setTeacherId(userId);
        courseService.create(course);
        return R.ok();
    }

    /** 更新课程信息（同时清除该课程的 Redis 缓存） */
    @Log(module = "课程管理", value = "更新课程")
    @RequireRole({"teacher", "admin"})
    @PutMapping
    public R<Void> update(@RequestBody Course course) {
        courseService.update(course);
        return R.ok();
    }

    /** 删除课程（逻辑删除 + 清除缓存） */
    @Log(module = "课程管理", value = "删除课程")
    @RequireRole({"teacher", "admin"})
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return R.ok();
    }
}
