package com.eduplatform.system.controller;

import com.eduplatform.common.core.domain.R;
import com.eduplatform.system.domain.entity.CourseKnowledgePoint;
import com.eduplatform.system.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程知识点管理控制器
 * <p>
 * 提供知识点的 CRUD 和按课程查询接口，用于知识点图谱管理。
 * </p>
 */
@RestController
@RequestMapping("/system/knowledge-point")
@RequiredArgsConstructor
public class KnowledgePointController {

    private final KnowledgePointService knowledgePointService;

    /** 查询指定课程的所有知识点（按 sortOrder 升序） */
    @GetMapping("/course/{courseId}")
    public R<List<CourseKnowledgePoint>> listByCourse(@PathVariable Long courseId) {
        return R.ok(knowledgePointService.listByCourse(courseId));
    }

    /** 根据 ID 查询单个知识点 */
    @GetMapping("/{id}")
    public R<CourseKnowledgePoint> getById(@PathVariable Long id) {
        return R.ok(knowledgePointService.getById(id));
    }

    /** 创建知识点 */
    @PostMapping
    public R<Void> create(@RequestBody CourseKnowledgePoint point) {
        knowledgePointService.create(point);
        return R.ok();
    }

    /** 更新知识点 */
    @PutMapping
    public R<Void> update(@RequestBody CourseKnowledgePoint point) {
        knowledgePointService.update(point);
        return R.ok();
    }

    /** 删除知识点 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        knowledgePointService.delete(id);
        return R.ok();
    }
}
