package com.example.demo.dto.response.gemini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiFailureDetails {
    private List<String> attemptedModels;
    private String lastModel;
    private Integer lastStatus;
    private String lastPayloadFormat;
    private String uri;
}
