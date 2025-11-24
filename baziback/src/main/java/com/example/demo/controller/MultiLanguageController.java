package com.example.demo.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.i18n.MultiLanguageReportRequest;
import com.example.demo.dto.request.i18n.TranslateRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.service.MultiLanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 多语言支持Controller
 * 对标 cantian.ai 的多语言功能
 */
@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "多语言支持", description = "多语言翻译和本地化API")
public class MultiLanguageController {

    private final MultiLanguageService multiLanguageService;

    /**
     * 获取支持的语言列表
     */
    @GetMapping("/languages")
    @Operation(summary = "获取支持的语言列表", description = "返回系统支持的所有语言及其名称")
    public Result<Map<String, String>> getSupportedLanguages() {
        try {
            Map<String, String> languages = multiLanguageService.getSupportedLanguages();
            return Result.success(languages);
        } catch (Exception e) {
            log.error("获取语言列表失败", e);
            return Result.error("获取语言列表失败：" + e.getMessage());
        }
    }

    /**
     * 翻译八字分析结果
     */
    @PostMapping("/translate")
    @RequireAuth
    @RateLimit(timeWindow = 60, maxCount = 10, limitType = RateLimit.LimitType.USER)
    @Operation(summary = "翻译分析结果", description = "将八字分析结果翻译成目标语言")
    public Result<String> translateAnalysis(@Validated @RequestBody TranslateRequest request) {
        
        log.info("收到翻译请求，目标语言：{}", request.getTargetLanguage());
        
        try {
            String translated = multiLanguageService.translateBaziAnalysis(
                    request.getContent(), 
                    request.getTargetLanguage());
            return Result.success(translated);
        } catch (IllegalArgumentException e) {
            log.warn("不支持的语言：{}", request.getTargetLanguage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("翻译失败", e);
            return Result.error("翻译失败：" + e.getMessage());
        }
    }

    /**
     * 生成多语言报告
     */
    @PostMapping("/report")
    @RequireAuth
    @RateLimit(timeWindow = 60, maxCount = 5, limitType = RateLimit.LimitType.USER)
    @Operation(summary = "生成多语言报告", description = "直接生成指定语言的八字分析报告")
    public Result<String> generateMultiLanguageReport(@Validated @RequestBody MultiLanguageReportRequest request) {
        
        log.info("收到多语言报告生成请求，语言：{}，类型：{}", 
                request.getLanguage(), request.getReportType());
        
        try {
            String report = multiLanguageService.generateMultiLanguageReport(
                    request.getBaziData(), 
                    request.getReportType(), 
                    request.getLanguage());
            return Result.success(report);
        } catch (IllegalArgumentException e) {
            log.warn("参数错误：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("生成报告失败", e);
            return Result.error("生成报告失败：" + e.getMessage());
        }
    }
}
