package com.example.demo.controller;

import com.example.demo.service.ReasoningService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/deepseek")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ReasoningController {

    private final ReasoningService reasoningService;
    private final AuthUtil authUtil;

    /**
     * ✅ 新增：非流式接口（推荐使用）
     * 一次性返回完整的推理结果
     */
    @PostMapping(value = "/reasoning", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> reasoning(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {
        
        // ✅ 验证认证信息
        Long userId = authUtil.requireUserId(token);
        log.info("用户 {} 请求推理", userId);
        
        if (request == null) {
            log.error("请求参数为空");
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "请求参数不能为空");
            return error;
        }

        Object msgObj = request.get("message");
        String message = msgObj == null ? null : String.valueOf(msgObj);

        if (message == null || message.trim().isEmpty()) {
            log.error("message 参数为空");
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "message不能为空");
            return error;
        }

        if ("0".equals(message) || message.trim().length() < 2) {
            log.error("message 参数无效: {}", message);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "message无效");
            return error;
        }

        log.info("收到推理请求，消息长度: {} 字符", message.length());

        try {
            // 调用推理服务获取完整结果
            String result = reasoningService.getReasoningResult(message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", result);
            response.put("finish_reason", "stop");
            
            log.info("推理完成，结果长度: {} 字符", result.length());
            return response;
            
        } catch (Exception e) {
            log.error("推理失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "推理失败: " + e.getMessage());
            return error;
        }
    }

    /**
     * 流式推理接口（SSE）
     * - 入参不合法：返回 400
     * - 正常：返回 text/event-stream
     * ✅ 需要认证
     */
    @PostMapping(value = "/reasoning-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamReasoning(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {
        
        // ✅ 验证认证信息
        Long userId = authUtil.requireUserId(token);
        log.info("用户 {} 请求推理流式响应", userId);
        
        if (request == null) {
            log.error("请求参数为空");
            return Flux.error(new IllegalArgumentException("请求参数不能为空"));
        }

        Object msgObj = request.get("message");
        String message = msgObj == null ? null : String.valueOf(msgObj);

        if (message == null || message.trim().isEmpty()) {
            log.error("message 参数为空");
            return Flux.error(new IllegalArgumentException("message不能为空"));
        }

        if ("0".equals(message) || message.trim().length() < 2) {
            log.error("message 参数无效: {}", message);
            return Flux.error(new IllegalArgumentException("message无效"));
        }

        log.info("收到推理请求，消息长度: {} 字符", message.length());

        return reasoningService.streamReasoningResponse(message)
                .doOnNext(data -> log.debug("发送数据块: {} 字节", data.length()))
                .doOnComplete(() -> log.info("推理流式响应完成"))
                .doOnError(error -> log.error("推理流式响应错误", error))
                .onErrorResume(ex -> {
                    if (isClientAbort(ex)) {
                        log.info("客户端中止连接");
                        return Flux.empty();
                    }
                    log.error("处理推理请求时发生错误", ex);
                    return Flux.just("data: {\"content\":\"服务器错误\",\"finish_reason\":\"error\"}\n\n")
                            .concatWith(Flux.just("data: [DONE]\n\n"));
                });
    }

    private boolean isClientAbort(Throwable ex) {
        if (ex == null) {
            return false;
        }
        if (ex instanceof IOException) {
            String msg = ex.getMessage();
            return msg != null && (msg.contains("中止") || msg.contains("aborted") || msg.contains("reset"));
        }
        return isClientAbort(ex.getCause());
    }
}
