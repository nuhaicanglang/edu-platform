package com.eduplatform.common.exception;

import com.eduplatform.common.core.domain.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 拦截所有 Controller 层抛出的异常，转换为统一的 {@link R} 响应格式。
 * 包括业务异常、参数校验异常、请求方法不支持异常和其他未知异常。
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常（Service 层主动抛出） */
    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /** @Valid 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数验证失败";
        log.error("参数验证异常: {}", message);
        return R.fail(400, message);
    }

    /** 参数绑定失败 */
    @ExceptionHandler(BindException.class)
    public R<?> handleBindException(BindException e) {
        String message = e.getFieldError() != null
                ? e.getFieldError().getDefaultMessage()
                : "参数绑定失败";
        log.error("参数绑定异常: {}", message);
        return R.fail(400, message);
    }

    /** 不支持的 HTTP 方法 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("不支持的请求方法: {}", e.getMethod());
        return R.fail(405, "不支持'" + e.getMethod() + "'请求");
    }

    /** 其他未知异常（兆底捕获） */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return R.fail("系统内部错误，请联系管理员");
    }
}
