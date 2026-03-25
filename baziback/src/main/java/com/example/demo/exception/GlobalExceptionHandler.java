package com.example.demo.exception;

import com.example.demo.common.Result;
import com.example.demo.util.I18nHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.PayloadTooLargeException;

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
    public ResponseEntity<Result<Object>> handleBusiness(BusinessException ex) {
        HttpStatus status = ex.getStatus();
        Result<Object> body = Result.error(status.value(), ex.getMessage(), ex.getData());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(McpApiException.class)
    public ResponseEntity<Result<Map<String, String>>> handleMcpApiException(McpApiException ex) {
        log.error("MCP API error", ex);
        Map<String, String> data = new HashMap<>();
        data.put("code", "MCP_API_ERROR");
        data.put("message", I18nHelper.localize(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, I18nHelper.message("error.mcp.api", "MCP API 错误"), data));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request argument", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.badRequest(ex.getMessage()));
    }

    @ExceptionHandler({PayloadTooLargeException.class, DataBufferLimitException.class})
    public ResponseEntity<Result<Void>> handlePayloadTooLarge(Exception ex) {
        log.warn("Payload too large", ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Result.error(413, I18nHelper.message("error.payload_too_large", "请求体过大，请压缩图片后重试。")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleGenericException(Exception ex) {
        log.error("Server error", ex);

        String msg = debugMode
                ? I18nHelper.message("error.internal.debug", new Object[]{ex.getMessage()}, "服务器内部错误: " + ex.getMessage())
                : I18nHelper.message("error.internal", "服务器内部错误，请稍后重试。");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(msg));
    }
}
