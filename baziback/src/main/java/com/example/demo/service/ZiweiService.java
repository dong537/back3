package com.example.demo.service;


import com.example.demo.client.McpZiweiClient;
import com.example.demo.dto.request.ziwei.*;
import com.example.demo.dto.response.ziwei.McpZiweiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZiweiService {

    private final McpZiweiClient mcpZiweiClient;

    public String listTools() {
        return mcpZiweiClient.listAvailableTools();
    }

    public McpZiweiResponse generateChart(ZiweiGenerateChartRequest request) {
        log.info("生成紫微斗数命盘请求: birthDate={}, gender={}", request.getBirthDate(), request.getGender());
        return mcpZiweiClient.generateChart(request);
    }

    public McpZiweiResponse interpretChart(ZiweiInterpretChartRequest request) {
        log.info("解读紫微斗数命盘: chartId={}, aspects={}", request.getChartId(), request.getAspects());
        return mcpZiweiClient.interpretChart(request);
    }

    public McpZiweiResponse analyzeFortune(ZiweiAnalyzeFortuneRequest request) {
        log.info("分析运势: chartId={}, period={}", request.getChartId(), request.getPeriod());
        return mcpZiweiClient.analyzeFortune(request);
    }

    public McpZiweiResponse analyzeCompatibility(ZiweiAnalyzeCompatibilityRequest request) {
        log.info("合婚分析: chart1Id={}, chart2Id={}, type={}",
                request.getChart1Id(), request.getChart2Id(), request.getAnalysisType());
        return mcpZiweiClient.analyzeCompatibility(request);
    }

    public McpZiweiResponse selectAuspiciousDate(ZiweiSelectAuspiciousDateRequest request) {
        log.info("择日: chartId={}, eventType={}, dateRange={}",
                request.getChartId(), request.getEventType(), request.getDateRange());
        return mcpZiweiClient.selectAuspiciousDate(request);
    }

    public McpZiweiResponse generateVisualization(ZiweiGenerateVisualizationRequest request) {
        log.info("生成可视化: chartId={}, type={}", request.getChartId(), request.getVisualizationType());
        return mcpZiweiClient.generateVisualization(request);
    }
}