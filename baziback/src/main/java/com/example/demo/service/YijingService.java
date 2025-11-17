package com.example.demo.service;

import com.example.demo.client.McpYijingClient;
import com.example.demo.dto.request.yijing.*;
import com.example.demo.dto.response.McpCallResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YijingService {

    private final McpYijingClient mcpYijingClient;

    public String listTools() {
        return mcpYijingClient.listAvailableTools();
    }

    public McpCallResult generateHexagram(YijingGenerateHexagramRequest request) {
        return mcpYijingClient.generateHexagram(request);
    }

    public McpCallResult interpretHexagram(YijingInterpretRequest request) {
        return mcpYijingClient.interpretHexagram(request);
    }

    public McpCallResult advise(YijingAdviseRequest request) {
        return mcpYijingClient.advise(request);
    }

    public McpCallResult generateBaziChart(YijingBaziGenerateChartRequest request) {
        return mcpYijingClient.generateBaziChart(request);
    }

    public McpCallResult analyzeBazi(YijingBaziAnalyzeRequest request) {
        return mcpYijingClient.analyzeBazi(request);
    }

    public McpCallResult forecastBazi(YijingBaziForecastRequest request) {
        return mcpYijingClient.forecastBazi(request);
    }

    public McpCallResult combinedAnalysis(YijingCombinedAnalysisRequest request) {
        return mcpYijingClient.combinedAnalysis(request);
    }

    public McpCallResult destinyConsult(YijingDestinyConsultRequest request) {
        return mcpYijingClient.destinyConsult(request);
    }

    public McpCallResult knowledgeLearn(YijingKnowledgeLearnRequest request) {
        return mcpYijingClient.knowledgeLearn(request);
    }

    public McpCallResult caseStudy(YijingCaseStudyRequest request) {
        return mcpYijingClient.caseStudy(request);
    }
}