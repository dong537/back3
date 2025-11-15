package com.example.demo.dto.response.tarot;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetDatabaseAnalyticsResponse {
    private Map<String, Integer> cardCountByCategory;
    private Map<String, Double> elementDistribution;
    private Map<String, String> qualityMetrics;
    private List<String> recommendations;
}
