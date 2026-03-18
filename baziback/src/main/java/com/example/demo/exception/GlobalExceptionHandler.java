package com.example.demo.exception;

import com.example.demo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.debug:false}")
    private boolean debugMode;

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.unauthorized(ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.badRequest(ex.getMessage()));
    }

    // 处理MCP接口异常
    @ExceptionHandler(McpApiException.class)
    public ResponseEntity<Result<Map<String, String>>> handleMcpApiException(McpApiException ex) {
        log.error("MCP API异常", ex);
        Map<String, String> data = new HashMap<>();
        data.put("code", "MCP_API_ERROR");
        data.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "MCP API异常", data));
    }

    // 处理参数错误
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数错误", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.badRequest(ex.getMessage()));
    }

    // 处理其他未知异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleGenericException(Exception ex) {
        log.error("服务器错误", ex);

        String msg = debugMode
                ? ("服务器内部错误：" + ex.getMessage())
                : "服务器内部错误，请稍后重试";

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(msg));
    }
}
