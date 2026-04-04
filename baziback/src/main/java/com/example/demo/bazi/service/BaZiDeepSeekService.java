package com.example.demo.bazi.service;

import com.example.demo.service.TokenTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 八字 AI 解读服务 - 统一走 OneAPI（Gemini）通道
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BaZiDeepSeekService {

    @Value("${gemini.api.key:${deepseek.api.key:}}")
    private String apiKey;

    @Value("${gemini.api.base-url:${deepseek.api.endpoint:https://gemini.agentpit.io/v1}}")
    private String apiBaseUrl;

    @Value("${deepseek.model:${gemini.text-model:gemini-2.0-flash}}")
    private String model;

    @Value("${deepseek.system-prompt:}")
    private String systemPrompt;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final TokenTracker tokenTracker;

    /**
     * 生成八字命理报告
     */
    public String generateBaziReport(String baziData) throws Exception {
        if (StringUtils.hasText(systemPrompt)) {
            log.info("八字解读：使用系统提示词");
        } else {
            log.warn("八字解读：系统提示词为空，使用默认提示词");
        }

        log.info("开始调用 OneAPI 生成八字报告, model={}", model);
        return callWithSystemPrompt(baziData);
    }

    /**
     * 根据八字分析结果生成解读报告
     */
    public String interpretAnalysis(Map<String, Object> analysisResult) throws Exception {
        String jsonData = objectMapper.writeValueAsString(analysisResult);
        return generateBaziReport(jsonData);
    }

    /**
     * 生成八字综合解读（包含特定方面）
     */
    public String generateAspectReport(String baziData, List<String> aspects) throws Exception {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下八字数据，重点解读以下方面：\n");
        for (String aspect : aspects) {
            prompt.append("- ").append(aspect).append("\n");
        }
        prompt.append("\n八字数据：\n").append(baziData);

        return callWithSystemPrompt(prompt.toString());
    }

    /**
     * 无系统提示词直接调用
     */
    public String callDeepSeekDirect(String userContent) throws Exception {
        return callWithoutSystemPrompt(userContent);
    }

    private String callWithSystemPrompt(String userContent) throws Exception {
        ensureApiKey();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        String sysPrompt = StringUtils.hasText(systemPrompt)
                ? systemPrompt
                : getDefaultBaziPrompt();
        messages.add(Map.of("role", "system", "content", sysPrompt));
        messages.add(Map.of("role", "user", "content", userContent));

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", false);

        return executeRequest(requestBody);
    }

    private String callWithoutSystemPrompt(String userContent) throws Exception {
        ensureApiKey();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userContent));

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", false);

        return executeRequest(requestBody);
    }

    @SuppressWarnings("unchecked")
    private String executeRequest(Map<String, Object> requestBody) throws Exception {
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        URI requestUri = buildRequestUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(100))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        log.info("发送 OneAPI 八字解读请求: model={}, uri={}, 内容长度={}", model, requestUri, requestBodyJson.length());

        LocalDateTime callStart = LocalDateTime.now();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("OneAPI 八字解读 API 调用失败: 状态码={}, 响应={}", response.statusCode(), response.body());
            throw new Exception("AI API 调用失败: HTTP " + response.statusCode());
        }

        tokenTracker.trackFromResponse(response.body(), model, "oneapi-bazi", callStart);

        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new Exception("AI 返回数据格式错误");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
        String content = (String) messageObj.get("content");

        log.info("OneAPI 八字解读返回: model={}, 内容长度={}", model, content.length());
        return content;
    }

    private URI buildRequestUri() {
        String base = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        if (base.endsWith("/chat/completions")) {
            return URI.create(base);
        }
        if (!base.endsWith("/v1")) {
            base = base + "/v1";
        }
        return URI.create(base + "/chat/completions");
    }

    private void ensureApiKey() throws Exception {
        if (!StringUtils.hasText(apiKey)) {
            throw new Exception("AI API Key 未配置，请在 application.yml 中设置 ONE_API_KEY 或 GEMINI_API_KEY");
        }
    }

    private String getDefaultBaziPrompt() {
        return """
            你是一位精通中国传统命理学的八字大师，专注于四柱八字命理分析。

            你的专业领域包括：
            1. 天干地支的生克关系
            2. 十神的含义与作用
            3. 五行的平衡与调候
            4. 大运流年的推算
            5. 喜用神与忌神的判断
            6. 刑冲合会的影响
            7. 神煞的吉凶判断

            请根据用户提供的八字数据，给出专业、详细、准确的命理分析。
            分析应包括：性格特点、事业发展、财运状况、婚姻感情、健康状况等方面。
            语言风格要专业但易懂，既有传统命理术语，也有通俗解释。
            """;
    }
}
