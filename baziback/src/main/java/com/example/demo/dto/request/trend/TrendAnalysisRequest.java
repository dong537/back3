package com.example.demo.dto.request.trend;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 趋势分析请求DTO
 */
@Data
@Schema(description = "趋势分析请求参数")
public class TrendAnalysisRequest {
    
    @NotBlank(message = "八字不能为空")
    @Schema(description = "八字（如：甲子 乙丑 丙寅 丁卯）", example = "庚午 辛巳 甲寅 己巳")
    private String bazi;
    
    @NotBlank(message = "性别不能为空")
    @Schema(description = "性别（male/female）", example = "male")
    private String gender;
    
    @NotNull(message = "出生日期不能为空")
    @Schema(description = "出生日期（格式：yyyy-MM-dd）", example = "1990-05-15")
    private LocalDate birthDate;
    
    @Schema(description = "起始年龄（可选，默认0）", example = "0")
    private Integer startAge;
    
    @Schema(description = "结束年龄（可选，默认80）", example = "80")
    private Integer endAge;
}
