package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class YijingAdviseRequest {

    @NotNull(message = "hexagram 不能为空")
    private HexagramData hexagram;

    @NotBlank(message = "question 不能为空")
    private String question;

    @JsonProperty("time_frame")
    private String timeFrame; // immediate / short_term / long_term

    private String context;

    @JsonProperty("decision_type")
    private String decisionType; // career / relationship / investment / health / general

    @JsonProperty("risk_tolerance")
    private String riskTolerance; // conservative / moderate / aggressive

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HexagramData {
        private Integer id;
        private String binary;
        private List<LineData> lines;
        private String name;
        private String chinese;
        private String upper;
        private String lower;
        private String symbol;
        private String judgment;
        private String image;
        private String meaning;
        private List<String> keywords;
        private String element;
        private String season;
        private String direction;
        private Map<String, String> applications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LineData {
        private Integer position;
        private String type;
        private Boolean changing;
        private Integer value;
        private String text;
        private String meaning;
    }
}
