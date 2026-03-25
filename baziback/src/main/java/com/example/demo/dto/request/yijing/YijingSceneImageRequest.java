package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
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
public class YijingSceneImageRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    @NotBlank(message = "method 不能为空")
    private String method;

    @NotBlank(message = "interpretation 不能为空")
    private String interpretation;

    @JsonProperty("interpretation_hint")
    private String interpretationHint;

    @JsonProperty("changing_lines")
    private List<Integer> changingLines;

    @Valid
    @NotNull(message = "original 不能为空")
    private HexagramSnapshot original;

    @Valid
    private HexagramSnapshot changed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HexagramSnapshot {
        private Integer id;
        private String name;
        private String chinese;
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
}
