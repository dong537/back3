package com.example.demo.controller;

import com.example.demo.yijing.service.LiuYaoDivinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 六爻占卜控制器
 * 保持原有返回结构（success/message/data），但修正 HTTP 状态码：
 * - 参数错误 -> 400
 * - 服务器异常 -> 500
 */
@RestController
@Slf4j
@RequestMapping("/api/liuyao")
@RequiredArgsConstructor
public class LiuYaoController {

    private final LiuYaoDivinationService liuYaoDivinationService;

    @PostMapping("/divination")
    public ResponseEntity<Map<String, Object>> divination(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(toResponse(false, "请求参数不能为空", null));
            }

            String question = (String) request.getOrDefault("question", "请指点迷津");
            String category = (String) request.getOrDefault("category", "自身");
            Boolean isMale = request.containsKey("is_male") ? (Boolean) request.get("is_male") : null;

            // 解析摇卦结果
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> coinResults = (List<Map<String, Object>>) request.get("coin_results");
            if (coinResults == null || coinResults.size() != 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(toResponse(false, "摇卦结果不完整，需要6次摇卦", null));
            }

            List<LiuYaoDivinationService.CoinResult> results = new ArrayList<>();
            for (Map<String, Object> coin : coinResults) {
                @SuppressWarnings("unchecked")
                List<Integer> coins = (List<Integer>) coin.get("coins");
                String yaoType = (String) coin.get("yao_type");
                Boolean dongYao = (Boolean) coin.get("is_dong_yao");
                Integer binary = (Integer) coin.get("binary");

                results.add(new LiuYaoDivinationService.CoinResult(
                        (Integer) coin.get("round"),
                        coins,
                        yaoType,
                        dongYao != null && dongYao,
                        binary != null ? binary : 0
                ));
            }

            // 解析日期
            String dateStr = (String) request.getOrDefault("divination_date", LocalDate.now().toString());
            LocalDate divinationDate = LocalDate.parse(dateStr);

            // 执行占卜
            Map<String, Object> result = liuYaoDivinationService.performDivination(
                    question, category, isMale, results, divinationDate
            );

            // 注意：成就检查现在在保存记录时自动触发（CalculationRecordService.saveRecord）
            // 前端会在占卜成功后自动保存记录，从而触发成就检查

            return ResponseEntity.ok(toResponse(true, "占卜成功", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(toResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("六爻占卜失败", e);
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
