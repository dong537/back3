package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingCaseStudyRequest {

    @JsonProperty("case_id")
    private String caseId;

    /**
     * yijing / bazi / combined
     */
    private String system;

    private String category;

    @JsonProperty("analysis_focus")
    private List<String> analysisFocus;
}