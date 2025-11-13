package com.example.demo.controller;

import com.example.demo.service.DeepSeekService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 八字智能报告接口
 */
@RestController
@RequestMapping("/api/bazi")
@Slf4j
@RequiredArgsConstructor
public class DeepSeekController {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;

    @PostMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody Map<String, Object> payload) {
        Instant start = Instant.now();
        log.info("收到智能报告请求: {}", payload);

        Map<String, Object> response = new HashMap<>();
        try {
            String rawPrompt = convertPayloadToPrompt(payload);
            String report = deepSeekService.generateBaziReport(rawPrompt);

            response.put("success", true);
            response.put("report", report);
            response.put("elapsedMs", Duration.between(start, Instant.now()).toMillis());
            log.info("智能报告生成成功，耗时 {} ms，报告字数 {}", response.get("elapsedMs"),
                    report != null ? report.length() : 0);
            return ResponseEntity.ok(response);
        } catch (HttpTimeoutException e) {
            log.error("调用DeepSeek超时", e);
            response.put("success", false);
            response.put("error", "生成报告超时，请稍后重试");
            return ResponseEntity.status(504).body(response);
        } catch (Exception e) {
            log.error("生成智能报告失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String convertPayloadToPrompt(Map<String, Object> payload) throws JsonProcessingException {
        if (payload == null || payload.isEmpty()) {
            return "{}";
        }
        return objectMapper.writeValueAsString(payload);
    }
}