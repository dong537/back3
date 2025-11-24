package com.example.demo.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.annotation.RequireAuth;
import com.example.demo.client.McpBaziClient;
import com.example.demo.dto.request.bazi.McpBaziRequest;
import com.example.demo.dto.response.FormattedBaziResponse;
import com.example.demo.dto.response.McpBaziResponse;
import com.example.demo.entity.AnalysisHistory;
import com.example.demo.service.AnalysisHistoryService;
import com.example.demo.service.DeepSeekService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bazi")
@RequiredArgsConstructor
@Slf4j
@RequireAuth  // 需要登录
public class BaziController {

    private final McpBaziClient mcpBaziClient;
    private final DeepSeekService deepSeekService;
    private final AnalysisHistoryService historyService;
    private final ObjectMapper objectMapper;

    /**
     * 查询可用工具列表
     * GET http://localhost:8080/api/bazi/tools
     */
    @GetMapping("/tools")
    @RateLimit(timeWindow = 60, maxCount = 20, limitType = RateLimit.LimitType.USER)
    public String listTools() {
        return mcpBaziClient.listAvailableTools();
    }
    /**
     * 获取八字详情（格式化的响应，前端友好）
     * POST http://localhost:8080/api/bazi/formatted
     */
    @PostMapping("/formatted")
    @RateLimit(timeWindow = 60, maxCount = 10, limitType = RateLimit.LimitType.USER)
    public FormattedBaziResponse getBaziFormatted(
            @RequestBody McpBaziRequest request,
            HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("收到八字查询请求: userId={}, gender={}, lunarDatetime={}, solarDatetime={}", 
                    userId, request.getGender(), request.getLunarDatetime(), request.getSolarDatetime());
            
            McpBaziResponse resp = mcpBaziClient.getBaziDetail(request);
            
            if (resp == null || resp.getBaziData() == null) {
                log.error("MCP响应为空或baziData为空");
                throw new RuntimeException("获取八字数据失败：响应为空");
            }
            
            FormattedBaziResponse formatted = FormattedBaziResponse.fromMcpResponse(resp);
            
            if (formatted == null) {
                log.error("格式化响应失败");
                throw new RuntimeException("格式化八字数据失败");
            }
            
            // 保存分析历史
            try {
                AnalysisHistory history = new AnalysisHistory();
                history.setUserId(userId);
                history.setAnalysisType("bazi");
                history.setRequestData(objectMapper.writeValueAsString(request));
                history.setResponseData(objectMapper.writeValueAsString(formatted));
                history.setAnalysisDuration((int) (System.currentTimeMillis() - startTime));
                history.setModelVersion("mcp-bazi-v1");
                history.setIsFavorite(0);
                historyService.saveHistory(history);
                log.info("保存八字分析历史成功，历史ID：{}", history.getId());
            } catch (Exception e) {
                log.error("保存分析历史失败，但不影响返回结果", e);
            }
            
            log.info("成功返回格式化八字数据: gender={}, bazi={}", 
                    formatted.getGender(), formatted.getBazi());
            
            return formatted;
        } catch (Exception e) {
            log.error("获取格式化八字数据失败", e);
            throw e;
        }
    }
}