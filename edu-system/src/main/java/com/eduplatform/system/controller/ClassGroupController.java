package com.eduplatform.system.controller;

import com.eduplatform.common.core.domain.PageResult;
import com.eduplatform.common.core.domain.R;
import com.eduplatform.system.domain.entity.ClassGroup;
import com.eduplatform.system.domain.entity.ClassStudent;
import com.eduplatform.system.service.ClassGroupService;
import com.eduplatform.common.annotation.Log;
import com.eduplatform.common.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 班级管理控制器
 * <p>
 * 提供班级 CRUD、学生管理（添加/移除/查询）、可用学生搜索等接口。
 * 添加学生操作使用 SELECT FOR UPDATE 悲观锁防止并发重复插入。
 * </p>
 */
@RestController
@RequestMapping("/system/class")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    /** 分页查询班级（支持按课程ID、教师ID过滤） */
    @GetMapping("/page")
    public R<PageResult<ClassGroup>> page(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "teacherId", required = false) Long teacherId) {
        return R.ok(classGroupService.page(pageNum, pageSize, courseId, teacherId));
    }

    /** 根据 ID 查询班级详情（带 Redis 互斥锁缓存） */
    @GetMapping("/{id}")
    public R<ClassGroup> getById(@PathVariable Long id) {
        return R.ok(classGroupService.getById(id));
    }

    /** 创建班级（自动绑定当前教师ID） */
    @Log(module = "班级管理", value = "创建班级")
    @RequireRole({"teacher", "admin"})
    @PostMapping
    public R<Void> create(@RequestBody ClassGroup classGroup, @RequestHeader("X-User-Id") Long userId) {
        classGroup.setTeacherId(userId);
        classGroupService.create(classGroup);
        return R.ok();
    }

    /** 更新班级信息 */
    @Log(module = "班级管理", value = "更新班级")
    @RequireRole({"teacher", "admin"})
    @PutMapping
    public R<Void> update(@RequestBody ClassGroup classGroup) {
        classGroupService.update(classGroup);
        return R.ok();
    }

    /** 删除班级（逻辑删除） */
    @Log(module = "班级管理", value = "删除班级")
    @RequireRole({"teacher", "admin"})
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        classGroupService.delete(id);
        return R.ok();
    }

    /** 添加学生到班级（SELECT FOR UPDATE 悲观锁防并发重复） */
    @Log(module = "班级管理", value = "添加学生")
    @RequireRole({"teacher", "admin"})
    @PostMapping("/{classId}/student/{studentId}")
    public R<Void> addStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        classGroupService.addStudent(classId, studentId);
        return R.ok();
    }

    /** 从班级移除学生 */
    @Log(module = "班级管理", value = "移除学生")
    @RequireRole({"teacher", "admin"})
    @DeleteMapping("/{classId}/student/{studentId}")
    public R<Void> removeStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        classGroupService.removeStudent(classId, studentId);
        return R.ok();
    }

    /** 查询班级学生列表（包含学号和姓名） */
    @GetMapping("/{classId}/students")
    public R<List<ClassStudent>> getStudents(@PathVariable Long classId) {
        return R.ok(classGroupService.getStudents(classId));
    }

    /** 搜索不在该班级中的可用学生（按学号/姓名模糊匹配） */
    @GetMapping("/{classId}/available-students")
    public R<List<Map<String, Object>>> searchAvailableStudents(
            @PathVariable Long classId,
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        return R.ok(classGroupService.searchAvailableStudents(classId, keyword));
    }

    /** 学生查询自己加入的班级列表 */
    @GetMapping("/student/my")
    public R<List<ClassGroup>> myClasses(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(classGroupService.listByStudent(userId));
    }
}
