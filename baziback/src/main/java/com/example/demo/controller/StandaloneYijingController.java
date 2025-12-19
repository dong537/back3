package com.example.demo.controller;

import com.example.demo.dto.request.yijing.YijingGenerateHexagramRequest;
import com.example.demo.dto.request.yijing.YijingInterpretRequest;
import com.example.demo.yijing.service.StandaloneYijingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        } catch (Exception e) {
            log.error("生成卦象失败", e);
            return ResponseEntity.ok(toResponse(false, "生成卦象失败: " + e.getMessage(), null));
        }
    }

    @PostMapping("/hexagram/interpret")
    public ResponseEntity<Map<String, Object>> interpretHexagram(
            @Validated @RequestBody YijingInterpretRequest request) {
        log.info("接收解读卦象请求 - 问题: {}", request.getQuestion());
        
        try {
            Map<String, Object> result = standaloneYijingService.interpretHexagram(request);
            return ResponseEntity.ok(toResponse(true, "卦象解读成功", result));
        } catch (Exception e) {
            log.error("解读卦象失败", e);
            return ResponseEntity.ok(toResponse(false, "解读卦象失败: " + e.getMessage(), null));
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
            return ResponseEntity.ok(toResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/hexagram/{id}")
    public ResponseEntity<Map<String, Object>> getHexagramById(@PathVariable Integer id) {
        log.info("获取卦象详情 - ID: {}", id);
        
        try {
            Map<String, Object> hexagram = standaloneYijingService.getHexagramInfo(id);
            if (hexagram != null) {
                return ResponseEntity.ok(toResponse(true, "获取成功", hexagram));
            } else {
                return ResponseEntity.ok(toResponse(false, "未找到该卦象", null));
            }
        } catch (Exception e) {
            log.error("获取卦象详情失败", e);
            return ResponseEntity.ok(toResponse(false, "获取失败: " + e.getMessage(), null));
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
    public ResponseEntity<Map<String, Object>> quickDivination(@RequestBody Map<String, String> request) {
        String question = request.getOrDefault("question", "请指点迷津");
        String method = request.getOrDefault("method", "time");
        
        log.info("快速占卜 - 问题: {}, 方法: {}", question, method);
        
        try {
            YijingGenerateHexagramRequest generateRequest = YijingGenerateHexagramRequest.builder()
                    .question(question)
                    .method(method)
                    .build();
            
            Map<String, Object> generateResult = standaloneYijingService.generateHexagram(generateRequest);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> original = (Map<String, Object>) generateResult.get("original");
            
            Map<String, Object> quickResult = new HashMap<>();
            quickResult.put("question", question);
            quickResult.put("hexagram_name", original.get("chinese"));
            quickResult.put("judgment", original.get("judgment"));
            quickResult.put("image", original.get("image"));
            quickResult.put("meaning", original.get("meaning"));
            quickResult.put("keywords", original.get("keywords"));
            quickResult.put("applications", original.get("applications"));
            quickResult.put("interpretation_hint", generateResult.get("interpretation_hint"));
            
            return ResponseEntity.ok(toResponse(true, "占卜成功", quickResult));
        } catch (Exception e) {
            log.error("快速占卜失败", e);
            return ResponseEntity.ok(toResponse(false, "占卜失败: " + e.getMessage(), null));
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
