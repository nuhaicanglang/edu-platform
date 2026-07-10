package com.eduplatform.common.exception;

import com.eduplatform.common.core.domain.R;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void forbiddenBusinessExceptionReturnsHttp403() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/course/1");

        ResponseEntity<R<?>> response = handler.handleBusinessException(
                new BusinessException(403, "权限不足"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode()).isEqualTo(403);
    }

    @Test
    void unavailableBusinessExceptionReturnsHttp503() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/knowledge/retrieve");

        ResponseEntity<R<?>> response = handler.handleBusinessException(
                new BusinessException(503, "向量服务不可用"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
