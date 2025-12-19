package com.example.demo.controller;

import com.example.demo.service.ReasoningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/deepseek")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReasoningController {

    private final ReasoningService reasoningService;

    /**
     * 流式推理接口
     * 支持 Server-Sent Events (SSE) 格式
     */
    @PostMapping(value = "/reasoning-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamReasoning(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        log.info("收到推理请求: {}", message);
        
        return reasoningService.streamReasoningResponse(message)
                .doOnComplete(() -> log.info("推理流式响应完成"))
                .onErrorResume(ex -> isClientAbort(ex) ? Flux.empty() : Flux.error(ex))
                .doOnError(error -> {
                    if (!isClientAbort(error)) {
                        log.error("推理流式响应错误", error);
                    }
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
