package com.example.demo.controller;

import com.example.demo.dto.request.yijing.YijingGenerateHexagramRequest;
import com.example.demo.dto.request.yijing.YijingInterpretRequest;
import com.example.demo.yijing.service.StandaloneYijingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/standalone/yijing")
@RequiredArgsConstructor
public class StandaloneYijingController {

    private final StandaloneYijingService standaloneYijingService;

    @PostMapping("/hexagram/generate")
    public ResponseEntity<Map<String, Object>> generateHexagram(
            @Validated @RequestBody YijingGenerateHexagramRequest request) {
        log.info("接收生成卦象请求 - 问题: {}, 方法: {}", request.getQuestion(), request.getMethod());

        try {
            Map<String, Object> result = standaloneYijingService.generateHexagram(request);
            return ResponseEntity.ok(toResponse(true, "卦象生成成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(toResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("生成卦象失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(toResponse(false, "生成卦象失败: " + e.getMessage(), null));
        }
    }

    @PostMapping("/hexagram/interpret")
    public ResponseEntity<Map<String, Object>> interpretHexagram(
            @Validated @RequestBody YijingInterpretRequest request) {
        log.info("接收解读卦象请求 - 问题: {}", request.getQuestion());

        try {
            Map<String, Object> result = standaloneYijingService.interpretHexagram(request);
            return ResponseEntity.ok(toResponse(true, "卦象解读成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(toResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("解读卦象失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(toResponse(false, "解读卦象失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/hexagrams")
    public ResponseEntity<Map<String, Object>> listAllHexagrams() {
        log.info("获取所有卦象列表");

        try {
            List<Map<String, Object>> hexagrams = standaloneYijingService.listAllHexagrams();
            return ResponseEntity.ok(toResponse(true, "获取成功", Map.of("hexagrams", hexagrams, "total", hexagrams.size())));
        } catch (Exception e) {
            log.error("获取卦象列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(toResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/hexagram/{id}")
    public ResponseEntity<Map<String, Object>> getHexagramById(@PathVariable Integer id) {
        log.info("获取卦象详情 - ID: {}", id);

        try {
            Map<String, Object> hexagram = standaloneYijingService.getHexagramInfo(id);
            if (hexagram != null) {
                return ResponseEntity.ok(toResponse(true, "获取成功", hexagram));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(toResponse(false, "未找到该卦象", null));
        } catch (Exception e) {
            log.error("获取卦象详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(toResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> listMethods() {
        log.info("获取起卦方法列表");

        Map<String, String> methods = new HashMap<>();
        methods.put("time", "时间起卦 - 根据当前时间生成卦象");
        methods.put("random", "随机起卦 - 完全随机生成卦象");
        methods.put("number", "数字起卦 - 根据提供的数字种子生成卦象");
        methods.put("coin", "金钱卦 - 模拟投掷三枚硬币六次");
        methods.put("plum_blossom", "梅花易数 - 结合时间和外应生成卦象");

        return ResponseEntity.ok(toResponse(true, "获取成功", Map.of("methods", methods)));
    }

    @PostMapping("/quick-divination")
    public ResponseEntity<Map<String, Object>> quickDivination(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> request) {
        String question = request == null ? null : request.get("question");
        String method = request == null ? null : request.get("method");
        String seed = request == null ? null : request.get("seed");

        question = (question == null || question.isBlank()) ? "请指点迷津" : question;
        method = (method == null || method.isBlank()) ? "time" : method;
        seed = seed == null ? "" : seed;

        log.info("快速占卜 - 问题: {}, 方法: {}, 种子: {}", question, method, seed);

        try {
            YijingGenerateHexagramRequest generateRequest = YijingGenerateHexagramRequest.builder()
                    .question(question)
                    .method(method)
                    .seed(seed)
                    .build();

            Map<String, Object> generateResult = standaloneYijingService.generateHexagram(generateRequest);

            // 注意：成就检查现在在保存记录时自动触发（CalculationRecordService.saveRecord）
            // 前端会在占卜成功后自动保存记录，从而触发成就检查

            // 直接返回完整的生成结果，包含original和changed字段，与前端期望的结构一致
            return ResponseEntity.ok(toResponse(true, "占卜成功", generateResult));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(toResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("快速占卜失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(toResponse(false, "占卜失败: " + e.getMessage(), null));
        }
    }

    private Map<String, Object> toResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
}
