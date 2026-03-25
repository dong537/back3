package com.example.demo.dto.response.gemini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiObservedFeatureResponse {
    private String region;
    private String observation;
    private String clarity;
    private String detailLevel;
}
