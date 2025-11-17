package com.example.demo.client;

import com.example.demo.dto.request.yijing.*;
import com.example.demo.dto.response.McpCallResult;
import com.example.demo.exception.McpApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
@Slf4j
public class McpYijingClient {

    private static final String MCP_PROTOCOL_VERSION = "2025-03-26";
    private static final String MCP_ENDPOINT = "";  // ✅ 明确指定SSE端点
    private static final int MAX_RETRY = 3;
    private static final long RETRY_INTERVAL_SECONDS = 1;
    private static final String INITIALIZING_MARKER = "INITIALIZING";

    private final WebClient mcpWebClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final AtomicReference<String> mcpSessionId = new AtomicReference<>();
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    public McpYijingClient(@Qualifier("yijingWebClient") WebClient mcpWebClient,
                           ObjectMapper objectMapper,
                           @Value("${mcp.yijing.api.api-key:}") String apiKey) {
        this.mcpWebClient = mcpWebClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("❌ 关键错误: mcp.yijing.api.api-key 未配置！");
        }
        log.info("✅ MCP Yijing API Key已加载: {}", getApiKeyPreview());
    }


    // ========== 公共业务方法 ==========

    public String listAvailableTools() {
        initializeSessionIfNeeded();
        String requestBody = buildListToolsRequestBody();
        log.debug("查询工具列表请求: {}", requestBody);

        try {
            String sseResponse = mcpWebClient.post()
                    .uri(MCP_ENDPOINT)
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

    public McpCallResult generateHexagram(YijingGenerateHexagramRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("question", request.getQuestion());
        String method = "virtual_coin".equals(request.getMethod()) ? "random" : request.getMethod();
        args.put("method", method);
        if (StringUtils.hasText(request.getSeed())) {
            args.put("seed", request.getSeed());
        }
        return callTool("yijing_generate_hexagram", args);
    }

    public McpCallResult interpretHexagram(YijingInterpretRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("hexagram", request.getHexagram());
        Optional.ofNullable(request.getFocus()).ifPresent(v -> args.put("focus", v));
        Optional.ofNullable(request.getLineNumber()).ifPresent(v -> args.put("line_number", v));
        Optional.ofNullable(request.getContext()).ifPresent(v -> args.put("context", v));
        Optional.ofNullable(request.getDetailLevel()).ifPresent(v -> args.put("detail_level", v));
        return callTool("yijing_interpret", args);
    }

    public McpCallResult advise(YijingAdviseRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("hexagram", request.getHexagram());
        args.put("question", request.getQuestion());
        Optional.ofNullable(request.getOptions()).ifPresent(v -> args.put("options", v));
        Optional.ofNullable(request.getTimeFrame()).ifPresent(v -> args.put("time_frame", v));
        Optional.ofNullable(request.getContext()).ifPresent(v -> args.put("context", v));
        return callTool("yijing_advise", args);
    }

    public McpCallResult generateBaziChart(YijingBaziGenerateChartRequest request) {
        Map<String, Object> args = objectMapper.convertValue(request, new TypeReference<>() {});
        return callTool("bazi_generate_chart", args);
    }

    public McpCallResult analyzeBazi(YijingBaziAnalyzeRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chart", request.getChart());
        Optional.ofNullable(request.getAnalysisType()).ifPresent(v -> args.put("analysis_type", v));
        Optional.ofNullable(request.getDetailLevel()).ifPresent(v -> args.put("detail_level", v));
        return callTool("bazi_analyze", args);
    }

    public McpCallResult forecastBazi(YijingBaziForecastRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chart", request.getChart());
        args.put("start_date", request.getStartDate());
        args.put("end_date", request.getEndDate());
        Optional.ofNullable(request.getAspects()).ifPresent(v -> args.put("aspects", v));
        Optional.ofNullable(request.getResolution()).ifPresent(v -> args.put("resolution", v));
        return callTool("bazi_forecast", args);
    }

    public McpCallResult combinedAnalysis(YijingCombinedAnalysisRequest request) {
        Map<String, Object> args = new HashMap<>();
        Optional.ofNullable(request.getQuestion()).ifPresent(v -> args.put("question", v));
        Optional.ofNullable(request.getAnalysisAspects()).ifPresent(v -> args.put("analysis_aspects", v));
        Optional.ofNullable(request.getContext()).ifPresent(v -> args.put("context", v));
        return callTool("mcp_combined_analysis", args);
    }

    public McpCallResult destinyConsult(YijingDestinyConsultRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("question", request.getQuestion());
        args.put("consultation_type", request.getConsultationType());
        Optional.ofNullable(request.getContext()).ifPresent(v -> args.put("context", v));
        return callTool("mcp_destiny_consult", args);
    }

    public McpCallResult knowledgeLearn(YijingKnowledgeLearnRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("topic", request.getTopic());
        args.put("system", request.getSystem());
        args.put("level", request.getLevel());
        Optional.ofNullable(request.getFormat()).ifPresent(v -> args.put("format", v));
        return callTool("mcp_knowledge_learn", args);
    }

    public McpCallResult caseStudy(YijingCaseStudyRequest request) {
        Map<String, Object> args = new HashMap<>();
        Optional.ofNullable(request.getCaseId()).ifPresent(v -> args.put("case_id", v));
        Optional.ofNullable(request.getSystem()).ifPresent(v -> args.put("system", v));
        Optional.ofNullable(request.getCategory()).ifPresent(v -> args.put("category", v));
        Optional.ofNullable(request.getAnalysisFocus()).ifPresent(v -> args.put("analysis_focus", v));
        return callTool("mcp_case_study", args);
    }

    // ========== 私有工具方法 ==========
    private String buildListToolsRequestBody() {
        int requestId = requestIdCounter.incrementAndGet();
        return String.format(
                "{\"jsonrpc\":\"2.0\",\"id\":%d,\"method\":\"tools/list\",\"params\":{\"_meta\":{\"progressToken\":0}}}",
                requestId
        );
    }

    private McpCallResult callTool(String toolName, Map<String, Object> arguments) {
        initializeSessionIfNeeded();
        String body = buildToolCallRequest(toolName, arguments);
        log.debug("调用工具[{}] 请求体: {}", toolName, body);

        String sseResponse = mcpWebClient.post()
                .uri(MCP_ENDPOINT)
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

        return parseToolResponse(sseResponse);
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
    private McpCallResult parseToolResponse(String sse) {
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
            return McpCallResult.builder()
                    .success(true)
                    .data(parseJsonToMap(responseText))
                    .raw(json)
                    .build();

        } catch (Exception e) {
            throw new McpApiException("解析MCP响应失败", e);
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String jsonText) {
        try {
            return objectMapper.readValue(jsonText, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("无法解析响应文本为JSON: {}", e.getMessage());
            return Map.of("value", jsonText);
        }
    }

    // ========== 会话管理 ==========

    private void waitForSessionInitialization() {
        int maxWaitCount = 30;
        int waitCount = 0;
        while (waitCount < maxWaitCount) {
            String sessionId = mcpSessionId.get();
            if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
                log.debug("获取到其他线程初始化的Yijing会话: {}", sessionId);
                return;
            }
            try {
                Thread.sleep(100);
                waitCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new McpApiException("等待Yijing MCP会话初始化被中断", e);
            }
        }
        throw new McpApiException("等待Yijing MCP会话初始化超时");
    }

    private void initializeSessionIfNeeded() {
        String sessionId = mcpSessionId.get();
        if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
            log.debug("使用现有Yijing MCP会话: {}", sessionId);
            return;
        }
        if (mcpSessionId.compareAndSet(null, INITIALIZING_MARKER)) {
            try {
                log.info("🔄 开始初始化Yijing MCP会话...");
                sessionId = createNewSession();
                mcpSessionId.set(sessionId);
                log.info("✅ Yijing MCP会话初始化成功，sessionId: {}", sessionId);
                sendInitializedNotification(sessionId);
            } catch (Exception e) {
                mcpSessionId.set(null);
                log.error("❌ Yijing MCP会话初始化失败", e);
                throw new McpApiException("初始化Yijing MCP会话失败: " + e.getMessage(), e);
            }
        } else {
            waitForSessionInitialization();
        }
    }

    private String createNewSession() {
        String initBody = buildInitRequestBody();
        log.info("会话初始化请求体: {}", initBody);

        return mcpWebClient.post()
                .uri(MCP_ENDPOINT)
                .headers(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
                    headers.set("mcp-protocol-version", MCP_PROTOCOL_VERSION);
                    // ✅ 关键修复：只使用 x-api-key
                    if (StringUtils.hasText(apiKey)) {
                        headers.set("x-api-key", apiKey);
                    }
                })
                .body(BodyInserters.fromValue(initBody))
                .exchangeToMono(this::extractSessionId)
                .retryWhen(buildRetrySpec("会话初始化"))
                .block();
    }

    // ========== 核心修复：Session ID提取方法 ==========

    private Mono<String> extractSessionId(ClientResponse clientResponse) {
        // 第一步：检查HTTP状态码
        if (clientResponse.statusCode().is4xxClientError()) {
            return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("MCP认证失败: HTTP {} - 响应体: {}",
                                clientResponse.statusCode(), errorBody);

                        String errorDetail = diagnoseHtmlError(errorBody);

                        // ✅ 修复：正确的参数数量和类型
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
                            new McpApiException("会话初始化失败，状态码: " +
                                    clientResponse.statusCode() + ", 错误: " + errorBody)
                    ));
        }

        // 第二步：从响应头获取sessionId
        String sessionId = clientResponse.headers().header("Mcp-Session-Id").stream()
                .findFirst()
                .orElseGet(() -> clientResponse.headers().header("Mcp-Session-ID").stream()
                        .findFirst()
                        .orElse(null));

        if (StringUtils.hasText(sessionId)) {
            log.info("✅ 从响应头成功提取sessionId: {}", sessionId);
            return Mono.just(sessionId);
        }

        // 第三步：从响应体提取或生成
        return clientResponse.bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        log.debug("原始响应内容: {}", responseBody);

                        if (!StringUtils.hasText(responseBody)) {
                            return Mono.error(new McpApiException("服务端返回空响应"));
                        }

                        JsonNode rootNode = objectMapper.readTree(responseBody);
                        JsonNode resultNode = rootNode.path("result");

                        if (resultNode.isMissingNode()) {
                            return Mono.error(new McpApiException("响应缺少result字段"));
                        }

                        String bodySessionId = resultNode.path("sessionId").asText(null);

                        if (StringUtils.hasText(bodySessionId)) {
                            log.info("✅ 从响应体成功提取sessionId: {}", bodySessionId);
                            return Mono.just(bodySessionId);
                        }

                        // 生成临时ID（兼容无状态服务）
                        String serverName = resultNode.path("serverInfo").path("name").asText("mcp-server");
                        String serverVersion = resultNode.path("serverInfo").path("version").asText("1.0");
                        String generatedSessionId = String.format("MCP-SESSION-%s-%s-%d",
                                serverName.replaceAll("[^a-zA-Z0-9]", "-"),
                                serverVersion,
                                System.currentTimeMillis() / 1000);

                        log.warn("⚠️ 服务端未返回sessionId，生成临时ID: {}", generatedSessionId);
                        return Mono.just(generatedSessionId);

                    } catch (Exception e) {
                        log.error("JSON解析失败: {}", responseBody, e);
                        return Mono.error(new McpApiException("JSON解析失败: " + e.getMessage()));
                    }
                });
    }

    // ========== 辅助方法 ==========

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

    // ✅ 终极修复：使用 int 类型，彻底避免 HttpStatusCode 问题
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
            clientInfo.put("name", "YijingClient");
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
        log.debug("发送initialized通知, sessionId: {}", sessionId);

        try {
            mcpWebClient.post()
                    .uri(MCP_ENDPOINT)
                    .headers(headers -> {
                        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
                        headers.set("mcp-session-id", sessionId);
                        // ✅ 只使用 x-api-key
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
        // ✅ 关键修复：只使用 x-api-key
        if (StringUtils.hasText(apiKey)) {
            headers.set("x-api-key", apiKey);
        }
    }
}