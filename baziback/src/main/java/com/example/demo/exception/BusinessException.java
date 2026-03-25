package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 业务异常：用于表示可预期的业务错误（例如：今天已打卡）
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final Object data;

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST, null);
    }

    public BusinessException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public BusinessException(String message, HttpStatus status, Object data) {
        super(message);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
        this.data = data;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}
