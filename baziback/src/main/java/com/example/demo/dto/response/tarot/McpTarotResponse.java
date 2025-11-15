package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.Map;

@Data
public class McpTarotResponse {

    /** 塔罗牌解读原始文本 */
    private String tarotText;
    /** 解析后的结构化数据（牌阵、每张牌含义、综合解读等） */
    private Map<String, Object> tarotData;
    /** MCP原始响应（调试用） */
    private String rawResponse;
}
