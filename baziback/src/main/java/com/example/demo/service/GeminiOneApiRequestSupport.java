package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class GeminiOneApiRequestSupport {

    private final String apiKey;
    private final String apiBaseUrl;
    private final String textModel;
    private final String visionModel;
    private final double temperature;
    private final int maxTokens;

    GeminiOneApiRequestSupport(String apiKey,
                               String apiBaseUrl,
                               String textModel,
                               String visionModel,
                               double temperature,
                               int maxTokens) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
        this.textModel = textModel;
        this.visionModel = visionModel;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    void validateOneApiConfiguration() {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("Gemini API key is missing; configure ONE_API_KEY or GEMINI_API_KEY");
        }
        if (!apiKey.trim().startsWith("sk-")) {
            throw new BusinessException("OneAPI key must start with sk-");
        }
        if (!StringUtils.hasText(visionModel)) {
            throw new BusinessException("Vision model is missing; configure ONE_API_GEMINI_VISION_MODEL or GEMINI_VISION_MODEL");
        }
        if (!StringUtils.hasText(apiBaseUrl)) {
            throw new BusinessException("OneAPI base URL is missing; configure ONE_API_BASE_URL");
        }
    }

    URI buildRequestUri() {
        return URI.create(normalizeBaseUrl(apiBaseUrl) + "/chat/completions");
    }

    String[] buildAuthorizationHeaders() {
        return new String[]{"Authorization", "Bearer " + apiKey.trim()};
    }

    Map<String, Object> buildTextProbeRequestBody(String prompt) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", textModel.trim());
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", Math.min(maxTokens, 200));
        return requestBody;
    }

    Map<String, Object> buildVisionRequestBody(String imageBase64,
                                               String mimeType,
                                               String prompt,
                                               int tokenLimit,
                                               String modelName,
                                               String payloadFormat) {
        String dataUrl = "data:" + mimeType + ";base64," + imageBase64;

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);

        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("type", "image_url");
        if ("openai-image-url-string".equalsIgnoreCase(payloadFormat)) {
            imagePart.put("image_url", dataUrl);
        } else {
            imagePart.put("image_url", Map.of("url", dataUrl));
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textPart, imagePart));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName.trim());
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", tokenLimit);
        return requestBody;
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            normalized = normalized.substring(0, normalized.length() - "/chat/completions".length());
        }
        if (!normalized.endsWith("/v1")) {
            normalized = normalized + "/v1";
        }
        return normalized;
    }
}
