package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private String question;

    @JsonProperty("analysis_aspects")
    private List<String> analysisAspects;

    /**
     * 可選的附加上下文（包含 hexagram、chart 等資訊）
     */
    private Map<String, Object> context;
}

