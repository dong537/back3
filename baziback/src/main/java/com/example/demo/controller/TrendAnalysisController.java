package com.example.demo.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.trend.TrendAnalysisRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.dto.response.TrendAnalysisResponse;
import com.example.demo.service.TrendAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 趋势分析Controller - 大运、流年、流月分析
 * 对标 cantian.ai 的趋势解读功能
 */
@RestController
@RequestMapping("/api/trend")
@RequiredArgsConstructor
@Slf4j
@RequireAuth
@Tag(name = "趋势分析", description = "大运、流年、流月趋势分析API")
public class TrendAnalysisController {

    private final TrendAnalysisService trendAnalysisService;

    /**
     * 获取完整趋势分析
     */
    @PostMapping("/analysis")
    @RateLimit(timeWindow = 60, maxCount = 5, limitType = RateLimit.LimitType.USER)
    @Operation(summary = "获取完整趋势分析", description = "包含大运、流年、流月分析，以及重要节点和风险提示")
    public Result<TrendAnalysisResponse> getTrendAnalysis(@Validated @RequestBody TrendAnalysisRequest request) {
        
        log.info("收到趋势分析请求: bazi={}, gender={}, birthDate={}", 
                request.getBazi(), request.getGender(), request.getBirthDate());
        
        try {
            TrendAnalysisResponse response = trendAnalysisService.getCompleteTrendAnalysis(
                    request.getBazi(), 
                    request.getGender(), 
                    request.getBirthDate(), 
                    request.getStartAge(), 
                    request.getEndAge());
            return Result.success(response);
        } catch (Exception e) {
            log.error("趋势分析失败", e);
            return Result.error("趋势分析失败：" + e.getMessage());
        }
    }
}
