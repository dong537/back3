package com.example.demo.dto.request.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 翻译请求DTO
 */
@Data
@Schema(description = "翻译请求参数")
public class TranslateRequest {
    
    @NotBlank(message = "内容不能为空")
    @Schema(description = "原始内容", example = "八字分析结果...")
    private String content;
    
    @NotBlank(message = "目标语言不能为空")
    @Schema(description = "目标语言代码（如：en, ja, ko）", example = "en")
    private String targetLanguage;
}
