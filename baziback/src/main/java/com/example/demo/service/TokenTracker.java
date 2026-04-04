package com.example.demo.service;

import com.example.demo.entity.TokenUsage;
import com.example.demo.mapper.TokenUsageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 从 OpenAI 兼容 API 的响应中提取 token 使用量并记录到 tb_token_usage 表。
 * GeminiService 和 DeepSeekService 在每次 API 调用后调用此组件。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TokenTracker {

    private final TokenUsageMapper tokenUsageMapper;
    private final ObjectMapper objectMapper;

    /**
     * 从 API 响应体中提取 usage 并异步记录。
     *
     * @param responseBody  原始 JSON 响应
     * @param modelName     使用的模型名称
     * @param agentId       来源标识（如 "gemini-face", "gemini-scene-image", "deepseek-bazi"）
     * @param startedAt     调用开始时间
     */
    @SuppressWarnings("unchecked")
    public void trackFromResponse(String responseBody, String modelName, String agentId, LocalDateTime startedAt) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            if (usage == null) {
                log.debug("API 响应中无 usage 字段，跳过 token 记录: agentId={}", agentId);
                return;
            }

            Integer promptTokens = toInt(usage.get("prompt_tokens"));
            Integer completionTokens = toInt(usage.get("completion_tokens"));
            Integer totalTokens = toInt(usage.get("total_tokens"));

            // 如果 total_tokens 缺失，从 prompt + completion 计算
            if (totalTokens == null || totalTokens == 0) {
                int p = promptTokens != null ? promptTokens : 0;
                int c = completionTokens != null ? completionTokens : 0;
                totalTokens = p + c;
            }

            if (totalTokens <= 0) {
                log.debug("Token 使用量为 0，跳过记录: agentId={}", agentId);
                return;
            }

            LocalDateTime endedAt = LocalDateTime.now();

            TokenUsage tokenUsage = TokenUsage.builder()
                    .agentId(agentId)
                    .applicationId("baziback")
                    .userId(0L) // 系统级记录，不关联特定用户
                    .tokensUsed(totalTokens)
                    .inputTokens(promptTokens)
                    .outputTokens(completionTokens)
                    .startedAt(startedAt)
                    .endedAt(endedAt)
                    .modelName(modelName)
                    .build();

            tokenUsageMapper.insert(tokenUsage);
            log.info("Token 消耗已记录: agentId={}, model={}, total={}, input={}, output={}",
                    agentId, modelName, totalTokens, promptTokens, completionTokens);

        } catch (Exception e) {
            // token 记录失败不影响主流程
            log.warn("Token 消耗记录失败: agentId={}, error={}", agentId, e.getMessage());
        }
    }

    /**
     * 带用户 ID 的 token 记录
     */
    @SuppressWarnings("unchecked")
    public void trackFromResponse(String responseBody, String modelName, String agentId,
                                  LocalDateTime startedAt, Long userId) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            if (usage == null) {
                log.debug("API 响应中无 usage 字段，跳过 token 记录: agentId={}", agentId);
                return;
            }

            Integer promptTokens = toInt(usage.get("prompt_tokens"));
            Integer completionTokens = toInt(usage.get("completion_tokens"));
            Integer totalTokens = toInt(usage.get("total_tokens"));

            if (totalTokens == null || totalTokens == 0) {
                int p = promptTokens != null ? promptTokens : 0;
                int c = completionTokens != null ? completionTokens : 0;
                totalTokens = p + c;
            }

            if (totalTokens <= 0) {
                return;
            }

            LocalDateTime endedAt = LocalDateTime.now();

            TokenUsage tokenUsage = TokenUsage.builder()
                    .agentId(agentId)
                    .applicationId("baziback")
                    .userId(userId != null ? userId : 0L)
                    .tokensUsed(totalTokens)
                    .inputTokens(promptTokens)
                    .outputTokens(completionTokens)
                    .startedAt(startedAt)
                    .endedAt(endedAt)
                    .modelName(modelName)
                    .build();

            tokenUsageMapper.insert(tokenUsage);
            log.info("Token 消耗已记录: agentId={}, model={}, userId={}, total={}, input={}, output={}",
                    agentId, modelName, userId, totalTokens, promptTokens, completionTokens);

        } catch (Exception e) {
            log.warn("Token 消耗记录失败: agentId={}, error={}", agentId, e.getMessage());
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
