package com.example.demo.controller;


import com.example.demo.dto.request.ziwei.*;
import com.example.demo.dto.response.ziwei.McpZiweiResponse;
import com.example.demo.service.DeepSeekService;
import com.example.demo.service.ZiweiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

/**
 * 紫微斗数MCP服务控制器
 * 提供命盘生成、解读、运势分析、合婚、择日、可视化等RESTful API接口
 */
@RestController
@Slf4j
@Validated
@RequestMapping("/api/ziwei")
@RequiredArgsConstructor
public class ZiweiController {

    private final ZiweiService ziweiService;
    private final DeepSeekService deepSeekService;

    /**
     * 获取可用工具列表
     * 返回MCP服务器支持的所有工具定义
     *
     * @return 工具列表JSON字符串
     */
    @GetMapping("/tools")
    public Mono<ResponseEntity<String>> listTools() {
        return asyncResponse("获取紫微工具列表", () -> ziweiService.listTools());
    }
    /**
     * 工具1：生成紫微斗数命盘
     * 根据出生信息生成命盘，返回chartId供后续工具使用
     *
     * 请求参数：
     * - birthDate: 出生日期（YYYY-MM-DD）
     * - birthTime: 出生时间（HH:mm）
     * - gender: 性别（male/female）
     * - birthLocation: 出生地点（可选）
     * - name: 姓名（可选）
     *
     * 示例请求：
     * {
     *   "birthDate": "1990-05-20",
     *   "birthTime": "14:30",
     *   "gender": "male",
     *   "birthLocation": "北京市",
     *   "name": "张三"
     * }
     *
     * 返回示例：
     * {
     *   "success": true,
     *   "chartId": "ziwei_8f3d9a1c-5b2e-4f6d-b9c8-7a1e3d5f9b2c",
     *   "data": { "chart": {...}, "stars": {...} },
     *   "raw": "..."
     * }
     *
     * @param request 命盘生成请求参数
     * @return 包含chartId的命盘数据
     */
    @PostMapping("/chart/generate")
    public Mono<ResponseEntity<Map<String, Object>>> generateChart(@RequestBody @Validated ZiweiGenerateChartRequest request) {
        return asyncResponse("生成紫微命盘", () -> toMap(ziweiService.generateChart(request)));
    }

    /**
     * 工具2：命盘解读
     * 对生成的命盘进行深度解读分析
     *
     * 请求参数：
     * - chartId: 工具1生成的命盘ID（必需）
     * - aspects: 解读方面数组（personality/career/wealth/relationships/health/family）
     * - detailLevel: 详细程度（basic/detailed/comprehensive）
     *
     * 示例请求：
     * {
     *   "chartId": "ziwei_8f3d9a1c-5b2e-4f6d-b9c8-7a1e3d5f9b2c",
     *   "aspects": ["personality", "career", "wealth"],
     *   "detailLevel": "detailed"
     * }
     *
     * @param request 命盘解读请求参数
     * @return 解读结果数据
     */
    @PostMapping("/chart/interpret")
    public Mono<ResponseEntity<Map<String, Object>>> interpretChart(@RequestBody @Validated ZiweiInterpretChartRequest request) {
        return asyncResponse("解读紫微命盘", () -> toMap(ziweiService.interpretChart(request)));
    }
    /**
     * 工具3：运势分析
     * 分析指定周期的运势走向
     *
     * 请求参数：
     * - chartId: 命盘ID（必需）
     * - period: 周期类型（current_year/next_year/decade/custom）
     * - startDate: 自定义开始日期（YYYY-MM-DD，period=custom时必需）
     * - endDate: 自定义结束日期（YYYY-MM-DD，period=custom时必需）
     * - aspects: 分析方面数组
     *
     * 示例请求（自定义周期）：
     * {
     *   "chartId": "ziwei_8f3d9a1c-5b2e-4f6d-b9c8-7a1e3d5f9b2c",
     *   "period": "custom",
     *   "startDate": "2025-01-01",
     *   "endDate": "2025-12-31",
     *   "aspects": ["career", "wealth", "relationships"]
     * }
     *
     * @param request 运势分析请求参数
     * @return 运势预测数据
     */
    @PostMapping("/fortune/analyze")
    public Mono<ResponseEntity<Map<String, Object>>> analyzeFortune(@RequestBody @Validated ZiweiAnalyzeFortuneRequest request) {
        return asyncResponse("紫微运势分析", () -> toMap(ziweiService.analyzeFortune(request)));
    }

