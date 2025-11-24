package com.example.demo.controller;

import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.report.GenerateReportRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.dto.response.analysis.AnalysisHistoryResponse;
import com.example.demo.dto.response.analysis.AnalysisReportResponse;
import com.example.demo.entity.AnalysisHistory;
import com.example.demo.entity.AnalysisReport;
import com.example.demo.entity.UserBaziInfo;
import com.example.demo.service.AnalysisHistoryService;
import com.example.demo.service.AnalysisReportService;
import com.example.demo.service.DeepSeekService;
import com.example.demo.service.UserBaziInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分析控制器
 */
@Slf4j
@Tag(name = "分析管理", description = "分析历史和报告管理")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    
    private final AnalysisHistoryService historyService;
    private final AnalysisReportService reportService;
    private final DeepSeekService deepSeekService;
    private final UserBaziInfoService baziInfoService;
    private final ObjectMapper objectMapper;
    
    // ========== 分析历史 ==========
    
    /**
     * 获取分析历史列表
     */
    @Operation(summary = "获取分析历史", description = "分页获取用户的分析历史记录")
    @RequireAuth
    @GetMapping("/history")
    public Result<List<AnalysisHistoryResponse>> getHistoryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String analysisType,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("获取分析历史，用户ID：{}，页码：{}，类型：{}", userId, page, analysisType);
            
            List<AnalysisHistory> list;
            if (analysisType != null && !analysisType.isEmpty()) {
                list = historyService.getUserHistoryByType(userId, analysisType, page, pageSize);
            } else {
                list = historyService.getUserHistoryList(userId, page, pageSize);
            }
            
            List<AnalysisHistoryResponse> responseList = list.stream()
                    .map(this::convertToHistoryResponse)
                    .collect(Collectors.toList());
            
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取分析历史失败", e);
            return Result.error("获取分析历史失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取分析历史详情
     */
    @Operation(summary = "获取历史详情", description = "获取指定分析历史的详细信息")
    @RequireAuth
    @GetMapping("/history/{id}")
    public Result<AnalysisHistoryResponse> getHistory(@PathVariable Long id) {
        try {
            log.info("获取分析历史详情，历史ID：{}", id);
            
            AnalysisHistory history = historyService.getHistory(id);
            AnalysisHistoryResponse response = convertToHistoryResponse(history);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取分析历史详情失败", e);
            return Result.error("获取分析历史详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 收藏/取消收藏分析历史
     */
    @Operation(summary = "收藏/取消收藏", description = "切换分析历史的收藏状态")
    @RequireAuth
    @PostMapping("/history/{id}/favorite")
    public Result<Void> toggleFavorite(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("切换收藏状态，用户ID：{}，历史ID：{}", userId, id);
            
            historyService.toggleFavorite(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("切换收藏状态失败", e);
            return Result.error("切换收藏状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取收藏列表
     */
    @Operation(summary = "获取收藏列表", description = "获取用户收藏的分析历史")
    @RequireAuth
    @GetMapping("/history/favorites")
    public Result<List<AnalysisHistoryResponse>> getFavorites(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("获取收藏列表，用户ID：{}", userId);
            
            List<AnalysisHistory> list = historyService.getUserFavorites(userId);
            List<AnalysisHistoryResponse> responseList = list.stream()
                    .map(this::convertToHistoryResponse)
                    .collect(Collectors.toList());
            
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return Result.error("获取收藏列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取分析统计
     */
    @Operation(summary = "获取分析统计", description = "获取用户的分析次数统计")
    @RequireAuth
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("获取分析统计，用户ID：{}", userId);
            
            int totalCount = historyService.countUserAnalysis(userId);
            int baziCount = historyService.countUserAnalysisByType(userId, "bazi");
            int tarotCount = historyService.countUserAnalysisByType(userId, "tarot");
            int yijingCount = historyService.countUserAnalysisByType(userId, "yijing");
            int ziweiCount = historyService.countUserAnalysisByType(userId, "ziwei");
            int zodiacCount = historyService.countUserAnalysisByType(userId, "zodiac");
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", totalCount);
            statistics.put("baziCount", baziCount);
            statistics.put("tarotCount", tarotCount);
            statistics.put("yijingCount", yijingCount);
            statistics.put("ziweiCount", ziweiCount);
            statistics.put("zodiacCount", zodiacCount);
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取分析统计失败", e);
            return Result.error("获取分析统计失败：" + e.getMessage());
        }
    }
    
    // ========== 分析报告 ==========
    
    /**
     * 获取报告列表
     */
    @Operation(summary = "获取报告列表", description = "分页获取用户的分析报告")
    @RequireAuth
    @GetMapping("/reports")
    public Result<List<AnalysisReportResponse>> getReportList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String reportType,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("获取报告列表，用户ID：{}，页码：{}，类型：{}", userId, page, reportType);
            
            List<AnalysisReport> list;
            if (reportType != null && !reportType.isEmpty()) {
                list = reportService.getUserReportByType(userId, reportType, page, pageSize);
            } else {
                list = reportService.getUserReportList(userId, page, pageSize);
            }
            
            List<AnalysisReportResponse> responseList = list.stream()
                    .map(this::convertToReportResponse)
                    .collect(Collectors.toList());
            
            return Result.success(responseList);
        } catch (Exception e) {
            log.error("获取报告列表失败", e);
            return Result.error("获取报告列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取报告详情
     */
    @Operation(summary = "获取报告详情", description = "获取指定报告的详细内容")
    @RequireAuth
    @GetMapping("/report/{id}")
    public Result<AnalysisReportResponse> getReport(@PathVariable Long id) {
        try {
            log.info("获取报告详情，报告ID：{}", id);
            
            AnalysisReport report = reportService.getReport(id);
            AnalysisReportResponse response = convertToReportResponse(report);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取报告详情失败", e);
            return Result.error("获取报告详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 导出报告
     */
    @Operation(summary = "导出报告", description = "导出指定报告（增加导出次数）")
    @RequireAuth
    @PostMapping("/report/{id}/export")
    public Result<Void> exportReport(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("导出报告，用户ID：{}，报告ID：{}", userId, id);
            
            reportService.exportReport(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("导出报告失败", e);
            return Result.error("导出报告失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成深度分析报告
     */
    @Operation(summary = "生成深度分析报告", description = "基于DeepSeek生成深度分析报告")
    @RequireAuth
    @PostMapping("/report/generate")
    public Result<AnalysisReportResponse> generateReport(
            @Validated @RequestBody GenerateReportRequest request,
            HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            log.info("生成深度分析报告，用户ID：{}，报告类型：{}", userId, request.getReportType());
            
            // 1. 获取八字信息
            UserBaziInfo baziInfo;
            if (request.getBaziInfoId() != null) {
                baziInfo = baziInfoService.getBaziInfo(request.getBaziInfoId());
            } else {
                baziInfo = baziInfoService.getDefaultBaziInfo(userId);
            }
            
            if (baziInfo == null) {
                return Result.error("未找到八字信息，请先创建八字信息");
            }
            
            // 2. 准备八字数据
            String baziData = prepareBaziData(baziInfo);
            
            // 3. 调用DeepSeek生成报告
            String reportContent = deepSeekService.generateDetailedReport(baziData, request.getReportType());
            
            // 4. 生成报告标题
            String reportTitle = request.getReportTitle();
            if (reportTitle == null || reportTitle.isEmpty()) {
                reportTitle = generateReportTitle(baziInfo.getName(), request.getReportType());
            }
            
            // 5. 保存报告
            AnalysisReport report = new AnalysisReport();
            report.setUserId(userId);
            report.setBaziInfoId(baziInfo.getId());
            report.setReportType(request.getReportType());
            report.setReportTitle(reportTitle);
            report.setReportContent(reportContent);
            report.setStatus(request.getPublish() != null && request.getPublish() ? 1 : 0);
            
            // 构建报告数据JSON
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("baziInfo", baziInfo);
            reportData.put("generatedAt", LocalDateTime.now());
            reportData.put("duration", System.currentTimeMillis() - startTime);
            report.setReportData(objectMapper.writeValueAsString(reportData));
            
            AnalysisReport created = reportService.createReport(report);
            
            log.info("生成深度分析报告成功，报告ID：{}，耗时：{}ms", 
                    created.getId(), System.currentTimeMillis() - startTime);
            
            AnalysisReportResponse response = convertToReportResponse(created);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("生成深度分析报告失败", e);
            return Result.error("生成深度分析报告失败：" + e.getMessage());
        }
    }
    
    /**
     * 准备八字数据
     */
    private String prepareBaziData(UserBaziInfo baziInfo) {
        StringBuilder data = new StringBuilder();
        data.append("姓名：").append(baziInfo.getName()).append("\n");
        data.append("性别：").append(baziInfo.getGender() == 1 ? "男" : "女").append("\n");
        data.append("出生时间：").append(baziInfo.getBirthYear()).append("年")
            .append(baziInfo.getBirthMonth()).append("月")
            .append(baziInfo.getBirthDay()).append("日 ")
            .append(baziInfo.getBirthHour()).append("时")
            .append(baziInfo.getBirthMinute()).append("分\n");
        data.append("历法：").append(baziInfo.getIsLunar() == 1 ? "农历" : "公历").append("\n");
        
        if (baziInfo.getBirthplace() != null) {
            data.append("出生地：").append(baziInfo.getBirthplace()).append("\n");
        }
        
        if (baziInfo.getBaziData() != null) {
            data.append("\n八字数据：\n").append(baziInfo.getBaziData());
        }
        
        return data.toString();
    }
    
    /**
     * 生成报告标题
     */
    private String generateReportTitle(String name, String reportType) {
        String typeName = getReportTypeDesc(reportType);
        return name + "的" + typeName;
    }
    
    // ========== 转换方法 ==========
    
    private AnalysisHistoryResponse convertToHistoryResponse(AnalysisHistory history) {
        return AnalysisHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .baziInfoId(history.getBaziInfoId())
                .analysisType(history.getAnalysisType())
                .analysisTypeDesc(getAnalysisTypeDesc(history.getAnalysisType()))
                .requestData(history.getRequestData())
                .responseData(history.getResponseData())
                .reportId(history.getReportId())
                .analysisDuration(history.getAnalysisDuration())
                .modelVersion(history.getModelVersion())
                .isFavorite(history.getIsFavorite())
                .createTime(history.getCreateTime())
                .build();
    }
    
    private AnalysisReportResponse convertToReportResponse(AnalysisReport report) {
        return AnalysisReportResponse.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .baziInfoId(report.getBaziInfoId())
                .reportType(report.getReportType())
                .reportTypeDesc(getReportTypeDesc(report.getReportType()))
                .reportTitle(report.getReportTitle())
                .reportContent(report.getReportContent())
                .reportData(report.getReportData())
                .version(report.getVersion())
                .status(report.getStatus())
                .statusDesc(getStatusDesc(report.getStatus()))
                .viewCount(report.getViewCount())
                .exportCount(report.getExportCount())
                .lastViewTime(report.getLastViewTime())
                .createTime(report.getCreateTime())
                .updateTime(report.getUpdateTime())
                .build();
    }
    
    private String getAnalysisTypeDesc(String type) {
        if (type == null) return "";
        switch (type) {
            case "bazi": return "八字分析";
            case "tarot": return "塔罗占卜";
            case "yijing": return "易经占卜";
            case "ziwei": return "紫微斗数";
            case "zodiac": return "星座运势";
            default: return type;
        }
    }
    
    private String getReportTypeDesc(String type) {
        if (type == null) return "";
        switch (type) {
            case "comprehensive": return "综合报告";
            case "career": return "事业报告";
            case "love": return "感情报告";
            case "health": return "健康报告";
            case "wealth": return "财运报告";
            default: return type;
        }
    }
    
    private String getStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            case 2: return "已归档";
            default: return "未知";
        }
    }
}
