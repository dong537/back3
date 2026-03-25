package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeepSeekService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.endpoint:https://api.deepseek.com/v1/chat/completions}")
    private String apiEndpoint;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.temperature:0.5}")
    private double temperature;

    @Value("${deepseek.system-prompt:}")
    private String systemPrompt;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public String generateBaziReport(String userData) throws Exception {
        if (StringUtils.hasText(systemPrompt)) {
            log.info("DeepSeek system prompt loaded, length={}", systemPrompt.length());
        } else {
            log.warn("DeepSeek system prompt is empty, falling back to the default prompt");
        }

        log.info("Calling DeepSeek to generate bazi report");
        return buildReportWithSystemPrompt(userData);
    }

    public String interpretHexagram(String request) throws Exception {
        return callDeepSeekWithoutSystemPrompt(request);
    }

    public String interpretZiweiChart(String request) throws Exception {
        return callDeepSeekWithoutSystemPrompt(request);
    }

    private String buildReportWithSystemPrompt(String userContent) throws Exception {
        ensureApiKey();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        String prompt = StringUtils.hasText(systemPrompt)
                ? systemPrompt
                : "你是一位精通《周易》、六爻预测与命理学的专家。";
        messages.add(Map.of("role", "system", "content", prompt));
        messages.add(Map.of("role", "user", "content", userContent));

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        requestBody.put("temperature", temperature);
        requestBody.put("stream", false);

        return executeDeepSeekRequest(requestBody);
    }

    private String callDeepSeekWithoutSystemPrompt(String userContent) throws Exception {
        ensureApiKey();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userContent));

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        requestBody.put("temperature", temperature);
        requestBody.put("stream", false);

        return executeDeepSeekRequest(requestBody);
    }

    @SuppressWarnings("unchecked")
    private String executeDeepSeekRequest(Map<String, Object> requestBody) throws Exception {
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(100))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        log.info("Sending DeepSeek request: model={}, contentLength={}", model, requestBodyJson.length());

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException ex) {
            log.error("Failed to connect to DeepSeek endpoint: {}", apiEndpoint, ex);
            throw new BusinessException("DeepSeek 服务暂时不可用，请检查接口地址或网络连通性。", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (HttpTimeoutException ex) {
            log.error("DeepSeek request timed out: {}", apiEndpoint, ex);
            throw new BusinessException("DeepSeek 响应超时，请稍后重试。", HttpStatus.GATEWAY_TIMEOUT);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("DeepSeek request interrupted", ex);
            throw new BusinessException("DeepSeek 请求被中断，请稍后重试。", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (IOException ex) {
            log.error("DeepSeek IO failure: {}", apiEndpoint, ex);
            throw new BusinessException("DeepSeek 服务调用失败，请检查网络或代理配置。", HttpStatus.BAD_GATEWAY);
        }

        if (response.statusCode() != 200) {
            log.error("DeepSeek API returned non-200: status={}, body={}", response.statusCode(), abbreviate(response.body()));
            throw new BusinessException("DeepSeek API 调用失败: HTTP " + response.statusCode(), HttpStatus.BAD_GATEWAY);
        }

        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("DeepSeek 返回数据格式错误", HttpStatus.BAD_GATEWAY);
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
        String content = messageObj == null ? null : (String) messageObj.get("content");
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("DeepSeek 未返回有效内容", HttpStatus.BAD_GATEWAY);
        }

        log.info("DeepSeek response length={}", content.length());
        return content;
    }

    private void ensureApiKey() {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("DeepSeek API Key 未配置，请检查 DEEPSEEK_API_KEY 或 application.yml。", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String abbreviate(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
    }
}
