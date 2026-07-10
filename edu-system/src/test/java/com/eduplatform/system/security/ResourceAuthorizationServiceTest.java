package com.eduplatform.system.security;

import com.eduplatform.common.exception.BusinessException;
import com.eduplatform.system.domain.entity.Assignment;
import com.eduplatform.system.domain.entity.AssignmentSubmission;
import com.eduplatform.system.domain.entity.Course;
import com.eduplatform.system.mapper.AssignmentMapper;
import com.eduplatform.system.mapper.AssignmentSubmissionMapper;
import com.eduplatform.system.mapper.ClassGroupMapper;
import com.eduplatform.system.mapper.ClassStudentMapper;
import com.eduplatform.system.mapper.CourseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceAuthorizationServiceTest {

    @Mock CourseMapper courseMapper;
    @Mock ClassGroupMapper classGroupMapper;
    @Mock ClassStudentMapper classStudentMapper;
    @Mock AssignmentMapper assignmentMapper;
    @Mock AssignmentSubmissionMapper submissionMapper;

    private ResourceAuthorizationService service() {
        return new ResourceAuthorizationService(
                courseMapper, classGroupMapper, classStudentMapper, assignmentMapper, submissionMapper);
    }

    @Test
    void teacherCannotModifyAnotherTeachersCourse() {
        Course course = new Course();
        course.setId(9L);
        course.setTeacherId(22L);
        when(courseMapper.selectById(9L)).thenReturn(course);

        assertThatThrownBy(() -> service().requireCourseManager(9L, 21L, "teacher"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void studentCannotReadAnotherStudentsSubmission() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setId(5L);
        submission.setStudentId(9L);
        submission.setAssignmentId(31L);
        when(submissionMapper.selectById(5L)).thenReturn(submission);

        assertThatThrownBy(() -> service().requireSubmissionAccess(5L, 8L, "student"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void rejectsSubmissionFromDifferentAssignment() {
        Assignment assignment = new Assignment();
        assignment.setId(31L);
        assignment.setTeacherId(21L);
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setId(5L);
        submission.setAssignmentId(32L);
        when(assignmentMapper.selectById(31L)).thenReturn(assignment);
        when(submissionMapper.selectById(5L)).thenReturn(submission);

        assertThatThrownBy(() -> service().requireGradingAccess(31L, 5L, 21L, "teacher"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不属于该作业");
    }

    @Test
    void adminCanManageAnyCourse() {
        service().requireCourseManager(999L, 1L, "admin");
    }
}
