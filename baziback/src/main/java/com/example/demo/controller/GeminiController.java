package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.request.gemini.GeminiFaceAnalysisRequest;
import com.example.demo.dto.request.gemini.GeminiTextProbeRequest;
import com.example.demo.dto.response.gemini.GeminiFaceAnalysisResponse;
import com.example.demo.dto.response.gemini.GeminiProbeResponse;
import com.example.demo.service.GeminiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
@Slf4j
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/face-analyze")
    public Result<GeminiFaceAnalysisResponse> analyzeFace(@Valid @RequestBody GeminiFaceAnalysisRequest request) throws Exception {
        log.info("鏀跺埌 Gemini 浜鸿劯鍒嗘瀽璇锋眰 | mimeType={}", request.getMimeType());
        return Result.success(geminiService.analyzeFace(request));
    }
    @PostMapping("/probe/text")
    public Result<GeminiProbeResponse> probeText(@RequestBody(required = false) GeminiTextProbeRequest request) throws Exception {
        String prompt = request == null ? null : request.getPrompt();
        return Result.success(geminiService.probeText(prompt));
    }

    @PostMapping("/probe/vision")
    public Result<GeminiProbeResponse> probeVision(@Valid @RequestBody GeminiFaceAnalysisRequest request) throws Exception {
        return Result.success(geminiService.probeVision(request));
    }
}
