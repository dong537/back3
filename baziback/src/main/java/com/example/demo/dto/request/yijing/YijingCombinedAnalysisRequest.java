package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingCombinedAnalysisRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    @NotEmpty(message = "bazi_chart 不能为空")
    @JsonProperty("bazi_chart")
    private Map<String, Object> baziChart;

    private Map<String, Object> hexagram;

    @JsonProperty("analysis_aspects")
    private List<String> analysisAspects;
}

