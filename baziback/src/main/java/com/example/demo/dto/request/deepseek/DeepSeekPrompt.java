package com.example.demo.dto.request.deepseek;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeepSeekPrompt {
    @NotBlank(message = "提示词内容不能为空")
    private String promptContent;
}
