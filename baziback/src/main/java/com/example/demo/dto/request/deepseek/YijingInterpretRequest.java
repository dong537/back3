package com.example.demo.dto.request.deepseek;


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
public class YijingInterpretRequest {

    @NotBlank(message = "timestamp 不能为空")
    private String timestamp;

    @NotBlank(message = "method 不能为空")
    private String method;

    @NotBlank(message = "question 不能为空")
    private String question;

    @NotNull(message = "original 不能为空")
    private HexagramData original;

    @JsonProperty("changing_lines")
    private List<Integer> changingLines;

    private HexagramData changed;

    @JsonProperty("interpretation_hint")
    private String interpretationHint;

    @NotBlank(message = "focus 不能为空")
    private String focus;

    @NotBlank(message = "context 不能为空")
    private String context;

    /**
     * brief / standard / detailed
     */
    private String detailLevel;

    /**
     * focus 为 specific_line 时传入 1-6
     */
    private Integer lineNumber;

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
        private String type; // yang / yin
        private Boolean changing;
        private Integer value;
        private String text; // 变卦的爻辞
        private String meaning; // 爻辞解释
    }
}
