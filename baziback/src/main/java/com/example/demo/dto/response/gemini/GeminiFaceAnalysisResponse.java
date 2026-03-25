package com.example.demo.dto.response.gemini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiFaceAnalysisResponse {
    private Boolean hasFace;
    private Integer faceCount;
    private String faceStatusText;
    private String visualSummary;
    @Builder.Default
    private List<GeminiObservedFeatureResponse> observedFeatures = new ArrayList<>();
    private GeminiPhysiognomyReportResponse physiognomyReport;
    @Builder.Default
    private List<GeminiNarrativeSectionResponse> detailSections = new ArrayList<>();
    private String imageQuality;
    private String reportSummary;
    @Builder.Default
    private List<String> suggestions = new ArrayList<>();
    private String disclaimer;
    private String provider;
    private String model;
    private String rawText;
}
