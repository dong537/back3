package com.example.demo.dto.request.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成报告请求
 */
@Data
public class GenerateReportRequest {
    
    /**
     * 八字信息ID（可选，如果不提供则使用默认八字）
     */
    private Long baziInfoId;
    
    /**
     * 报告类型：comprehensive-综合，career-事业，love-感情，health-健康，wealth-财运
     */
    @NotBlank(message = "报告类型不能为空")
    private String reportType;
    
    /**
     * 报告标题（可选，如果不提供则自动生成）
     */
    private String reportTitle;
    
    /**
     * 是否立即发布（默认true）
     */
    private Boolean publish;
}
