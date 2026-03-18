package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.request.gemini.GeminiFaceAnalysisRequest;
import com.example.demo.service.GeminiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
@Slf4j
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/face-analyze")
    public Result<Map<String, Object>> analyzeFace(@Valid @RequestBody GeminiFaceAnalysisRequest request) throws Exception {
        log.info("收到 Gemini 人脸分析请求 | mimeType={}", request.getMimeType());
        return Result.success(geminiService.analyzeFace(request));
    }
}
