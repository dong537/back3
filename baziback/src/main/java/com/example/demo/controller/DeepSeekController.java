package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * DeepSeek 接口控制器（前端调用入口）
 */
@RestController
@RequestMapping("/api/deepseek")
@RequiredArgsConstructor
@Slf4j
public class DeepSeekController {

    private final DeepSeekService deepSeekService;

    /**
     * 前端/内部调用：通过POST提交完整的提示词或JSON
     */
    @PostMapping("/generate-report")
    public Result<String> generateReport(@RequestBody String rawPrompt) throws Exception {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            throw new BusinessException("请求内容不能为空");
        }
        return Result.success(deepSeekService.generateBaziReport(rawPrompt));
    }

    /**
     * 使用 DeepSeek 解读卦象
     */
    @PostMapping("/interpret-hexagram")
    public Result<String> interpretHexagram(@RequestBody String request) throws Exception {
        if (request == null || request.isBlank()) {
            throw new BusinessException("请求内容不能为空");
        }
        return Result.success(deepSeekService.interpretHexagram(request));
    }

    /**
     * 使用 DeepSeek 解读紫微命盘
     */
    @PostMapping("/chart/deepseek-interpret")
    public Result<String> interpretChartByDeepSeek(@RequestBody String request) throws Exception {
        if (request == null || request.isBlank()) {
            throw new BusinessException("请求内容不能为空");
        }
        return Result.success(deepSeekService.interpretZiweiChart(request));
    }
}
