package com.example.demo.client;


import com.example.demo.dto.request.ziwei.*;
import com.example.demo.dto.response.ziwei.McpZiweiResponse;
import com.example.demo.exception.McpApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
@Slf4j
public class McpZiweiClient {

    private static final String MCP_PROTOCOL_VERSION = "2025-03-26";
    private static final int MAX_RETRY = 3;
    private static final long RETRY_INTERVAL_SECONDS = 1;
    private static final String INITIALIZING_MARKER = "INITIALIZING";

    private final WebClient mcpWebClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final AtomicReference<String> mcpSessionId = new AtomicReference<>();
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    public McpZiweiClient(@Qualifier("ziweiWebClient") WebClient mcpWebClient,
                          ObjectMapper objectMapper,
                          @Value("${mcp.ziwei.api.api-key}") String apiKey) {
        this.mcpWebClient = mcpWebClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("❌ 关键错误: mcp.ziwei.api.api-key 未配置！");
        }
        log.info("✅ MCP Ziwei API Key已加载: {}", getApiKeyPreview());
    }

    /**
     * 工具1：生成紫微斗数命盘
     * 返回包含chartId的响应，供后续工具使用
     */
    public McpZiweiResponse generateChart(ZiweiGenerateChartRequest request) {
        Map<String, Object> args = objectMapper.convertValue(request, new TypeReference<>() {});
        McpZiweiResponse response = callTool("generate_chart", args);

        // 提取chartId并保存到响应中
        if (response.isSuccess() && response.getData() instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) response.getData();
            Object chartId = dataMap.get("chartId");
            if (chartId != null) {
                response.setChartId(chartId.toString());
                log.info("✅ 成功生成紫微斗数命盘，chartId: {}", chartId);
            }
        }
        return response;
    }

    /**
     * 工具2：命盘解读
     */
    public McpZiweiResponse interpretChart(ZiweiInterpretChartRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chartId", request.getChartId());
        args.put("aspects", request.getAspects());
        Optional.ofNullable(request.getDetailLevel()).ifPresent(v -> args.put("detailLevel", v));
        return callTool("interpret_chart", args);
    }

    /**
     * 工具3：运势分析
     */
    public McpZiweiResponse analyzeFortune(ZiweiAnalyzeFortuneRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chartId", request.getChartId());
        args.put("period", request.getPeriod());
        args.put("aspects", request.getAspects());
        Optional.ofNullable(request.getStartDate()).ifPresent(v -> args.put("startDate", v));
        Optional.ofNullable(request.getEndDate()).ifPresent(v -> args.put("endDate", v));
        return callTool("analyze_fortune", args);
    }

    /**
     * 工具4：合婚分析
     */
    public McpZiweiResponse analyzeCompatibility(ZiweiAnalyzeCompatibilityRequest request) {
        Map<String, Object> args = objectMapper.convertValue(request, new TypeReference<>() {});
        return callTool("analyze_compatibility", args);
    }

    /**
     * 工具5：择日功能
     */
    public McpZiweiResponse selectAuspiciousDate(ZiweiSelectAuspiciousDateRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chartId", request.getChartId());
        args.put("eventType", request.getEventType());
        args.put("dateRange", request.getDateRange());
        Optional.ofNullable(request.getPreferences()).ifPresent(v -> args.put("preferences", v));
        return callTool("select_auspicious_date", args);
    }

    /**
     * 工具6：生成可视化图表
     */
    public McpZiweiResponse generateVisualization(ZiweiGenerateVisualizationRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chartId", request.getChartId());
        args.put("visualizationType", request.getVisualizationType());
        Optional.ofNullable(request.getIncludeElements()).ifPresent(v -> args.put("includeElements", v));
        Optional.ofNullable(request.getColorscheme()).ifPresent(v -> args.put("colorscheme", v));
        Optional.ofNullable(request.getOutputFormat()).ifPresent(v -> args.put("outputFormat", v));
        return callTool("generate_visualization", args);
    }

    public String listAvailableTools() {
        initializeSessionIfNeeded();
        String requestBody = buildListToolsRequestBody();
        log.debug("查询紫微斗数工具列表请求: {}", requestBody);

        try {
            String sseResponse = mcpWebClient.post()
                    .headers(headers -> setCommonRequestHeaders(headers, mcpSessionId.get()))
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), res ->
                            res.bodyToMono(String.class)
                                    .flatMap(err -> handleErrorResponse(res.statusCode().value(), err, "查询工具列表"))
                    )
                    .bodyToFlux(String.class)
                    .take(1)
                    .single()
                    .retryWhen(buildRetrySpec("查询工具列表"))
                    .block();

            return parseToolsListResponse(sseResponse);
        } catch (Exception e) {
            log.error("查询可用工具列表异常", e);
            throw new McpApiException("获取可用工具列表失败: " + e.getMessage(), e);
        }
    }

    // ========== 私有工具方法 ==========

    private McpZiweiResponse callTool(String toolName, Map<String, Object> arguments) {
        initializeSessionIfNeeded();
        String body = buildToolCallRequest(toolName, arguments);
        log.debug("调用紫微斗数工具[{}] 请求体: {}", toolName, body);

        String sseResponse = mcpWebClient.post()
                .headers(headers -> setCommonRequestHeaders(headers, mcpSessionId.get()))
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), res ->
                        res.bodyToMono(String.class)
                                .flatMap(err -> handleErrorResponse(res.statusCode().value(), err, "调用工具[" + toolName + "]"))
                )
                .bodyToFlux(String.class)
                .take(1)
                .single()
                .retryWhen(buildRetrySpec("调用工具 " + toolName))
                .block();

        return parseToolResponse(toolName, sseResponse);
    }

    private String buildListToolsRequestBody() {
        int requestId = requestIdCounter.incrementAndGet();
        return String.format(
                "{\"jsonrpc\":\"2.0\",\"id\":%d,\"method\":\"tools/list\",\"params\":{\"_meta\":{\"progressToken\":0}}}",
                requestId
        );
    }

    private String buildToolCallRequest(String toolName, Map<String, Object> arguments) {
        int id = requestIdCounter.incrementAndGet();
        String argsJson;
        try {
            argsJson = objectMapper.writeValueAsString(arguments == null ? Collections.emptyMap() : arguments);
        } catch (JsonProcessingException e) {
            throw new McpApiException("序列化工具[" + toolName + "]参数失败", e);
        }
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id +
                ",\"method\":\"tools/call\",\"params\":{\"name\":\"" + toolName +
                "\",\"arguments\":" + argsJson + ",\"_meta\":{\"progressToken\":0}}}";
    }

    @SuppressWarnings("unchecked")
    private McpZiweiResponse parseToolResponse(String toolName, String sse) {
        try {
            if (!StringUtils.hasText(sse)) {
                throw new McpApiException("MCP响应为空");
            }

            String json = sse.startsWith("data:") ? sse.substring(5).trim() : sse.trim();
            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<>() {});

            if (root.containsKey("error")) {
                throw new McpApiException("MCP返回错误: " + root.get("error"));
            }

            Map<String, Object> result = (Map<String, Object>) root.get("result");
            if (result == null) {
                throw new McpApiException("响应缺少result字段");
            }

            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            if (content == null || content.isEmpty()) {
                throw new McpApiException("响应缺少content");
            }

            String responseText = (String) content.get(0).get("text");

            // 尝试将响应文本解析为JSON对象
            Object data;
            try {
                data = objectMapper.readValue(responseText, new TypeReference<>() {});
            } catch (Exception e) {
                // 如果解析失败，返回原始文本
                data = Map.of("value", responseText);
            }

            return McpZiweiResponse.builder()
                    .success(true)
                    .data(data)
                    .raw(json)
                    .build();

        } catch (Exception e) {
            throw new McpApiException("解析MCP工具[" + toolName + "]响应失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String parseToolsListResponse(String sse) {
        try {
            if (!StringUtils.hasText(sse)) {
                throw new McpApiException("工具列表响应为空");
            }

            String json = sse.startsWith("data:") ? sse.substring(5).trim() : sse.trim();
            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<>() {});

            if (root.containsKey("error")) {
                throw new McpApiException("MCP返回错误: " + root.get("error"));
            }

            Map<String, Object> result = (Map<String, Object>) root.get("result");
            if (result == null) {
                throw new McpApiException("工具列表响应缺少result字段");
            }

            List<Map<String, Object>> tools = (List<Map<String, Object>>) result.getOrDefault("tools", Collections.emptyList());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tools);
        } catch (Exception e) {
            throw new McpApiException("解析工具列表响应失败", e);
        }
    }

    // ========== 会话管理 ==========

    private void initializeSessionIfNeeded() {
        String sessionId = mcpSessionId.get();
        if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
            log.debug("使用现有紫微斗数MCP会话: {}", sessionId);
            return;
        }
        if (mcpSessionId.compareAndSet(null, INITIALIZING_MARKER)) {
            try {
                log.info("🔄 开始初始化紫微斗数MCP会话...");
                sessionId = createNewSession();
                mcpSessionId.set(sessionId);
                log.info("✅ 紫微斗数MCP会话初始化成功，sessionId: {}", sessionId);
                sendInitializedNotification(sessionId);
            } catch (Exception e) {
                mcpSessionId.set(null);
                log.error("❌ 紫微斗数MCP会话初始化失败", e);
                throw new McpApiException("初始化紫微斗数MCP会话失败: " + e.getMessage(), e);
            }
        } else {
            waitForSessionInitialization();
        }
    }

    private void waitForSessionInitialization() {
        int maxWaitCount = 30;
        int waitCount = 0;
        while (waitCount < maxWaitCount) {
            String sessionId = mcpSessionId.get();
            if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
                log.debug("获取到其他线程初始化的紫微斗数会话: {}", sessionId);
                return;
            }
            try {
                Thread.sleep(100);
                waitCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new McpApiException("等待紫微斗数MCP会话初始化被中断", e);
            }
        }
        throw new McpApiException("等待紫微斗数MCP会话初始化超时");
    }

    private String createNewSession() {
        String initBody = buildInitRequestBody();
        log.info("紫微斗数会话初始化请求体: {}", initBody);

        return mcpWebClient.post()
                .headers(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
                    headers.set("mcp-protocol-version", MCP_PROTOCOL_VERSION);
                    if (StringUtils.hasText(apiKey)) {
                        headers.set("x-api-key", apiKey);
                    }
                })
                .body(BodyInserters.fromValue(initBody))
                .exchangeToMono(this::extractSessionId)
                .retryWhen(buildRetrySpec("会话初始化"))
                .block();
    }

    private Mono<String> extractSessionId(ClientResponse clientResponse) {
        if (clientResponse.statusCode().is4xxClientError()) {
            return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("紫微斗数MCP认证失败: HTTP {} - 响应体: {}",
                                clientResponse.statusCode(), errorBody);
                        String errorDetail = diagnoseHtmlError(errorBody);
                        return Mono.error(new McpApiException(
                                String.format("MCP认证失败: HTTP %d - %s\n建议:\n" +
                                                "1. 检查API密钥有效性\n" +
                                                "2. 确认ms-格式正确\n" +
                                                "3. 控制台重新生成密钥\n" +
                                                "4. 错误详情: %s",
                                        clientResponse.statusCode().value(),
                                        errorDetail,
                                        errorBody)
                        ));
                    });
        }

        if (!clientResponse.statusCode().is2xxSuccessful()) {
            return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(
                            new McpApiException("紫微斗数会话初始化失败，状态码: " +
                                    clientResponse.statusCode() + ", 错误: " + errorBody)
                    ));
        }

        String sessionId = clientResponse.headers().header("Mcp-Session-Id").stream()
                .findFirst()
                .orElseGet(() -> clientResponse.headers().header("Mcp-Session-ID").stream()
                        .findFirst()
                        .orElse(null));

        if (StringUtils.hasText(sessionId)) {
            log.info("✅ 从响应头成功提取紫微斗数sessionId: {}", sessionId);
            return Mono.just(sessionId);
        }

        return clientResponse.bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        log.debug("原始响应内容: {}", responseBody);
                        if (!StringUtils.hasText(responseBody)) {
                            return Mono.error(new McpApiException("服务端返回空响应"));
                        }
                        JsonNode rootNode = objectMapper.readTree(responseBody);
                        String bodySessionId = rootNode.path("result").path("sessionId").asText(null);
                        if (StringUtils.hasText(bodySessionId)) {
                            log.info("✅ 从响应体成功提取sessionId: {}", bodySessionId);
                            return Mono.just(bodySessionId);
                        }
                        String serverName = rootNode.path("result").path("serverInfo").path("name").asText("mcp-server");
                        String generatedSessionId = String.format("MCP-SESSION-%s-%d",
                                serverName.replaceAll("[^a-zA-Z0-9]", "-"),
                                System.currentTimeMillis() / 1000);
                        log.warn("⚠️ 服务端未返回sessionId，生成临时ID: {}", generatedSessionId);
                        return Mono.just(generatedSessionId);
                    } catch (Exception e) {
                        log.error("JSON解析失败: {}", responseBody, e);
                        return Mono.error(new McpApiException("JSON解析失败: " + e.getMessage()));
                    }
                });
    }

    private String diagnoseHtmlError(String html) {
        try {
            String lowerHtml = html.toLowerCase();
            if (lowerHtml.contains("unauthorized") || lowerHtml.contains("请登录") ||
                    lowerHtml.contains("登录") || lowerHtml.contains("sign in")) {
                return "认证失败";
            }
            if (lowerHtml.contains("forbidden") || lowerHtml.contains("无权限")) {
                return "权限不足";
            }
            if (lowerHtml.contains("404") || lowerHtml.contains("not found")) {
                return "路径错误";
            }
            if (lowerHtml.contains("500") || lowerHtml.contains("internal server error")) {
                return "服务端错误";
            }
            return "未知错误";
        } catch (Exception e) {
            return "诊断失败";
        }
    }

    private Mono<Throwable> handleErrorResponse(int statusCodeValue, String errorBody, String operation) {
        if (errorBody.contains("SessionExpired")) {
            mcpSessionId.set(null);
        }
        log.error("{}失败，状态码: {}, 错误: {}", operation, statusCodeValue, errorBody);
        return Mono.error(new McpApiException(operation + "失败: HTTP " + statusCodeValue + " - " + errorBody));
    }

    private String getApiKeyPreview() {
        if (!StringUtils.hasText(apiKey)) {
            return "未配置";
        }
        if (apiKey.length() <= 8) {
            return apiKey;
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    private String buildInitRequestBody() {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", 1);
            request.put("method", "initialize");

            Map<String, Object> params = new HashMap<>();
            params.put("protocolVersion", MCP_PROTOCOL_VERSION);
            params.put("capabilities", Collections.emptyMap());

            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("name", "ZiweiClient");
            clientInfo.put("version", "1.0.0");
            params.put("clientInfo", clientInfo);

            request.put("params", params);
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new McpApiException("构建初始化请求体失败", e);
        }
    }

    private void sendInitializedNotification(String sessionId) {
        String body = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{}}";
        log.debug("发送紫微斗数initialized通知, sessionId: {}", sessionId);

        try {
            mcpWebClient.post()
                    .headers(headers -> {
                        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
                        headers.set("mcp-session-id", sessionId);
                        if (StringUtils.hasText(apiKey)) {
                            headers.set("x-api-key", apiKey);
                        }
                    })
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(buildRetrySpec("发送initialized通知"))
                    .block();
        } catch (Exception e) {
            log.warn("发送initialized通知失败: {}", e.getMessage());
        }
    }

    private Retry buildRetrySpec(String operation) {
        return Retry.fixedDelay(MAX_RETRY, Duration.ofSeconds(RETRY_INTERVAL_SECONDS))
                .filter(ex -> ex instanceof WebClientResponseException responseEx &&
                        (responseEx.getStatusCode().is5xxServerError() ||
                                responseEx.getStatusCode().value() == 429))
                .doBeforeRetry(signal -> log.warn("{}失败，开始第{}次重试", operation, signal.totalRetries() + 1))
                .onRetryExhaustedThrow((spec, signal) -> {
                    Throwable cause = signal.failure();
                    return new McpApiException(String.format("%s重试%d次后仍失败", operation, MAX_RETRY), cause);
                });
    }

    private void setCommonRequestHeaders(HttpHeaders headers, String sessionId) {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
        headers.set("mcp-session-id", sessionId);
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        if (StringUtils.hasText(apiKey)) {
            headers.set("x-api-key", apiKey);
        }
    }
}
