package com.example.demo.dto.request.tarot;

import com.example.demo.dto.response.tarot.PerformReadingResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TarotDeepSeekInterpretRequest {

    @NotBlank
    private String question;

    @NotBlank
    private String spreadType;

    @NotEmpty
    private List<PerformReadingResponse.CardReading> cards;

    /** 可选：前端希望覆盖系统默认的输出要求（比如更短/更长/更偏实操） */
    private String outputPreference;

    /** 可选：是否把后端生成的 prompt 一并返回，便于调试/展示 */
    private Boolean returnPrompt;
}
