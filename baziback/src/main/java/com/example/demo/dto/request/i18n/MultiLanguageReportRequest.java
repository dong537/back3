package com.example.demo.dto.request.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 多语言报告生成请求DTO
 */
@Data
@Schema(description = "多语言报告生成请求参数")
public class MultiLanguageReportRequest {
    
    @NotBlank(message = "八字数据不能为空")
    @Schema(description = "八字数据", example = "庚午 辛巳 甲寅 己巳")
    private String baziData;
    
    @NotBlank(message = "报告类型不能为空")
    @Schema(description = "报告类型（comprehensive/career/love/health/wealth）", example = "comprehensive")
    private String reportType;
    
    @NotBlank(message = "语言代码不能为空")
    @Schema(description = "语言代码（如：zh-CN, en, ja）", example = "en")
    private String language;
}
