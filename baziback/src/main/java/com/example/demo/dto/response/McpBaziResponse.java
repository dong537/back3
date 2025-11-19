package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpBaziResponse {
    /**
     * 八字原始文本（JSON 字符串）
     */
    private String baziText;

    /**
     * ✅ 新增：解析后的结构化八字数据
     * 包含完整的八字信息：性别、阳历、农历、八字、生肖、四柱、大运等
     */
    private Map<String, Object> baziData;
    /**
     * MCP 原始响应（用于调试）
     */
    private String rawResponse;
    /**
     * 获取性别
     */
    public String getGender() {
        return baziData != null ? (String) baziData.get("性别") : null;
    }

    /**
     * 获取八字
     */
    public String getBazi() {
        return baziData != null ? (String) baziData.get("八字") : null;
    }

}