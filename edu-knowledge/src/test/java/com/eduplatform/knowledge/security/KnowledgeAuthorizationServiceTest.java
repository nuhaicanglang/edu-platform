package com.eduplatform.knowledge.security;

import com.eduplatform.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeAuthorizationServiceTest {

    @Mock JdbcTemplate jdbcTemplate;

    @Test
    void studentCannotRetrieveFromCourseTheyHaveNotJoined() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(12L), eq(7L)))
                .thenReturn(0L);
        KnowledgeAuthorizationService service = new KnowledgeAuthorizationService(jdbcTemplate);

        assertThatThrownBy(() -> service.requireCourseAccess(12L, 7L, "student"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void teacherCannotUploadToAnotherTeachersCourse() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(12L), eq(7L)))
                .thenReturn(0L);
        KnowledgeAuthorizationService service = new KnowledgeAuthorizationService(jdbcTemplate);

        assertThatThrownBy(() -> service.requireCourseManager(12L, 7L, "teacher"))
                .isInstanceOf(BusinessException.class);
    }
}
