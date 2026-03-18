package com.example.demo.bazi.controller;

import com.example.demo.bazi.service.BaZiService;
import com.example.demo.bazi.service.BaZiDeepSeekService;
import com.example.demo.bazi.util.DateToBaZiConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 八字测算控制器
 */
@RestController
@RequestMapping("/api/bazi")
@CrossOrigin
@Slf4j
public class BaZiController {

    @Autowired
    private BaZiService baZiService;

    @Autowired
    private BaZiDeepSeekService baZiDeepSeekService;

    /**
     * 完整八字分析
     * @param request 请求体，包含 baZi, birthYear, isMale, qiYunAge
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            Integer birthYear = (Integer) request.get("birthYear");
            Boolean isMale = (Boolean) request.getOrDefault("isMale", true);
            Integer qiYunAge = (Integer) request.getOrDefault("qiYunAge", 4);
            
            if (baZi == null || baZi.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字不能为空")
                );
            }
            
            if (birthYear == null) {
                birthYear = 2000; // 默认年份
            }
            
            Map<String, Object> result = baZiService.analyze(baZi, birthYear, isMale, qiYunAge);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "分析失败: " + e.getMessage())
            );
        }
    }

    /**
     * 简易八字分析（不包含大运）
     */
    @PostMapping("/analyze/simple")
    public ResponseEntity<Map<String, Object>> analyzeSimple(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            Boolean isMale = (Boolean) request.getOrDefault("isMale", true);
            
            if (baZi == null || baZi.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字不能为空")
                );
            }
            
