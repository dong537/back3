package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {
    private Long id;
    private String agentId;
    private String applicationId;
    private Long userId;
    private Integer tokensUsed;
    private Integer inputTokens;
    private Integer outputTokens;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String modelName;
    private String requestId;
    private String metadata;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