    /**
     * 工具4：合婚分析
     * 双人命盘配对分析（需调用两次工具1获取两个chartId）
     *
     * 请求参数：
     * - chart1Id: 第一人命盘ID（必需）
     * - chart2Id: 第二人命盘ID（必需）
     * - analysisType: 分析类型（marriage/business/friendship）
     * - aspects: 分析维度数组（可选）
     *
     * 示例请求：
     * {
     *   "chart1Id": "ziwei_8f3d9a1c-5b2e-4f6d-b9c8-7a1e3d5f9b2c",
     *   "chart2Id": "ziwei_a1b2c3d4-5e6f-7g8h-9i0j-k1l2m3n4o5p6",
     *   "analysisType": "marriage",
     *   "aspects": ["personality_match", "wealth_compatibility"]
     * }
     *
     * @param request 合婚分析请求参数
     * @return 配对分析结果
     */
    @PostMapping("/compatibility/analyze")
    public Mono<ResponseEntity<Map<String, Object>>> analyzeCompatibility(@RequestBody @Validated ZiweiAnalyzeCompatibilityRequest request) {
        return asyncResponse("紫微合婚分析", () -> toMap(ziweiService.analyzeCompatibility(request)));
    }

    /**
     * 工具5：择日功能
     * 根据命盘选择吉日良辰
     *
     * 请求参数：
     * - chartId: 命盘ID（必需）
     * - eventType: 事件类型（marriage/business_opening/moving/travel等）
     * - dateRange: 日期范围对象（必需）
     *   - start: 开始日期（YYYY-MM-DD）
     *   - end: 结束日期（YYYY-MM-DD）
     * - preferences: 偏好设置（可选）
     *   - weekendsOnly: 仅周末
     *   - avoidHolidays: 避开节假日
     *
     * 示例请求：
     * {
     *   "chartId": "ziwei_8f3d9a1c-5b2e-4f6d-b9c8-7a1e3d5f9b2c",
     *   "eventType": "business_opening",
     *   "dateRange": {
     *     "start": "2025-08-01",
     *     "end": "2025-08-31"
     *   },
     *   "preferences": {
     *     "weekendsOnly": false,
     *     "avoidHolidays": true
     *   }
     * }
     *
     * @param request 择日请求参数
     * @return 推荐吉日列表
     */
    @PostMapping("/auspicious-date/select")
    public Mono<ResponseEntity<Map<String, Object>>> selectAuspiciousDate(@RequestBody @Validated ZiweiSelectAuspiciousDateRequest request) {
        return asyncResponse("紫微择日", () -> toMap(ziweiService.selectAuspiciousDate(request)));
    }

    /**
     * 工具6：生成可视化图表
     * 生成命盘可视化图片/网页
     *
     * 请求参数：
     * - chartId: 命盘ID（必需）
     * - visualizationType: 图表类型（traditional_chart/modern_wheel/palace_grid/star_map）
     * - includeElements: 包含元素数组（可选）
     * - colorscheme: 配色方案（classic/modern/minimalist）
     * - outputFormat: 输出格式（svg/png/html，默认png）
     *
     * 示例请求：
     * {
     *   "chartId": "ziwei_8f3d9a1c-5b2e-4f6d-b9c8-7a1e3d5f9b2c",
     *   "visualizationType": "traditional_chart",
     *   "includeElements": ["stars", "palaces", "four_transformations"],
     *   "colorscheme": "classic",
     *   "outputFormat": "png"
     * }
     *
     * @param request 可视化请求参数
     * @return 图表URL或Base64数据
     */
    @PostMapping("/visualization/generate")
    public Mono<ResponseEntity<Map<String, Object>>> generateVisualization(@RequestBody @Validated ZiweiGenerateVisualizationRequest request) {
        return asyncResponse("紫微可视化生成", () -> toMap(ziweiService.generateVisualization(request)));
    }

    /**
     * 统一响应格式转换
     * 将MCP响应转换为标准Map格式
     *
     * @param response MCP服务调用结果
     * @return 包含success、data、chartId、raw的标准响应Map
     */
    private Map<String, Object> toMap(McpZiweiResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", response != null && response.isSuccess());
        map.put("chartId", response == null ? null : response.getChartId());
        map.put("data", response == null ? null : response.getData());
        map.put("raw", response == null ? null : response.getRaw());
        map.put("message", response == null ? null : response.getMessage());
        return map;
    }

    private <T> Mono<ResponseEntity<T>> asyncResponse(String action, CheckedSupplier<T> supplier) {
        return Mono.fromCallable(() -> ResponseEntity.ok(supplier.get()))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("{}失败", action, e));
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}