package com.example.demo.dto.response.bazi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaziInterpretationResponse {
    private String id;
    private String position;
    private String shiShen;
    private String title;
    private String basicDef;
    private String mainContent;
    private String supportContent;
    private String restrictContent;
    private String genderDiff;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private Integer helpCount;
    private Integer unhelpCount;
    private Integer commentCount;
    private Map<String, Integer> scores;
    private Map<String, String> advices;
    private String suggestions;
    private String avoidances;
}
