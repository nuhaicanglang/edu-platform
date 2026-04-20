package com.eduplatform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduplatform.system.domain.entity.CourseKnowledgePoint;
import com.eduplatform.system.mapper.CourseKnowledgePointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程知识点业务服务
 * <p>
 * 提供知识点 CRUD 和按课程查询，用于知识图谱管理和 AI 教学分析。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class KnowledgePointService {

    private final CourseKnowledgePointMapper mapper;

    /** 查询指定课程的所有知识点（按 sortOrder 升序） */
    public List<CourseKnowledgePoint> listByCourse(Long courseId) {
        return mapper.selectList(
                new LambdaQueryWrapper<CourseKnowledgePoint>()
                        .eq(CourseKnowledgePoint::getCourseId, courseId)
                        .orderByAsc(CourseKnowledgePoint::getSortOrder));
    }

    /** 创建知识点 */
    public void create(CourseKnowledgePoint point) {
        mapper.insert(point);
    }

    /** 更新知识点 */
    public void update(CourseKnowledgePoint point) {
        mapper.updateById(point);
    }

    /** 删除知识点 */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /** 根据 ID 查询单个知识点 */
    public CourseKnowledgePoint getById(Long id) {
        return mapper.selectById(id);
    }
}
