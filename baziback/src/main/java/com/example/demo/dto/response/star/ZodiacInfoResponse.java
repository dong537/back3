package com.example.demo.dto.response.star;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZodiacInfoResponse {
    private String name;        // 中文名称
    private String englishName; // 英文名称
    private String dateRange;   // 日期范围（如"3月21日-4月19日"）
    private String personality; // 性格描述（逗号分隔）
    private String description; // 详细描述
    private String rawContent;  // MCP原始内容（Markdown/文本）
}