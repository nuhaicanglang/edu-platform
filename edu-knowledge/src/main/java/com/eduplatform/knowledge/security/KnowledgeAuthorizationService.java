package com.eduplatform.knowledge.security;

import com.eduplatform.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 知识库课程与文档对象级权限校验。 */
@Service
@RequiredArgsConstructor
public class KnowledgeAuthorizationService {

    private final JdbcTemplate jdbcTemplate;

    public void requireCourseAccess(Long courseId, Long userId, String role) {
        requireIdentity(courseId, userId, role);
        if ("admin".equals(role)) return;
        Long count;
        if ("teacher".equals(role)) {
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM course WHERE id=? AND teacher_id=? AND deleted=0",
                    Long.class, courseId, userId);
        } else if ("student".equals(role)) {
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM class_group cg "
                            + "JOIN class_student cs ON cs.class_id=cg.id AND cs.deleted=0 "
                            + "WHERE cg.course_id=? AND cs.student_id=? AND cg.deleted=0",
                    Long.class, courseId, userId);
        } else {
            count = 0L;
        }
        if (count == null || count == 0) deny();
    }

    public void requireCourseManager(Long courseId, Long userId, String role) {
        requireIdentity(courseId, userId, role);
        if ("admin".equals(role)) return;
        if (!"teacher".equals(role)) deny();
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM course WHERE id=? AND teacher_id=? AND deleted=0",
                Long.class, courseId, userId);
        if (count == null || count == 0) deny();
    }

    public void requireDocumentAccess(Long documentId, Long userId, String role) {
        requireCourseAccess(documentCourseId(documentId), userId, role);
    }

    public void requireDocumentManager(Long documentId, Long userId, String role) {
        requireCourseManager(documentCourseId(documentId), userId, role);
    }

    public void requireOptionalCourseScope(Long courseId, Long userId, String role) {
        if (courseId == null) {
            if (!"admin".equals(role)) {
                throw new BusinessException(403, "非管理员查询必须指定 courseId");
            }
            return;
        }
        requireCourseAccess(courseId, userId, role);
    }

    private Long documentCourseId(Long documentId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT course_id FROM knowledge_document WHERE id=? AND deleted=0",
                    Long.class, documentId);
        } catch (Exception e) {
            throw new BusinessException(404, "知识文档不存在");
        }
    }

    private void requireIdentity(Long courseId, Long userId, String role) {
        if (courseId == null || userId == null || role == null || role.isBlank()) {
            throw new BusinessException(401, "身份或课程参数缺失");
        }
    }

    private void deny() {
        throw new BusinessException(403, "权限不足，无法访问该课程知识库");
    }
}
