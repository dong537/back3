package com.example.demo.dto.request.gemini;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GeminiFaceAnalysisRequest {

    @NotBlank(message = "图片数据不能为空")
    private String imageBase64;

    @NotBlank(message = "图片类型不能为空")
    private String mimeType;

    private String prompt;
}
