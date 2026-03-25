package com.example.demo.dto.response.gemini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiProbeResponse {
    private String model;
    private String uri;
    private String content;
    private Integer contentLength;
}
