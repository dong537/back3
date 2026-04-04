package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * 易经推理服务 - 统一走 OneAPI（Gemini）通道，支持流式思维链输出
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class YijingReasoningService {

    @Value("${gemini.api.key:${deepseek.api.key:}}")
    private String apiKey;

    @Value("${gemini.api.base-url:${deepseek.api.endpoint:https://gemini.agentpit.io/v1}}")
    private String apiBaseUrl;

    @Value("${deepseek.model:${gemini.text-model:gemini-2.0-flash}}")
    private String model;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final TokenTracker tokenTracker;

    /**
     * 流式易经占卜推理响应
     */
    public Flux<String> streamYijingDivination(String question, String method, String hexagram) {
        return Flux.<String>create(sink -> {
            try {
                if (!StringUtils.hasText(apiKey)) {
                    log.warn("API 密钥未配置，无法进行易经推理");
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("content", "AI 服务未配置，请联系管理员。");
                    errorData.put("finish_reason", "error");
                    sink.next(toSseJson(errorData));
                    sink.next("data: [DONE]\n\n");
                    sink.complete();
                    return;
                }

                URI requestUri = buildRequestUri();

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("stream", true);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 4000);

                String systemPrompt = buildYijingSystemPrompt(method);
                String userContent = buildYijingUserPrompt(question, method, hexagram);

                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", systemPrompt));
                messages.add(Map.of("role", "user", "content", userContent));
                requestBody.put("messages", messages);

                String jsonRequest = objectMapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(requestUri)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(Duration.ofSeconds(100))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                        .build();

                log.info("发送 OneAPI 易经流式推理请求: model={}, uri={}", model, requestUri);

                HttpResponse<java.io.InputStream> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                        String line;
                        boolean sentStop = false;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();

                                if (data.equals("[DONE]")) {
                                    if (!sentStop) {
                                        sink.next(toSseJson(Map.of("finish_reason", "stop")));
                                        sentStop = true;
                                    }
                                    sink.next("data: [DONE]\n\n");
                                    break;
                                }

                                try {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");

                                    if (choices != null && !choices.isEmpty()) {
                                        Map<String, Object> choice = choices.get(0);
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");

                                        if (delta != null) {
                                            String content = (String) delta.get("content");
                                            String reasoningContent = (String) delta.get("reasoning_content");
                                            String finishReason = (String) choice.get("finish_reason");

                                            Map<String, Object> responseData = new HashMap<>();

                                            if (reasoningContent != null && !reasoningContent.isEmpty()) {
                                                responseData.put("reasoning_content", reasoningContent);
                                            }

                                            if (content != null && !content.isEmpty()) {
                                                responseData.put("content", content);
                                            }

                                            if (finishReason != null) {
                                                responseData.put("finish_reason", finishReason);
                                            }

                                            if (!responseData.isEmpty()) {
                                                sink.next(toSseJson(responseData));
                                            }

                                            if ("stop".equals(finishReason) && !sentStop) {
                                                sink.next(toSseJson(Map.of("finish_reason", "stop")));
                                                sentStop = true;
                                                sink.next("data: [DONE]\n\n");
                                                break;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.debug("解析流式响应失败: {}", e.getMessage());
                                }
                            }
                        }
                    }
                } else {
                    log.error("OneAPI 易经推理返回错误: status={}", response.statusCode());
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("content", "抱歉，易经解读服务暂时不可用。错误码: " + response.statusCode());
                    errorData.put("finish_reason", "error");
                    sink.next(toSseJson(errorData));
                    sink.next("data: [DONE]\n\n");
                }

                sink.complete();

            } catch (Exception e) {
                log.error("易经流式推理请求失败", e);
                try {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("content", "易经解读服务异常: " + e.getMessage());
                    errorData.put("finish_reason", "error");
                    sink.next(toSseJson(errorData));
                    sink.next("data: [DONE]\n\n");
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
                sink.complete();
            }
        }).subscribeOn(Schedulers.boundedElastic());
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

    private String toSseJson(Map<String, Object> payload) throws Exception {
        return "data: " + objectMapper.writeValueAsString(payload) + "\n\n";
    }

    private String buildYijingSystemPrompt(String method) {
        return String.format("""
            你是一位精通《周易》的易经大师，深谙六十四卦的卦辞、爻辞和象传。

            当前使用的起卦方式是：%s

            请按照以下步骤进行解读：
            1. 分析卦象的基本含义（卦名、卦辞、卦象）
            2. 解读各爻的爻辞和变化
            3. 结合问题背景，给出针对性的分析
            4. 提供具体的建议和行动指引

            请展示你的完整思考过程，让求卦者理解你的推理逻辑。
            最后给出清晰、实用的建议。
            """, getMethodName(method));
    }

    private String buildYijingUserPrompt(String question, String method, String hexagram) {
        return String.format("""
            问题：%s

            起卦方式：%s
            卦象编码：%s

            请为我解读这次易经占卜，展示你的思考过程，并给出具体建议。
            """, question, getMethodName(method), hexagram);
    }

    private String getMethodName(String method) {
        Map<String, String> methodNames = Map.of(
            "random", "随机起卦",
            "time", "时间起卦",
            "number", "数字起卦",
            "plum_blossom", "梅花易数"
        );
        return methodNames.getOrDefault(method, method);
    }
}
