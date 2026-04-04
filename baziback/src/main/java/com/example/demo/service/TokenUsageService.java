package com.example.demo.service;

import com.example.demo.dto.request.TokenReportRequest;
import com.example.demo.entity.TokenUsage;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TokenUsageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenUsageService {

    private final TokenUsageMapper tokenUsageMapper;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Transactional
    public TokenUsage reportTokenUsage(Long userId, TokenReportRequest request) {
        // 校验必填字段
        if (request.getAgentId() == null || request.getAgentId().isBlank()) {
            throw new BusinessException("agentId 不能为空", HttpStatus.BAD_REQUEST);
        }
        if (request.getTokensUsed() == null || request.getTokensUsed() <= 0) {
            throw new BusinessException("tokensUsed 必须大于 0", HttpStatus.BAD_REQUEST);
        }
        if (request.getStartedAt() == null || request.getStartedAt().isBlank()) {
            throw new BusinessException("startedAt 不能为空", HttpStatus.BAD_REQUEST);
        }
        if (request.getEndedAt() == null || request.getEndedAt().isBlank()) {
            throw new BusinessException("endedAt 不能为空", HttpStatus.BAD_REQUEST);
        }

        // 解析时间
        LocalDateTime startedAt = parseDateTime(request.getStartedAt(), "startedAt");
        LocalDateTime endedAt = parseDateTime(request.getEndedAt(), "endedAt");

        // 时间逻辑校验：endedAt 必须晚于 startedAt
        if (!endedAt.isAfter(startedAt)) {
            throw new BusinessException("endedAt 必须晚于 startedAt", HttpStatus.BAD_REQUEST);
        }

        // 序列化 metadata
        String metadataJson = null;
        if (request.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(request.getMetadata());
            } catch (Exception e) {
                throw new BusinessException("metadata 格式无效", HttpStatus.BAD_REQUEST);
            }
        }

        TokenUsage tokenUsage = TokenUsage.builder()
                .agentId(request.getAgentId())
                .applicationId(request.getApplicationId())
                .userId(userId)
                .tokensUsed(request.getTokensUsed())
                .inputTokens(request.getInputTokens())
                .outputTokens(request.getOutputTokens())
                .startedAt(startedAt)
                .endedAt(endedAt)
                .modelName(request.getModelName())
                .requestId(request.getRequestId())
                .metadata(metadataJson)
                .build();

        tokenUsageMapper.insert(tokenUsage);
        log.info("Token 消耗上报成功: userId={}, agentId={}, tokensUsed={}, model={}",
                userId, request.getAgentId(), request.getTokensUsed(), request.getModelName());

        return tokenUsage;
    }

    public Map<String, Object> getUsageSummary(Long userId) {
        Long totalTokens = tokenUsageMapper.sumTokensByUserId(userId);
        int totalRecords = tokenUsageMapper.countByUserId(userId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTokens", totalTokens);
        summary.put("totalRecords", totalRecords);
        return summary;
    }

    public List<TokenUsage> getUserUsageRecords(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return tokenUsageMapper.findByUserId(userId, size, offset);
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException(fieldName + " 时间格式无效，请使用 ISO 8601 格式（如 2026-03-26T10:30:00）",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
