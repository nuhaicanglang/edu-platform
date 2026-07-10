package com.eduplatform.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduplatform.common.exception.BusinessException;
import com.eduplatform.system.domain.entity.Assignment;
import com.eduplatform.system.domain.entity.AssignmentSubmission;
import com.eduplatform.system.domain.entity.ClassGroup;
import com.eduplatform.system.domain.entity.ClassStudent;
import com.eduplatform.system.domain.entity.Course;
import com.eduplatform.system.mapper.AssignmentMapper;
import com.eduplatform.system.mapper.AssignmentSubmissionMapper;
import com.eduplatform.system.mapper.ClassGroupMapper;
import com.eduplatform.system.mapper.ClassStudentMapper;
import com.eduplatform.system.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 集中校验课程、班级、作业和提交记录的对象级访问权限。
 */
@Service
@RequiredArgsConstructor
public class ResourceAuthorizationService {

    private final CourseMapper courseMapper;
    private final ClassGroupMapper classGroupMapper;
    private final ClassStudentMapper classStudentMapper;
    private final AssignmentMapper assignmentMapper;
    private final AssignmentSubmissionMapper submissionMapper;

    public void requireCourseManager(Long courseId, Long userId, String role) {
        if (isAdmin(role)) return;
        Course course = requireCourse(courseId);
        if (!"teacher".equals(role) || !Objects.equals(course.getTeacherId(), userId)) {
            deny();
        }
    }

    public void requireCourseAccess(Long courseId, Long userId, String role) {
        if (isAdmin(role)) return;
        Course course = requireCourse(courseId);
        if ("teacher".equals(role) && Objects.equals(course.getTeacherId(), userId)) return;
        if ("student".equals(role) && isStudentInCourse(courseId, userId)) return;
        deny();
    }

    public void requireClassManager(Long classId, Long userId, String role) {
        if (isAdmin(role)) return;
        ClassGroup classGroup = requireClass(classId);
        if (!"teacher".equals(role) || !Objects.equals(classGroup.getTeacherId(), userId)) {
            deny();
        }
    }

    public void requireClassAccess(Long classId, Long userId, String role) {
        if (isAdmin(role)) return;
        ClassGroup classGroup = requireClass(classId);
        if ("teacher".equals(role) && Objects.equals(classGroup.getTeacherId(), userId)) return;
        if ("student".equals(role) && isStudentInClass(classId, userId)) return;
        deny();
    }

    public void requireAssignmentCreationAccess(
            Long courseId, Long classId, Long userId, String role) {
        requireCourseManager(courseId, userId, role);
        if (classId == null) return;
        ClassGroup classGroup = requireClass(classId);
        if (!Objects.equals(classGroup.getCourseId(), courseId)) {
            throw new BusinessException(400, "班级不属于指定课程");
        }
        requireClassManager(classId, userId, role);
    }

    public void requireAssignmentManager(Long assignmentId, Long userId, String role) {
        if (isAdmin(role)) return;
        Assignment assignment = requireAssignment(assignmentId);
        if (!"teacher".equals(role) || !Objects.equals(assignment.getTeacherId(), userId)) {
            deny();
        }
    }

    public void requireAssignmentAccess(Long assignmentId, Long userId, String role) {
        if (isAdmin(role)) return;
        Assignment assignment = requireAssignment(assignmentId);
        if ("teacher".equals(role) && Objects.equals(assignment.getTeacherId(), userId)) return;
        if ("student".equals(role)) {
            boolean enrolled = assignment.getClassId() != null
                    ? isStudentInClass(assignment.getClassId(), userId)
                    : isStudentInCourse(assignment.getCourseId(), userId);
            if (enrolled) return;
        }
        deny();
    }

    public void requireSubmissionAccess(Long submissionId, Long userId, String role) {
        if (isAdmin(role)) return;
        AssignmentSubmission submission = requireSubmission(submissionId);
        if ("student".equals(role) && Objects.equals(submission.getStudentId(), userId)) return;
        if ("teacher".equals(role)) {
            requireAssignmentManager(submission.getAssignmentId(), userId, role);
            return;
        }
        deny();
    }

    public void requireGradingAccess(
            Long assignmentId, Long submissionId, Long userId, String role) {
        requireAssignmentManager(assignmentId, userId, role);
        AssignmentSubmission submission = requireSubmission(submissionId);
        if (!Objects.equals(submission.getAssignmentId(), assignmentId)) {
            throw new BusinessException(400, "提交记录不属于该作业");
        }
    }

    private Course requireCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException(404, "课程不存在");
        return course;
    }

    private ClassGroup requireClass(Long classId) {
        ClassGroup classGroup = classGroupMapper.selectById(classId);
        if (classGroup == null) throw new BusinessException(404, "班级不存在");
        return classGroup;
    }

    private Assignment requireAssignment(Long assignmentId) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) throw new BusinessException(404, "作业不存在");
        return assignment;
    }

    private AssignmentSubmission requireSubmission(Long submissionId) {
        AssignmentSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) throw new BusinessException(404, "提交记录不存在");
        return submission;
    }

    private boolean isStudentInClass(Long classId, Long studentId) {
        return classStudentMapper.selectCount(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getClassId, classId)
                        .eq(ClassStudent::getStudentId, studentId)) > 0;
    }

    private boolean isStudentInCourse(Long courseId, Long studentId) {
        List<ClassGroup> classes = classGroupMapper.selectList(
                new LambdaQueryWrapper<ClassGroup>().eq(ClassGroup::getCourseId, courseId));
        return classes.stream().anyMatch(classGroup -> isStudentInClass(classGroup.getId(), studentId));
    }

    private boolean isAdmin(String role) {
        return "admin".equals(role);
    }

    private void deny() {
        throw new BusinessException(403, "权限不足，无法访问该资源");
    }
}