            Map<String, Object> result = baZiService.analyzeSimple(baZi, isMale);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "分析失败: " + e.getMessage())
            );
        }
    }

    /**
     * GET方式分析八字
     */
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeGet(
            @RequestParam String baZi,
            @RequestParam(defaultValue = "2000") int birthYear,
            @RequestParam(defaultValue = "true") boolean isMale,
            @RequestParam(defaultValue = "4") int qiYunAge) {
        try {
            Map<String, Object> result = baZiService.analyze(baZi, birthYear, isMale, qiYunAge);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "分析失败: " + e.getMessage())
            );
        }
    }

    /**
     * 分析大运
     */
    @PostMapping("/dayun")
    public ResponseEntity<Map<String, Object>> analyzeDaYun(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            Integer birthYear = (Integer) request.get("birthYear");
            Boolean isMale = (Boolean) request.getOrDefault("isMale", true);
            
            if (baZi == null || birthYear == null) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字和出生年份不能为空")
                );
            }
            
            Map<String, Object> result = baZiService.analyzeDaYun(baZi, birthYear, isMale);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "分析失败: " + e.getMessage())
            );
        }
    }

    /**
     * 分析刑冲合会
     */
    @PostMapping("/xingchonghe")
    public ResponseEntity<?> analyzeXingChongHeHui(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            
            if (baZi == null) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字不能为空")
                );
            }
            
            return ResponseEntity.ok(baZiService.analyzeXingChongHeHui(baZi));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "分析失败: " + e.getMessage())
            );
        }
    }

    /**
     * 分析神煞
     */
    @PostMapping("/shensha")
    public ResponseEntity<?> analyzeShenSha(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            
            if (baZi == null) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字不能为空")
                );
            }
            
            return ResponseEntity.ok(baZiService.analyzeShenSha(baZi));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "分析失败: " + e.getMessage())
            );
        }
    }

    /**
     * 分析喜用神
     */
    @PostMapping("/xiyongshen")
    public ResponseEntity<?> analyzeXiYongShen(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            
            if (baZi == null) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字不能为空")
                );
            }
            
            return ResponseEntity.ok(baZiService.analyzeXiYongShen(baZi));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "分析失败: " + e.getMessage())
            );
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "八字测算服务");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据出生日期时间生成八字并进行完整分析
     * @param request 请求体，包含 birthDateTime (格式: yyyy-MM-dd HH:mm:ss), isMale, qiYunAge
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateBaZi(@RequestBody Map<String, Object> request) {
        try {
            String birthDateTime = (String) request.get("birthDateTime");
            Boolean isMale = (Boolean) request.getOrDefault("isMale", true);
            Integer qiYunAge = (Integer) request.getOrDefault("qiYunAge", 4);
            Double longitude = request.get("longitude") == null ? null : Double.valueOf(request.get("longitude").toString());
            
            if (birthDateTime == null || birthDateTime.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "出生日期时间不能为空，格式：yyyy-MM-dd HH:mm:ss")
                );
            }
            
            // 根据出生日期时间生成八字
            String baZi = DateToBaZiConverter.convert(birthDateTime, longitude);
            
            // 提取出生年份
            int birthYear = Integer.parseInt(birthDateTime.substring(0, 4));
            
            // 获取详细的日期转换信息
            Map<String, Object> dateInfo = DateToBaZiConverter.convertDetailed(birthDateTime, isMale, longitude);
            
            // 进行完整的八字分析
            Map<String, Object> analysisResult = baZiService.analyze(baZi, birthYear, isMale, qiYunAge);
            
            // 合并日期信息和分析结果
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("出生时间", dateInfo.get("出生时间"));
            // 附带换算信息（如传入 longitude）
            if (dateInfo.containsKey("经度")) {
                result.put("经度", dateInfo.get("经度"));
                result.put("真太阳时", dateInfo.get("真太阳时"));
                result.put("真太阳时偏移分钟", dateInfo.get("真太阳时偏移分钟"));
            }
            result.put("计算用时间", dateInfo.get("计算用时间"));
            if (dateInfo.containsKey("夜子时说明")) {
                result.put("夜子时说明", dateInfo.get("夜子时说明"));
            }

            result.put("性别", isMale ? "男" : "女");
            result.putAll(analysisResult);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // 记录完整堆栈，便于定位 Zip / Jar 读取错误来源
            log.error("生成八字失败", e);
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "生成八字失败: " + e.getMessage())
            );
        }
    }

    /**
     * 仅根据出生日期时间生成八字（不进行分析）
     * @param request 请求体，包含 birthDateTime (格式: yyyy-MM-dd HH:mm:ss), isMale
     */
    @PostMapping("/convert")
    public ResponseEntity<?> convertDateToBaZi(@RequestBody Map<String, Object> request) {
        try {
            String birthDateTime = (String) request.get("birthDateTime");
            Boolean isMale = (Boolean) request.getOrDefault("isMale", true);
            Double longitude = request.get("longitude") == null ? null : Double.valueOf(request.get("longitude").toString());
            
            if (birthDateTime == null || birthDateTime.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "出生日期时间不能为空，格式：yyyy-MM-dd HH:mm:ss")
                );
            }
            
            // 获取详细的八字信息
            Map<String, Object> result = DateToBaZiConverter.convertDetailed(birthDateTime, isMale, longitude);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "转换失败: " + e.getMessage())
            );
        }
    }

    /**
     * DeepSeek AI解读八字报告
     * @param request 请求体，包含 baZi, birthYear, isMale, qiYunAge
     */
    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            Integer birthYear = (Integer) request.get("birthYear");
            Boolean isMale = (Boolean) request.getOrDefault("isMale", true);
            Integer qiYunAge = (Integer) request.getOrDefault("qiYunAge", 4);
            
            if (baZi == null || baZi.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字不能为空")
                );
            }
            
            if (birthYear == null) {
                birthYear = 2000;
            }
            
            // 先进行八字分析
            Map<String, Object> analysisResult = baZiService.analyze(baZi, birthYear, isMale, qiYunAge);
            
            // 再使用DeepSeek进行AI解读
            String report = baZiDeepSeekService.interpretAnalysis(analysisResult);
            
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis", analysisResult);
            result.put("report", report);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "生成报告失败: " + e.getMessage())
            );
        }
    }

    /**
     * DeepSeek AI解读（直接传入提示词）
     * @param rawPrompt 原始提示词或JSON数据
     */
    @PostMapping("/interpret")
    public ResponseEntity<?> interpret(@RequestBody String rawPrompt) {
        try {
            String report = baZiDeepSeekService.generateBaziReport(rawPrompt);
            return ResponseEntity.ok(Collections.singletonMap("report", report));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "解读失败: " + e.getMessage())
            );
        }
    }

    /**
     * DeepSeek AI解读特定方面
     * @param request 请求体，包含 baZi, birthYear, isMale, qiYunAge, aspects
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/interpret-aspects")
    public ResponseEntity<?> interpretAspects(@RequestBody Map<String, Object> request) {
        try {
            String baZi = (String) request.get("baZi");
            Integer birthYear = (Integer) request.get("birthYear");
            Boolean isMale = (Boolean) request.getOrDefault("isMale", true);
            Integer qiYunAge = (Integer) request.getOrDefault("qiYunAge", 4);
            List<String> aspects = (List<String>) request.getOrDefault("aspects", 
                Arrays.asList("事业", "财运", "婚姻", "健康"));
            
            if (baZi == null || baZi.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "八字不能为空")
                );
            }
            
            if (birthYear == null) {
                birthYear = 2000;
            }
            
            // 先进行八字分析
            Map<String, Object> analysisResult = baZiService.analyze(baZi, birthYear, isMale, qiYunAge);
            String analysisJson = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(analysisResult);
            
            // 使用DeepSeek进行特定方面解读
            String report = baZiDeepSeekService.generateAspectReport(analysisJson, aspects);
            
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis", analysisResult);
            result.put("aspects", aspects);
            result.put("report", report);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", "解读失败: " + e.getMessage())
            );
        }
    }
}
