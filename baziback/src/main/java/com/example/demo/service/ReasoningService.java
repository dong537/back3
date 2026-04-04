package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import java.time.LocalDateTime;
import java.util.*;

/**
 * 推理服务 - 统一走 OneAPI（Gemini）通道
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReasoningService {

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
     * 非流式推理 - 一次性返回完整结果
     */
    public String getReasoningResult(String userMessage) throws Exception {
        if (userMessage == null || userMessage.trim().isEmpty() || "0".equals(userMessage)) {
            log.warn("收到无效的推理消息，使用默认提示: {}", userMessage);
            userMessage = "请帮我分析一下";
        }

        if (!StringUtils.hasText(apiKey)) {
            log.warn("API 密钥未配置，使用演示模式");
            return getDemoResponse(userMessage);
        }

        URI requestUri = buildRequestUri();

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4000);

        String sysPrompt = StringUtils.hasText(systemPrompt)
                ? systemPrompt
                : "你是一位精通命理学、易经、八字、塔罗的专家。请详细展示你的思考过程。";

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", sysPrompt),
                Map.of("role", "user", "content", userMessage)
        );
        requestBody.put("messages", messages);

        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(100))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        log.info("发送 OneAPI 推理请求: model={}, uri={}", model, requestUri);

        LocalDateTime callStart = LocalDateTime.now();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("OneAPI 推理 API 返回错误: status={}, body={}", response.statusCode(), abbreviate(response.body()));
            throw new BusinessException("AI 推理服务调用失败: HTTP " + response.statusCode(), HttpStatus.BAD_GATEWAY);
        }

        tokenTracker.trackFromResponse(response.body(), model, "oneapi-reasoning", callStart);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");

        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> choice = choices.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            if (message != null) {
                String content = (String) message.get("content");
                if (content != null) {
                    log.info("推理完成: model={}, 结果长度={} 字符", model, content.length());
                    return content;
                }
            }
        }

        return "推理失败，请稍后重试";
    }

    /**
     * 流式推理响应
     */
    public Flux<String> streamReasoningResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty() || "0".equals(userMessage)) {
            log.warn("收到无效的推理消息，使用默认提示: {}", userMessage);
            userMessage = "请帮我分析一下";
        }

        final String finalMessage = userMessage;

        return Flux.<String>create(sink -> {
            try {
                if (!StringUtils.hasText(apiKey)) {
                    log.warn("API 密钥未配置，使用演示模式");
                    streamDemoResponse(sink, finalMessage);
                    return;
                }

                URI requestUri = buildRequestUri();

                Map<String, Object> requestBody = new LinkedHashMap<>();
                requestBody.put("model", model);
                requestBody.put("stream", true);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 4000);

                String sysPrompt = StringUtils.hasText(systemPrompt)
                        ? systemPrompt
                        : "你是一位精通命理学、易经、八字、塔罗的专家。请详细展示你的思考过程。";

                List<Map<String, String>> messages = List.of(
                        Map.of("role", "system", "content", sysPrompt),
                        Map.of("role", "user", "content", finalMessage)
                );
                requestBody.put("messages", messages);

                String jsonRequest = objectMapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(requestUri)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(Duration.ofSeconds(100))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                        .build();

                log.info("发送 OneAPI 流式推理请求: model={}, uri={}", model, requestUri);

                HttpResponse<java.io.InputStream> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                        String line;
                        boolean sentStop = false;
                        int dataCount = 0;

                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();

                                if (data.equals("[DONE]")) {
                                    log.info("收到 [DONE] 标记");
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
                                        String finishReason = (String) choice.get("finish_reason");

                                        if (delta != null) {
                                            String content = (String) delta.get("content");
                                            String reasoningContent = (String) delta.get("reasoning_content");

                                            Map<String, Object> responseData = new HashMap<>();

                                            if (reasoningContent != null && !reasoningContent.isEmpty()) {
                                                responseData.put("reasoning_content", reasoningContent);
                                            }

                                            if (content != null && !content.isEmpty()) {
                                                responseData.put("content", content);
                                            }

                                            if (!responseData.isEmpty()) {
                                                sink.next(toSseJson(responseData));
                                                dataCount++;
                                            }
                                        }

                                        if ("stop".equals(finishReason) && !sentStop) {
                                            log.info("收到 stop 标记");
                                            sink.next(toSseJson(Map.of("finish_reason", "stop")));
                                            sentStop = true;
                                            sink.next("data: [DONE]\n\n");
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    log.debug("解析流式响应失败: {}", e.getMessage());
                                }
                            }
                        }

                        log.info("流式推理完成: model={}, 发送 {} 条数据", model, dataCount);
                    }
                } else {
                    log.error("OneAPI 流式推理返回错误: status={}", response.statusCode());
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("content", "抱歉，推理服务暂时不可用。错误码: " + response.statusCode());
                    errorData.put("finish_reason", "error");
                    sink.next(toSseJson(errorData));
                    sink.next("data: [DONE]\n\n");
                }

                sink.complete();

            } catch (Exception e) {
                log.error("流式推理请求失败", e);
                try {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("content", "推理服务异常: " + e.getMessage());
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

    private String abbreviate(String value) {
        if (!StringUtils.hasText(value)) return "";
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
    }

    private String getDemoResponse(String userMessage) {
        return "根据您的问题，以下是我的分析结果：\n\n" +
                "1 核心观点\n" +
                "这是一个很有意思的问题。通过深入分析，我发现了几个关键要点。\n\n" +
                "2 详细分析\n" +
                "首先，从理论角度来看，这涉及到多个方面的考虑。其次，从实践角度来看，我们需要考虑具体的应用场景。\n\n" +
                "3 建议\n" +
                "基于以上分析，我建议您采取以下措施：\n" +
                "第一步：了解基本原理\n" +
                "第二步：结合实际情况\n" +
                "第三步：持续优化调整\n\n" +
                "4 总结\n" +
                "总的来说，这个问题的解决需要综合考虑多个因素。希望以上分析对您有所帮助。";
    }

    private void streamDemoResponse(reactor.core.publisher.FluxSink<String> sink, String userMessage) throws Exception {
        log.info("使用演示模式响应用户问题");

        Map<String, Object> thinkingData = new HashMap<>();
        thinkingData.put("reasoning_content", "让我分析一下这个问题...\n\n" +
                "首先，我需要理解用户的具体需求。\n" +
                "用户要求：" + userMessage + "\n\n" +
                "我将从多个角度进行分析...");
        sink.next(toSseJson(thinkingData));

        Map<String, Object> contentData = new HashMap<>();
        contentData.put("content", getDemoResponse(userMessage));
        sink.next(toSseJson(contentData));

        sink.next(toSseJson(Map.of("finish_reason", "stop")));
        sink.next("data: [DONE]\n\n");
        sink.complete();
    }
}
