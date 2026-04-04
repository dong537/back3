package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.request.TokenReportRequest;
import com.example.demo.entity.TokenUsage;
import com.example.demo.service.TokenUsageService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenUsageController {

    private final TokenUsageService tokenUsageService;
    private final AuthUtil authUtil;

    /**
     * Token 消耗上报接口
     * POST /api/v1/tokens/report
     *
     * 认证方式: Authorization: Bearer <JWT token>
     * 请求体: { agentId, tokensUsed, startedAt, endedAt, ... }
     */
    @PostMapping("/report")
    public Result<Map<String, Object>> reportTokenUsage(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody TokenReportRequest request) {
        Long userId = authUtil.requireUserId(token);

        TokenUsage usage = tokenUsageService.reportTokenUsage(userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("id", usage.getId());
        result.put("agentId", usage.getAgentId());
        result.put("tokensUsed", usage.getTokensUsed());
        result.put("startedAt", usage.getStartedAt().toString());
        result.put("endedAt", usage.getEndedAt().toString());
        result.put("modelName", usage.getModelName());

        return Result.success(result);
    }

    /**
     * 查询用户 Token 消耗汇总
     * GET /api/v1/tokens/summary
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> getUsageSummary(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        Map<String, Object> summary = tokenUsageService.getUsageSummary(userId);
        return Result.success(summary);
    }

    /**
     * 查询用户 Token 消耗记录列表
     * GET /api/v1/tokens/records?page=1&size=20
     */
    @GetMapping("/records")
    public Result<Map<String, Object>> getUsageRecords(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = authUtil.requireUserId(token);

        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;

        List<TokenUsage> records = tokenUsageService.getUserUsageRecords(userId, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }
}
