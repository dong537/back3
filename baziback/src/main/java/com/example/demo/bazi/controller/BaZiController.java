package com.example.demo.bazi.controller;

import com.example.demo.bazi.service.BaZiDeepSeekService;
import com.example.demo.bazi.service.BaZiService;
import com.example.demo.bazi.util.DateToBaZiConverter;
import com.example.demo.dto.response.bazi.BaziAnalysisResponse;
import com.example.demo.dto.response.bazi.BaziDaYunResponse;
import com.example.demo.dto.response.bazi.BaziResponseMapper;
import com.example.demo.util.I18nUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bazi")
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
public class BaZiController {

    private final BaZiService baZiService;
    private final BaZiDeepSeekService baZiDeepSeekService;
    private final I18nUtil i18nUtil;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            Integer birthYear = asInteger(request.get("birthYear"));
            boolean isMale = asBoolean(request.get("isMale"), true);
            int qiYunAge = asInteger(request.get("qiYunAge")) != null ? asInteger(request.get("qiYunAge")) : 4;

            if (baZi == null || baZi.isBlank()) {
                return badRequest(i18nUtil.getErrorMessage("bazi.invalidInput"));
            }

            int resolvedBirthYear = birthYear != null ? birthYear : 2000;
            return ResponseEntity.ok(BaziResponseMapper.fromAnalysisMap(
                    baZiService.analyze(baZi, resolvedBirthYear, isMale, qiYunAge)
            ));
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
        }
    }

    @PostMapping("/analyze/simple")
    public ResponseEntity<?> analyzeSimple(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            boolean isMale = asBoolean(request.get("isMale"), true);

            if (baZi == null || baZi.isBlank()) {
                return badRequest(i18nUtil.getErrorMessage("bazi.invalidInput"));
            }

            return ResponseEntity.ok(BaziResponseMapper.fromAnalysisMap(
                    baZiService.analyzeSimple(baZi, isMale)
            ));
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
        }
    }

    @GetMapping("/analyze")
    public ResponseEntity<?> analyzeGet(@RequestParam String baZi,
                                        @RequestParam(defaultValue = "2000") int birthYear,
                                        @RequestParam(defaultValue = "true") boolean isMale,
                                        @RequestParam(defaultValue = "4") int qiYunAge) {
        try {
            return ResponseEntity.ok(BaziResponseMapper.fromAnalysisMap(
                    baZiService.analyze(baZi, birthYear, isMale, qiYunAge)
            ));
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
        }
    }

    @PostMapping("/dayun")
    public ResponseEntity<?> analyzeDaYun(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            Integer birthYear = asInteger(request.get("birthYear"));
            boolean isMale = asBoolean(request.get("isMale"), true);

            if (baZi == null || baZi.isBlank() || birthYear == null) {
                return badRequest(i18nUtil.getErrorMessage("bazi.invalidInput"));
            }

            BaziDaYunResponse response = BaziResponseMapper.fromAnalysisMap(
                    wrapDaYun(baZiService.analyzeDaYun(baZi, birthYear, isMale))
            ).getDaYun();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return badRequest("分析失败: " + e.getMessage());
        }
    }

    @PostMapping("/xingchonghe")
    public ResponseEntity<?> analyzeXingChongHeHui(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            if (baZi == null || baZi.isBlank()) {
                return badRequest(i18nUtil.getErrorMessage("bazi.invalidInput"));
            }
            return ResponseEntity.ok(baZiService.analyzeXingChongHeHui(baZi));
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
        }
    }

    @PostMapping("/shensha")
    public ResponseEntity<?> analyzeShenSha(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            if (baZi == null || baZi.isBlank()) {
                return badRequest(i18nUtil.getErrorMessage("bazi.invalidInput"));
            }
            return ResponseEntity.ok(baZiService.analyzeShenSha(baZi));
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
        }
    }

    @PostMapping("/xiyongshen")
    public ResponseEntity<?> analyzeXiYongShen(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            if (baZi == null || baZi.isBlank()) {
                return badRequest(i18nUtil.getErrorMessage("bazi.invalidInput"));
            }
            return ResponseEntity.ok(baZiService.analyzeXiYongShen(baZi));
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("service", "八字测算服务");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateBaZi(@RequestBody Map<String, Object> request) {
        try {
            String birthDateTime = asString(request.get("birthDateTime"));
            boolean isMale = asBoolean(request.get("isMale"), true);
            int qiYunAge = asInteger(request.get("qiYunAge")) != null ? asInteger(request.get("qiYunAge")) : 4;
            Double longitude = asDouble(request.get("longitude"));

            if (birthDateTime == null || birthDateTime.isBlank()) {
                return badRequest("出生日期时间不能为空，格式：yyyy-MM-dd HH:mm:ss");
            }

            String baZi = DateToBaZiConverter.convert(birthDateTime, longitude);
            int birthYear = Integer.parseInt(birthDateTime.substring(0, 4));
            Map<String, Object> dateInfo = DateToBaZiConverter.convertDetailed(birthDateTime, isMale, longitude);
            Map<String, Object> analysisResult = baZiService.analyze(baZi, birthYear, isMale, qiYunAge);

            Map<String, Object> result = new LinkedHashMap<>(analysisResult);
            result.put("出生时间", dateInfo.get("出生时间"));
            result.put("计算用时间", dateInfo.get("计算用时间"));
            result.put("性别", isMale ? "男" : "女");
            if (dateInfo.containsKey("经度")) {
                result.put("经度", dateInfo.get("经度"));
            }
            if (dateInfo.containsKey("真太阳时")) {
                result.put("真太阳时", dateInfo.get("真太阳时"));
            }
            if (dateInfo.containsKey("真太阳时偏移分钟")) {
                result.put("真太阳时偏移分钟", dateInfo.get("真太阳时偏移分钟"));
            }

            return ResponseEntity.ok(BaziResponseMapper.fromAnalysisMap(result));
        } catch (Exception e) {
            log.error("生成八字失败", e);
            return badRequest("生成八字失败: " + e.getMessage());
        }
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convertDateToBaZi(@RequestBody Map<String, Object> request) {
        try {
            String birthDateTime = asString(request.get("birthDateTime"));
            boolean isMale = asBoolean(request.get("isMale"), true);
            Double longitude = asDouble(request.get("longitude"));

            if (birthDateTime == null || birthDateTime.isBlank()) {
                return badRequest("出生日期时间不能为空，格式：yyyy-MM-dd HH:mm:ss");
            }

            return ResponseEntity.ok(DateToBaZiConverter.convertDetailed(birthDateTime, isMale, longitude));
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
        }
    }

    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            Integer birthYear = asInteger(request.get("birthYear"));
            boolean isMale = asBoolean(request.get("isMale"), true);
            int qiYunAge = asInteger(request.get("qiYunAge")) != null ? asInteger(request.get("qiYunAge")) : 4;

            if (baZi == null || baZi.isBlank()) {
                return badRequest("八字不能为空");
            }

            int resolvedBirthYear = birthYear != null ? birthYear : 2000;
            Map<String, Object> analysisResult = baZiService.analyze(baZi, resolvedBirthYear, isMale, qiYunAge);
            String report = baZiDeepSeekService.interpretAnalysis(analysisResult);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis", BaziResponseMapper.fromAnalysisMap(analysisResult));
            result.put("report", report);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return badRequest(i18nUtil.getErrorMessage("bazi.reportFailed", e.getMessage()));
        }
    }

    @PostMapping("/interpret")
    public ResponseEntity<?> interpret(@RequestBody String rawPrompt) {
        try {
            String report = baZiDeepSeekService.generateBaziReport(rawPrompt);
            return ResponseEntity.ok(Collections.singletonMap("report", report));
        } catch (Exception e) {
            return badRequest("解读失败: " + e.getMessage());
        }
    }

    @PostMapping("/interpret-aspects")
    public ResponseEntity<?> interpretAspects(@RequestBody Map<String, Object> request) {
        try {
            String baZi = asString(request.get("baZi"));
            Integer birthYear = asInteger(request.get("birthYear"));
            boolean isMale = asBoolean(request.get("isMale"), true);
            int qiYunAge = asInteger(request.get("qiYunAge")) != null ? asInteger(request.get("qiYunAge")) : 4;
            @SuppressWarnings("unchecked")
            List<String> aspects = request.get("aspects") instanceof List<?> list
                    ? list.stream().map(Object::toString).toList()
                    : Arrays.asList("事业", "财运", "婚姻", "健康");

            if (baZi == null || baZi.isBlank()) {
                return badRequest("八字不能为空");
            }

            int resolvedBirthYear = birthYear != null ? birthYear : 2000;
            Map<String, Object> analysisResult = baZiService.analyze(baZi, resolvedBirthYear, isMale, qiYunAge);
            String analysisJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(analysisResult);
            String report = baZiDeepSeekService.generateAspectReport(analysisJson, aspects);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis", BaziResponseMapper.fromAnalysisMap(analysisResult));
            result.put("aspects", aspects);
            result.put("report", report);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return badRequest("解读失败: " + e.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", message));
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer asInteger(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Double asDouble(Object value) {
        if (value instanceof Double aDouble) {
            return aDouble;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return defaultValue;
    }

    private Map<String, Object> wrapDaYun(Map<String, Object> daYun) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("大运数据", daYun);
        return result;
    }
}
