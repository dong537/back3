package com.example.demo.controller;

import com.example.demo.dto.request.yijing.YijingInterpretRequest;
import com.example.demo.service.DeepSeekService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 八字报告接口控制器（前端调用入口）
 */
@RestController
@RequestMapping("/api/deepseek")
@RequiredArgsConstructor
@Slf4j
public class DeepSeekController {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;

    /**
     * 前端/内部调用：通过POST提交完整的提示词或JSON
     * 兼容旧路径 /api/bazi/generate-report (如果前端未改)
     */
    @PostMapping({"/generate-report", "/bazi/generate-report"})
    public ResponseEntity<String> generateReport(@RequestBody String rawPrompt) throws Exception {
        return ResponseEntity.ok(deepSeekService.generateBaziReport(rawPrompt));
    }

    /**
     * 使用 DeepSeek 解读卦象
     */
    @PostMapping("/interpret-hexagram")
    public ResponseEntity<String> interpretHexagram(@RequestBody String request) throws Exception {
        return ResponseEntity.ok(deepSeekService.interpretHexagram(request));
    }
}
