package com.example.demo.dto.request.ziwei;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 将紫微命盘输出交给 DeepSeek 进行解读的请求体
 */
@Data
public class ZiweiDeepSeekInterpretRequest {

    /**
     * 命盘ID（来自 MCP 生成结果）
     */
    @NotBlank(message = "chartId 不能为空")
    private String chartId;

    /**
     * 紫微命盘详情（即 MCP 返回的 chart 对象）
     */
    @NotNull(message = "chart 数据不能为空")
    private Map<String, Object> chart;

    /**
     * MCP 返回的 summary，可选
     */
    private Map<String, Object> summary;

    /**
     * 用户关注的问题 / 解读重点，可选
     */
    private String focus;

    /**
     * 额外说明或上下文，可选
     */
    private String question;
}

