package com.example.demo.service;

import com.example.demo.dto.request.gemini.GeminiFaceAnalysisRequest;
import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiEndpoint;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.max-image-bytes:5242880}")
    private long maxImageBytes;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Map<String, Object> analyzeFace(GeminiFaceAnalysisRequest request) throws Exception {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("Gemini API Key 未配置，请设置 GEMINI_API_KEY");
        }

        String mimeType = normalizeMimeType(request.getMimeType());
        if (!SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            throw new BusinessException("仅支持 JPG、PNG、WEBP 图片");
        }

        String imageBase64 = sanitizeBase64(request.getImageBase64());
        if (!StringUtils.hasText(imageBase64)) {
            throw new BusinessException("图片数据无效");
        }

        long imageBytes = estimateDecodedBytes(imageBase64);
        if (imageBytes <= 0) {
            throw new BusinessException("图片数据无效");
        }
        if (imageBytes > maxImageBytes) {
            throw new BusinessException("图片不能超过 5MB");
        }

        Map<String, Object> requestBody = buildRequestBody(imageBase64, mimeType, request.getPrompt());
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(buildRequestUri())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(100))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        log.info("调用 Gemini 人脸分析 | model={}, mimeType={}, imageBytes={}", model, mimeType, imageBytes);
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Gemini API 调用失败 | status={}, body={}", response.statusCode(), response.body());
            throw new BusinessException("Gemini 服务调用失败: HTTP " + response.statusCode());
        }

        return parseResponse(response.body());
    }

    private URI buildRequestUri() {
        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String url = String.format("%s/%s:generateContent?key=%s", apiEndpoint, model, encodedKey);
        return URI.create(url);
    }

    private Map<String, Object> buildRequestBody(String imageBase64, String mimeType, String prompt) {
        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.<String, Object>of("text", buildPrompt(prompt)));
        parts.add(Map.<String, Object>of("inline_data", Map.of(
                "mime_type", mimeType,
                "data", imageBase64
        )));

        Map<String, Object> content = Map.<String, Object>of(
                "role", "user",
                "parts", parts
        );

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("responseMimeType", "application/json");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("contents", List.of(content));
        requestBody.put("generationConfig", generationConfig);
        return requestBody;
    }

    private String buildPrompt(String userPrompt) {
        String safePrompt = StringUtils.hasText(userPrompt)
                ? userPrompt.trim()
                : "请结合传统面相学的文化表述方式，先描述可见五官特征，再给出娱乐性的文化解读和一份报告。";

        return """
                你是一个“传统面相学文化说明”助手。
                你只能根据图片中可见的五官与脸部轮廓，输出文化娱乐性的说明报告。
                你不能进行身份识别，严禁猜测具体人物姓名、背景或与任何数据库做比对。
                你不能断言此人的真实性格、命运、财富、婚恋结果、智力、信用、健康、精神状态或其他敏感属性。
                你可以介绍“在传统面相学语境中，这类外观常被怎样解读”，但必须明确这只是传统文化视角，不是事实判断。
                如果图中没有清晰可见的人脸，请明确说明。

                请严格输出 JSON，对象结构如下：
                {
                  "hasFace": true,
                  "faceCount": 1,
                  "visualSummary": "先总结图片中可见的人脸与五官特征",
                  "observedFeatures": [
                    {
                      "region": "额头",
                      "observation": "看到的客观外观特征",
                      "clarity": "清晰/一般/不清晰"
                    }
                  ],
                  "physiognomyReport": {
                    "forehead": "介绍传统相学里对额头区域的常见文化说法，使用“常被视为/常被联想到”措辞",
                    "eyesAndBrows": "介绍眉眼区域的传统文化说法",
                    "nose": "介绍鼻部区域的传统文化说法",
                    "mouthAndChin": "介绍口唇与下巴区域的传统文化说法",
                    "overallImpression": "给出整体的文化风格总结，但不要断言事实或命运"
                  },
                  "imageQuality": "对清晰度、光线、角度的简短判断",
                  "reportSummary": "输出一段完整总结，语气克制，强调仅供文化娱乐参考",
                  "suggestions": ["拍摄建议1", "拍摄建议2"],
                  "disclaimer": "本报告为基于可见外观生成的传统文化娱乐性说明，不构成对性格、命运、能力、健康或身份的事实判断。"
                }

                用户补充要求：
                """ + safePrompt;
    }

    private Map<String, Object> parseResponse(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new BusinessException("Gemini 返回内容为空");
        }

        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        if (content == null) {
            throw new BusinessException("Gemini 返回内容格式错误");
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new BusinessException("Gemini 返回内容格式错误");
        }

        Object textObj = parts.get(0).get("text");
        String text = textObj == null ? "" : String.valueOf(textObj).trim();
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("Gemini 返回内容为空");
        }

        Map<String, Object> parsedResult = tryParseJson(text);
        parsedResult.put("provider", "gemini");
        parsedResult.put("model", model);
        parsedResult.put("rawText", text);
        return parsedResult;
    }

    private Map<String, Object> tryParseJson(String text) {
        try {
            return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            log.warn("Gemini 返回内容不是 JSON，使用回退文本模式");
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("hasFace", null);
            fallback.put("faceCount", null);
            fallback.put("visualSummary", text);
            fallback.put("observedFeatures", List.of());
            fallback.put("physiognomyReport", Map.of(
                    "forehead", "",
                    "eyesAndBrows", "",
                    "nose", "",
                    "mouthAndChin", "",
                    "overallImpression", ""
            ));
            fallback.put("imageQuality", "");
            fallback.put("reportSummary", text);
            fallback.put("suggestions", List.of());
            fallback.put("disclaimer", "结果为文本回退模式，仅供文化娱乐参考，不包含身份识别，也不构成事实判断。");
            return fallback;
        }
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null ? "" : mimeType.trim().toLowerCase();
    }

    private String sanitizeBase64(String rawBase64) {
        if (!StringUtils.hasText(rawBase64)) {
            return "";
        }
        String value = rawBase64.trim();
        int commaIndex = value.indexOf(',');
        if (value.startsWith("data:") && commaIndex >= 0) {
            value = value.substring(commaIndex + 1);
        }
        return value.replaceAll("\\s+", "");
    }

    private long estimateDecodedBytes(String base64) {
        int length = base64.length();
        if (length == 0) {
            return 0;
        }
        int padding = 0;
        if (base64.endsWith("==")) {
            padding = 2;
        } else if (base64.endsWith("=")) {
            padding = 1;
        }
        return (length * 3L) / 4L - padding;
    }
}
