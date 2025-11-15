package com.example.demo.dto.request.tarot;

import lombok.Data;

@Data
public class McpTarotRequest {
    /** 占卜问题（必填） */
    private String question;
    /** 牌阵类型（如："三牌阵"、"六芒星阵"，可选） */
    private String arrayType;
    /** 用户性别（0-女，1-男，可选） */
    private Integer gender;
}
