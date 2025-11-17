package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class YijingBaziAnalyzeRequest {

    /**
     * 由 bazi_generate_chart 返回的完整命盤
     */
    @NotEmpty(message = "chart 不能为空")
    private Map<String, Object> chart;

    @JsonProperty("analysis_type")
    private List<String> analysisType;

    @JsonProperty("detail_level")
    private String detailLevel;
}

