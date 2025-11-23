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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class McpYijingClient {

    private static final String MCP_PROTOCOL_VERSION = "2025-03-26";
    private final String mcpEndpoint;
    private static final int MAX_RETRY = 3;
    private static final long RETRY_INTERVAL_SECONDS = 1;
    private static final String INITIALIZING_MARKER = "INITIALIZING";

    private final WebClient mcpWebClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final AtomicReference<String> mcpSessionId = new AtomicReference<>();
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    // 关键修改：通过构造函数注入配置项，确保初始化顺序正确
    public McpYijingClient(@Qualifier("yijingWebClient") WebClient mcpWebClient,
                           ObjectMapper objectMapper,
                           @Value("${mcp.yijing.api.api-key:}") String apiKey,
                           @Value("${mcp.yijing.api.endpoint:}") String mcpEndpoint) {
        this.mcpWebClient = mcpWebClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.mcpEndpoint = mcpEndpoint;

        // 启动时校验核心配置
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("❌ 关键错误: mcp.yijing.api.api-key 未配置！");
        }
        if (!StringUtils.hasText(mcpEndpoint)) {
            throw new IllegalStateException("❌ 关键错误: mcp.yijing.api.endpoint 未配置！");
        }

        log.info("✅ MCP Yijing 初始化完成 - 端点: {}, API Key预览: {}",
                mcpEndpoint, getApiKeyPreview());
    }


    // ========== 公共业务方法 ==========

    public McpCallResult listAvailableTools() {
        initializeSessionIfNeeded();
        String requestBody = buildListToolsRequestBody();
        log.info("【工具列表查询】请求体: {}", requestBody);

        try {
            AtomicReference<String> firstChunk = new AtomicReference<>();
            String sseResponse = mcpWebClient.post()
                    .uri(mcpEndpoint)
                    .headers(headers -> setCommonRequestHeaders(headers, mcpSessionId.get()))
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), res ->
                            res.bodyToMono(String.class)
                                    .flatMap(err -> handleErrorResponse(res.statusCode().value(), err, "查询工具列表"))
                    )
                    .bodyToFlux(String.class)
                    .doOnNext(firstChunk::set)
                    .take(1)
                    .single()
                    .onErrorResume(PrematureCloseException.class, e -> {
                        if (firstChunk.get() != null) {
                            log.warn("【工具列表查询】连接提前关闭，但已收到首个事件，使用缓存响应。");
                            return Mono.just(firstChunk.get());
                        }
                        return Mono.error(e);
                    })
                    .retryWhen(buildRetrySpec("查询工具列表"))
                    .block();

            return parseToolsResponse(sseResponse);
        } catch (Exception e) {
            log.error("【工具列表查询】异常", e);
            throw new McpApiException("获取可用工具列表失败: " + e.getMessage(), e);
        }
    }

    public McpCallResult generateHexagram(YijingGenerateHexagramRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("question", request.getQuestion());
        String method = "virtual_coin".equals(request.getMethod()) ? "random" : request.getMethod();
        args.put("method", method);
        Optional.ofNullable(request.getSeed()).ifPresent(v -> args.put("seed", v));
        log.info("【生成卦象】参数: {}", args);
        return callTool("yijing_generate_hexagram", args);
    }

    public McpCallResult generateBaziChart(YijingBaziGenerateChartRequest request) {
        Map<String, Object> args = objectMapper.convertValue(request, new TypeReference<>() {});
        log.info("【生成八字 chart】参数: {}", args);
        return callTool("bazi_generate_chart", args);
    }

    public McpCallResult analyzeBazi(YijingBaziAnalyzeRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chart", request.getChart());
        args.put("analysis_type", request.getAnalysisType());
        Optional.ofNullable(request.getDetailLevel()).ifPresent(v -> args.put("detail_level", v));
        log.info("【分析八字】参数: {}", args);
        return callTool("bazi_analyze", args);
    }

    public McpCallResult forecastBazi(YijingBaziForecastRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("chart", request.getChart());
        args.put("start_date", request.getStartDate());
        args.put("end_date", request.getEndDate());
        args.put("aspects", request.getAspects());
        Optional.ofNullable(request.getResolution()).ifPresent(v -> args.put("resolution", v));
        log.info("【八字预测】参数: {}", args);
        return callTool("bazi_forecast", args);
    }

    public McpCallResult combinedAnalysis(YijingCombinedAnalysisRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("question", request.getQuestion());
        args.put("bazi_chart", request.getBaziChart());
        Optional.ofNullable(request.getHexagram()).ifPresent(v -> args.put("hexagram", v));
        Optional.ofNullable(request.getAnalysisAspects()).ifPresent(v -> args.put("analysis_aspects", v));
        log.info("【综合分析】参数: {}", args);
        return callTool("mcp_combined_analysis", args);
    }

    public McpCallResult destinyConsult(YijingDestinyConsultRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("user_profile", request.getUserProfile());
        args.put("question", request.getQuestion());
        args.put("consultation_type", request.getConsultationType());
        Optional.ofNullable(request.getContext()).ifPresent(v -> args.put("context", v));
        log.info("【命理咨询】参数: {}", args);
        return callTool("mcp_destiny_consult", args);
    }

    public McpCallResult knowledgeLearn(YijingKnowledgeLearnRequest request) {
        Map<String, Object> args = new HashMap<>();
        args.put("topic", request.getTopic());
        args.put("system", request.getSystem());
        args.put("level", request.getLevel());
        args.put("learning_type", request.getLearningType());
        Optional.ofNullable(request.getFormat()).ifPresent(v -> args.put("format", v));
        log.info("【知识学习】参数: {}", args);
        return callTool("mcp_knowledge_learn", args);
    }

    public McpCallResult caseStudy(YijingCaseStudyRequest request) {
        Map<String, Object> args = new HashMap<>();
        Optional.ofNullable(request.getCaseId()).ifPresent(v -> args.put("case_id", v));
        args.put("system", request.getSystem());
        Optional.ofNullable(request.getCategory()).ifPresent(v -> args.put("category", v));
        Optional.ofNullable(request.getAnalysisFocus()).ifPresent(v -> args.put("analysis_focus", v));
        log.info("【案例研究】参数: {}", args);
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
        log.info("【调用工具】工具名: {}, 会话ID: {}, 请求体: {}",
                toolName, mcpSessionId.get(), body);

        try {
            AtomicReference<String> firstChunk = new AtomicReference<>();
            String sseResponse = mcpWebClient.post()
                    .uri(mcpEndpoint)
                    .headers(headers -> setCommonRequestHeaders(headers, mcpSessionId.get()))
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), res ->
                            res.bodyToMono(String.class)
                                    .flatMap(err -> handleErrorResponse(
                                            res.statusCode().value(),
                                            err,
                                            "调用工具[" + toolName + "]"
                                    ))
                    )
                    .bodyToFlux(String.class)
                    .doOnNext(firstChunk::set)
                    .take(1)
                    .single()
                    .onErrorResume(PrematureCloseException.class, e -> {
                        if (firstChunk.get() != null) {
                            log.warn("【调用工具】{} 连接提前关闭，但已收到首个事件，使用缓存响应。", toolName);
                            return Mono.just(firstChunk.get());
                        }
                        return Mono.error(e);
                    })
                    .retryWhen(buildRetrySpec("调用工具 " + toolName))
                    .block();

            log.info("【调用工具】原始响应[{}]: [START]{}[END]",
                    toolName, sseResponse);

            return parseCommonResponse(sseResponse, "工具[" + toolName + "]");
        } catch (Exception e) {
            log.error("【调用工具】{} 异常", toolName, e);
            throw new McpApiException("调用工具[" + toolName + "]失败: " + e.getMessage(), e);
        }
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

    /**
     * 公共响应解析方法（优化后：优先处理服务端错误标记）
     */
    @SuppressWarnings("unchecked")
    private McpCallResult parseCommonResponse(String sse, String operation) {
        try {
            if (!StringUtils.hasText(sse)) {
                log.error("【解析响应】{} 响应为空", operation);
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "响应为空")
                        .raw("")
                        .build();
            }

            String extractedContent = sse.startsWith("data:") ? sse.substring(5).trim() : sse.trim();
            log.info("【解析响应】{} 提取后内容: [START]{}[END]",
                    operation, extractedContent);

            // 解析根JSON
            Map<String, Object> root;
            try {
                root = objectMapper.readValue(extractedContent, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.warn("【解析响应】{} 根内容非JSON格式", operation, e);
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "响应格式错误（非JSON）: " + extractedContent)
                        .raw(extractedContent)
                        .build();
            }

            // 检查顶层错误
            if (root.containsKey("error")) {
                String errorMsg = root.get("error").toString();
                log.error("【解析响应】{} MCP返回错误: {}", operation, errorMsg);
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "失败: " + errorMsg)
                        .raw(extractedContent)
                        .build();
            }

            // 解析result字段
            Map<String, Object> result = (Map<String, Object>) root.get("result");
            if (result == null) {
                log.error("【解析响应】{} 响应缺少result字段", operation);
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "响应缺少result字段")
                        .raw(extractedContent)
                        .build();
            }

            // 关键修改：优先处理服务端错误标记（isError: true）
            boolean isError = (Boolean) result.getOrDefault("isError", false);
            if (isError) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
                String errorText = content != null && !content.isEmpty()
                        ? (String) content.get(0).getOrDefault("text", "服务端返回未知错误")
                        : "服务端返回错误但无详细信息";

                log.error("【解析响应】{} 服务端明确标记错误: {}", operation, errorText);
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "服务端错误: " + errorText)
                        .raw(extractedContent)
                        .build();
            }

            // 解析content字段
            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            if (content == null || content.isEmpty()) {
                log.error("【解析响应】{} 响应缺少content字段", operation);
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "响应缺少content字段")
                        .raw(extractedContent)
                        .build();
            }

            // 解析content.text
            String responseText = (String) content.get(0).get("text");
            if (!StringUtils.hasText(responseText)) {
                log.error("【解析响应】{} content.text为空", operation);
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "content.text为空")
                        .raw(extractedContent)
                        .build();
            }

            // 解析text为JSON，失败则包装为文本
            Object data;
            try {
                data = objectMapper.readValue(responseText, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.warn("【解析响应】{} content.text非JSON格式，原始文本: {}",
                        operation, responseText, e);
                data = Map.of("text", responseText);
            }

            return McpCallResult.builder()
                    .success(true)
                    .data(data)
                    .raw(extractedContent)
                    .build();

        } catch (Exception e) {
            String errorMsg = operation + "解析响应失败: " + e.getMessage();
            log.error("【解析响应】{} 异常", operation, e);
            return McpCallResult.builder()
                    .success(false)
                    .errorMsg(errorMsg)
                    .raw(sse)
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private McpCallResult parseToolsResponse(String sse) {
        final String operation = "工具列表查询";
        try {
            if (!StringUtils.hasText(sse)) {
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "响应为空")
                        .raw("")
                        .build();
            }

            String extractedContent = sse.startsWith("data:") ? sse.substring(5).trim() : sse.trim();
            Map<String, Object> root = objectMapper.readValue(extractedContent, new TypeReference<>() {});

            if (root.containsKey("error")) {
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "失败: " + root.get("error"))
                        .raw(extractedContent)
                        .build();
            }

            Map<String, Object> result = (Map<String, Object>) root.get("result");
            if (result == null) {
                return McpCallResult.builder()
                        .success(false)
                        .errorMsg(operation + "响应缺少result字段")
                        .raw(extractedContent)
                        .build();
            }

            List<Map<String, Object>> tools = (List<Map<String, Object>>) result.get("tools");
            return McpCallResult.builder()
                    .success(true)
                    .data(tools == null ? Collections.emptyList() : tools)
                    .raw(extractedContent)
                    .build();
        } catch (Exception e) {
            return McpCallResult.builder()
                    .success(false)
                    .errorMsg(operation + "解析失败: " + e.getMessage())
                    .raw(sse)
                    .build();
        }
    }

    // ========== 会话管理 ==========

    private void waitForSessionInitialization() {
        int maxWaitCount = 30;
        int waitCount = 0;
        while (waitCount < maxWaitCount) {
            String sessionId = mcpSessionId.get();
            if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
                log.debug("【会话管理】获取到其他线程初始化的会话: {}", sessionId);
                return;
            }
            try {
                Thread.sleep(100);
                waitCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new McpApiException("等待会话初始化被中断", e);
            }
        }
        throw new McpApiException("等待会话初始化超时");
    }

    private void initializeSessionIfNeeded() {
        String sessionId = mcpSessionId.get();
        if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
            log.debug("【会话管理】使用现有会话: {}", sessionId);
            return;
        }
        if (mcpSessionId.compareAndSet(null, INITIALIZING_MARKER)) {
            try {
                log.info("【会话管理】开始初始化新会话...");
                sessionId = createNewSession();
                mcpSessionId.set(sessionId);
                log.info("【会话管理】初始化成功，sessionId: {}", sessionId);
                sendInitializedNotification(sessionId);
            } catch (Exception e) {
                mcpSessionId.set(null);
                log.error("【会话管理】初始化失败", e);
                throw new McpApiException("初始化会话失败: " + e.getMessage(), e);
            }
        } else {
            waitForSessionInitialization();
        }
    }

    private String createNewSession() {
        String initBody = buildInitRequestBody();
        log.info("【会话初始化】请求体: {}", initBody);

        return mcpWebClient.post()
                .uri(mcpEndpoint)
                .headers(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
                    headers.set(HttpHeaders.ACCEPT_ENCODING, "identity");
                    headers.set(HttpHeaders.CONNECTION, "keep-alive");
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
        if (!clientResponse.statusCode().is2xxSuccessful()) {
            return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        int statusCode = clientResponse.statusCode().value();
                        log.error("【会话提取】状态码: {}, 错误响应: [START]{}[END]",
                                statusCode, errorBody);
                        String errorDetail = diagnoseHtmlError(errorBody);
                        return Mono.error(new McpApiException(
                                String.format("会话初始化失败: HTTP %d - %s，详情: %s",
                                        statusCode, errorDetail, errorBody)
                        ));
                    });
        }

        // 从响应头提取sessionId
        String sessionId = clientResponse.headers().header("Mcp-Session-Id").stream()
                .findFirst()
                .orElseGet(() -> clientResponse.headers().header("Mcp-Session-ID").stream()
                        .findFirst()
                        .orElse(null));

        if (StringUtils.hasText(sessionId)) {
            log.info("【会话提取】从响应头获取sessionId: {}", sessionId);
            return Mono.just(sessionId);
        }

        // 从响应体提取sessionId
        return clientResponse.bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        log.info("【会话提取】响应体内容: [START]{}[END]", responseBody);
                        if (!StringUtils.hasText(responseBody)) {
                            return Mono.error(new McpApiException("会话响应体为空"));
                        }

                        JsonNode rootNode = objectMapper.readTree(responseBody);
                        JsonNode resultNode = rootNode.path("result");
                        if (resultNode.isMissingNode()) {
                            return Mono.error(new McpApiException("会话响应缺少result字段"));
                        }

                        String bodySessionId = resultNode.path("sessionId").asText(null);
                        if (StringUtils.hasText(bodySessionId)) {
                            log.info("【会话提取】从响应体获取sessionId: {}", bodySessionId);
                            return Mono.just(bodySessionId);
                        }

                        // 生成临时会话ID
                        String generatedId = "TEMP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
                        log.warn("【会话提取】服务端未返回sessionId，生成临时ID: {}", generatedId);
                        return Mono.just(generatedId);

                    } catch (Exception e) {
                        log.error("【会话提取】解析响应体失败", e);
                        return Mono.error(new McpApiException("解析会话响应失败: " + e.getMessage()));
                    }
                });
    }

    // ========== 辅助方法 ==========

    private String diagnoseHtmlError(String html) {
        try {
            String lowerHtml = html.toLowerCase();
            if (lowerHtml.contains("unauthorized") || lowerHtml.contains("请登录")) {
                return "认证失败（API Key无效）";
            }
            if (lowerHtml.contains("forbidden") || lowerHtml.contains("无权限")) {
                return "权限不足（无访问工具权限）";
            }
            if (lowerHtml.contains("404") || lowerHtml.contains("not found")) {
                return "端点错误（MCP_ENDPOINT配置错误）";
            }
            if (lowerHtml.contains("500") || lowerHtml.contains("internal server error")) {
                return "服务端内部错误";
            }
            return "未知错误";
        } catch (Exception e) {
            return "诊断失败: " + e.getMessage();
        }
    }

    private Mono<Throwable> handleErrorResponse(int statusCode, String errorBody, String operation) {
        if (errorBody.contains("SessionExpired")) {
            mcpSessionId.set(null);
            log.warn("【错误处理】{} 会话已过期，将重新初始化", operation);
        }
        log.error("【错误处理】{} 失败，状态码: {}, 响应: [START]{}[END]",
                operation, statusCode, errorBody);
        return Mono.error(new McpApiException(operation + "失败: HTTP " + statusCode + " - " + errorBody));
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
        log.debug("【发送通知】initialized通知，sessionId: {}", sessionId);

        try {
            mcpWebClient.post()
                    .uri(mcpEndpoint)
                    .headers(headers -> {
                        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
                        headers.set(HttpHeaders.ACCEPT_ENCODING, "identity");
                        headers.set(HttpHeaders.CONNECTION, "keep-alive");
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
            log.warn("【发送通知】失败: {}", e.getMessage());
        }
    }

    private Retry buildRetrySpec(String operation) {
        return Retry.fixedDelay(MAX_RETRY, Duration.ofSeconds(RETRY_INTERVAL_SECONDS))
                .filter(ex -> ex instanceof WebClientResponseException responseEx &&
                        (responseEx.getStatusCode().is5xxServerError() ||
                                responseEx.getStatusCode().value() == 429))
                .doBeforeRetry(signal -> log.warn("【重试机制】{} 第{}次重试（原因: {}）",
                        operation, signal.totalRetries() + 1, signal.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) -> {
                    Throwable cause = signal.failure();
                    return new McpApiException(String.format("%s 重试%d次后失败", operation, MAX_RETRY), cause);
                });
    }

    private void setCommonRequestHeaders(HttpHeaders headers, String sessionId) {
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
        headers.set(HttpHeaders.ACCEPT_ENCODING, "identity");
        headers.set(HttpHeaders.CONNECTION, "keep-alive");
        headers.set("mcp-session-id", sessionId);
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        if (StringUtils.hasText(apiKey)) {
            headers.set("x-api-key", apiKey);
        }
    }
}