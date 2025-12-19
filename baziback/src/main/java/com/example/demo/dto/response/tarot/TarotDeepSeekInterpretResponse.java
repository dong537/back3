package com.example.demo.dto.response.tarot;

import lombok.Data;

@Data
public class TarotDeepSeekInterpretResponse {

    private String aiInterpretation;

    /** 可选返回：后端生成的 DeepSeek 提示词 */
    private String prompt;
}
