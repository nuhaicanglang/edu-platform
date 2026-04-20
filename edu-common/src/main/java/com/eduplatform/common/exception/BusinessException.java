package com.eduplatform.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * Service 层检测到不合法操作时抛出，由 {@link GlobalExceptionHandler} 统一捕获并返回友好提示。
 * 默认 code=500，可通过构造器自定义 HTTP 语义状态码。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
