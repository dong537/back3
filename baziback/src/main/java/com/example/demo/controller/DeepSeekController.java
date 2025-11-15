package com.example.demo.controller;

import com.example.demo.dto.response.deepseek.BaziReportResponse;
import com.example.demo.service.DeepSeekService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 八字报告接口控制器（前端调用入口）
 */
@RestController
@RequestMapping("/api/bazi")
public class DeepSeekController {
    private final DeepSeekService deepSeekService;

    public DeepSeekController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping("/generate-report")
    public String generateReport(@RequestBody String rawPrompt) throws Exception {
        return deepSeekService.generateBaziReport(rawPrompt);
    }
}