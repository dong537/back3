package com.example.demo.dto.response.gemini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiPhysiognomyReportResponse {
    private String forehead;
    private String eyesAndBrows;
    private String nose;
    private String mouthAndChin;
    private String overallImpression;
}
