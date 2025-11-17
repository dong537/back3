package com.example.demo.dto.response.ziwei;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpZiweiResponse {
    private boolean success;
    private String chartId;  // 工具1生成的ID
    private Object data;     // 具体业务数据
    private String raw;      // 原始响应
    private String message;  // 错误信息（如有）
}