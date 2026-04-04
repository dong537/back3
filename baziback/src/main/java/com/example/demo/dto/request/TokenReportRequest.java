package com.example.demo.dto.request;

import lombok.Data;

@Data
public class TokenReportRequest {
    /** Agent ID（必填） */
    private String agentId;

    /** 应用 ID（可选，默认从 ApiKey 推断） */
    private String applicationId;

    /** 总 token 消耗量（必填，>0） */
    private Integer tokensUsed;

    /** 输入 token 数（可选） */
    private Integer inputTokens;

    /** 输出 token 数（可选） */
    private Integer outputTokens;

    /** 调用开始时间（必填，ISO 8601） */
    private String startedAt;

    /** 调用结束时间（必填，ISO 8601） */
    private String endedAt;

    /** AI 模型名称，如 gpt-4、claude-3（可选） */
    private String modelName;

    /** 请求追踪 ID，用于关联外部系统日志（可选） */
    private String requestId;

    /** 扩展 JSON 数据（可选） */
    private Object metadata;
}
