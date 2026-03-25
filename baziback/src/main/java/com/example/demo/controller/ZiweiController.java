package com.example.demo.controller;

import com.example.demo.dto.request.ziwei.*;
import com.example.demo.dto.response.ziwei.McpZiweiResponse;
import com.example.demo.service.ZiweiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 紫微斗数MCP服务控制器
 * 保持原有返回结构（success/chartId/data/raw/message），但修正 HTTP 状态码：
 * - 参数错误 -> 400
 * - 服务器异常 -> 500
 */
@RestController
@ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
@Slf4j
@Validated
@RequestMapping("/api/ziwei")
@RequiredArgsConstructor
public class ZiweiController {

    private final ZiweiService ziweiService;

    @GetMapping("/tools")
    public ResponseEntity<String> listTools() {
        try {
            return ResponseEntity.ok(ziweiService.listTools());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("获取紫微工具列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取紫微工具列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/chart/generate")
    public ResponseEntity<Map<String, Object>> generateChart(@RequestBody @Validated ZiweiGenerateChartRequest request) {
        try {
            return ResponseEntity.ok(toMap(ziweiService.generateChart(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap(e.getMessage()));
        } catch (Exception e) {
            log.error("生成紫微命盘失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap("生成紫微命盘失败: " + e.getMessage()));
        }
    }

    @PostMapping("/chart/interpret")
    public ResponseEntity<Map<String, Object>> interpretChart(@RequestBody @Validated ZiweiInterpretChartRequest request) {
        try {
            return ResponseEntity.ok(toMap(ziweiService.interpretChart(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap(e.getMessage()));
        } catch (Exception e) {
            log.error("解读紫微命盘失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap("解读紫微命盘失败: " + e.getMessage()));
        }
    }

    @PostMapping("/fortune/analyze")
    public ResponseEntity<Map<String, Object>> analyzeFortune(@RequestBody @Validated ZiweiAnalyzeFortuneRequest request) {
        try {
            return ResponseEntity.ok(toMap(ziweiService.analyzeFortune(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap(e.getMessage()));
        } catch (Exception e) {
            log.error("紫微运势分析失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap("紫微运势分析失败: " + e.getMessage()));
        }
    }

    @PostMapping("/compatibility/analyze")
    public ResponseEntity<Map<String, Object>> analyzeCompatibility(@RequestBody @Validated ZiweiAnalyzeCompatibilityRequest request) {
        try {
            return ResponseEntity.ok(toMap(ziweiService.analyzeCompatibility(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap(e.getMessage()));
        } catch (Exception e) {
            log.error("紫微合婚分析失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap("紫微合婚分析失败: " + e.getMessage()));
        }
    }

    @PostMapping("/auspicious-date/select")
    public ResponseEntity<Map<String, Object>> selectAuspiciousDate(@RequestBody @Validated ZiweiSelectAuspiciousDateRequest request) {
        try {
            return ResponseEntity.ok(toMap(ziweiService.selectAuspiciousDate(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap(e.getMessage()));
        } catch (Exception e) {
            log.error("紫微择日失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap("紫微择日失败: " + e.getMessage()));
        }
    }

    @PostMapping("/visualization/generate")
    public ResponseEntity<Map<String, Object>> generateVisualization(@RequestBody @Validated ZiweiGenerateVisualizationRequest request) {
        try {
            return ResponseEntity.ok(toMap(ziweiService.generateVisualization(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap(e.getMessage()));
        } catch (Exception e) {
            log.error("紫微可视化生成失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap("紫微可视化生成失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> toMap(McpZiweiResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", response != null && response.isSuccess());
        map.put("chartId", response == null ? null : response.getChartId());
        map.put("data", response == null ? null : response.getData());
        map.put("raw", response == null ? null : response.getRaw());
        map.put("message", response == null ? null : response.getMessage());
        return map;
    }

    private Map<String, Object> errorMap(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        map.put("chartId", null);
        map.put("data", null);
        map.put("raw", null);
        return map;
    }
}
