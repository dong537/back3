package com.example.demo.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.response.Result;
import com.example.demo.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * DeepSeek AI接口控制器
 */
@RestController
@RequestMapping("/api/deepseek")
@RequiredArgsConstructor
@Slf4j
@RequireAuth  // 所有接口都需要登录
public class DeepSeekController {

    private final DeepSeekService deepSeekService;

    /**
     * 生成八字报告
     * 限流：每分钟最多5次
     */
    @PostMapping({"/generate-report", "/bazi/generate-report"})
    @RateLimit(timeWindow = 60, maxCount = 5, limitType = RateLimit.LimitType.USER)
    public Result<String> generateReport(@RequestBody String rawPrompt) {
        try {
            String report = deepSeekService.generateBaziReport(rawPrompt);
            return Result.success(report);
        } catch (Exception e) {
            log.error("生成八字报告失败", e);
            return Result.error("生成报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 解读卦象
     * 限流：每分钟最多5次
     */
    @PostMapping("/interpret-hexagram")
    @RateLimit(timeWindow = 60, maxCount = 5, limitType = RateLimit.LimitType.USER)
    public Result<String> interpretHexagram(@RequestBody String request) {
        try {
            String interpretation = deepSeekService.interpretHexagram(request);
            return Result.success(interpretation);
        } catch (Exception e) {
            log.error("解读卦象失败", e);
            return Result.error("解读失败: " + e.getMessage());
        }
    }
    
    /**
     * 紫微斗数命盘解读
     * 限流：每分钟最多5次
     */
    @PostMapping("/chart/deepseek-interpret")
    @RateLimit(timeWindow = 60, maxCount = 5, limitType = RateLimit.LimitType.USER)
    public Result<String> interpretChart(@RequestBody String request) {
        try {
            String interpretation = deepSeekService.interpretHexagram(request);
            return Result.success(interpretation);
        } catch (Exception e) {
            log.error("解读命盘失败", e);
            return Result.error("解读失败: " + e.getMessage());
        }
    }
}
