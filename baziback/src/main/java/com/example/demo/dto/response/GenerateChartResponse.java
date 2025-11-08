package com.example.demo.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class GenerateChartResponse {
    private String rawResponse;
    private String chartText;
    private Map<String, Object> chartData;
    private String chartId;
    private Boolean success;
}