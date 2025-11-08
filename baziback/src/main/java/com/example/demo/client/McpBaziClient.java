package com.example.demo.client;

import com.example.demo.dto.request.McpBaziRequest;
import com.example.demo.dto.response.McpBaziResponse;
import com.example.demo.exception.McpApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class McpBaziClient {
    // 公共常量
    private static final String MCP_PROTOCOL_VERSION = "2025-03-26";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0.0 Safari/537.36 Edg/141.0.0.0.0";
    private static final String ORIGIN = "https://www.modelscope.cn";
    private static final String REFERER = "https://www.modelscope.cn/";
    private static final int MAX_RETRY = 3;
    private static final long RETRY_INTERVAL_SECONDS = 1;
    private static final String INITIALIZING_MARKER = "INITIALIZING";

    // 依赖与状态管理
    private final WebClient mcpWebClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final AtomicReference<String> mcpSessionId = new AtomicReference<>();
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    public McpBaziClient(
            @Qualifier("baziWebClient") WebClient mcpWebClient,
            ObjectMapper objectMapper,
            @Value("${mcp.bazi.api.api-key}") String apiKey
    ) {
        this.mcpWebClient = mcpWebClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }
    // ========== 新增：获取可用工具列表 ==========
    /**
     * 获取MCP服务支持的八字相关可用工具列表
     */
    public String listAvailableTools() {
        initializeSessionIfNeeded();

        String requestBody = buildListToolsRequestBody();
        log.debug("=== 发送可用工具列表查询请求 ===\n{}", requestBody);
        try {
            String sseResponse = mcpWebClient.post()
                    .headers(headers -> setCommonRequestHeaders(headers, mcpSessionId.get()))
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), res -> res.bodyToMono(String.class)
                            .flatMap(err -> {
                                if (err.contains("SessionExpired")) {
                                    mcpSessionId.set(null);
                                }
                                return Mono.error(new McpApiException("查询可用工具失败:" + err));
                            }))
                    .bodyToFlux(String.class)
                    .take(1)
                    .single()
                    .retryWhen(Retry.fixedDelay(MAX_RETRY, Duration.ofSeconds(RETRY_INTERVAL_SECONDS))
                            .filter(ex -> ex instanceof McpApiException && ex.getMessage().contains("SessionExpired")))
                    .block();

            // 解析SSE响应，提取工具列表
            return parseToolsListResponse(sseResponse);
        } catch (Exception e) {
            log.error("查询可用工具列表异常", e);
            throw new McpApiException("获取可用工具列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建查询工具列表的请求体
     */
    private String buildListToolsRequestBody() {
        int requestId = requestIdCounter.incrementAndGet();
        // MCP服务标准工具列表查询方法：tools/list
        return String.format(
                "{\"jsonrpc\":\"2.0\",\"id\":%d,\"method\":\"tools/list\",\"params\":{\"_meta\":{\"progressToken\":0}}}",
                requestId
        );
    }

    /**
     * 解析工具列表响应（返回JSON格式字符串，便于前端处理）
     */
    private String parseToolsListResponse(String sse) {
        try {
            if (!hasText(sse)) {
                throw new McpApiException("工具列表响应为空");
            }

            // 提取SSE中的JSON数据
            String json = sse.startsWith("data:") ? sse.substring(5).trim() : sse.trim();
            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            if (root.containsKey("error")) {
                throw new McpApiException("MCP返回错误: " + root.get("error"));
            }

            Map<String, Object> result = (Map<String, Object>) root.get("result");
            if (result == null) {
                throw new McpApiException("工具列表响应缺少result字段");
            }

            // 提取工具列表（MCP服务通常返回tools数组，包含name/description等字段）
            List<Map<String, Object>> tools = (List<Map<String, Object>>) result.getOrDefault("tools", new ArrayList<>());
            log.debug("查询到可用工具数量: {}", tools.size());

            // 转换为格式化的JSON字符串返回
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tools);
        } catch (McpApiException e) {
            throw e;
        } catch (Exception e) {
            throw new McpApiException("解析工具列表响应失败", e);
        }
    }
    // ========== 新增方法结束 ==========

    private String getClientName() {
        return "BaziClient";
    }

    /**
     * 获取八字详情
     */
    public McpBaziResponse getBaziDetail(McpBaziRequest request) {
        validateBaziRequest(request);
        initializeSessionIfNeeded();

        String body = buildMcpRequestBodyAsJson(request);
        log.debug("=== 发送八字查询请求 ===\n{}", body);

        return mcpWebClient.post()
                .headers(headers -> setCommonRequestHeaders(headers, mcpSessionId.get()))
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), res -> res.bodyToMono(String.class)
                        .flatMap(err -> {
                            if (err.contains("SessionExpired")) {
                                mcpSessionId.set(null);
                            }
                            return Mono.error(new McpApiException("八字查询失败:" + err));
                        }))
                .bodyToFlux(String.class)
                .take(1)
                .single()
                .map(this::parseBaziSseResponse)
                .retryWhen(Retry.fixedDelay(MAX_RETRY, Duration.ofSeconds(RETRY_INTERVAL_SECONDS))
                        .filter(ex -> ex instanceof McpApiException && ex.getMessage().contains("SessionExpired")))
                .block();
    }
    /**
     * 解析SSE响应为McpBaziResponse
     */
    private McpBaziResponse parseBaziSseResponse(String sse) {
        try {
            if (!hasText(sse)) {
                throw new McpApiException("SSE响应为空");
            }
            String json = sse.startsWith("data:") ? sse.substring(5).trim() : sse.trim();
            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            if (root.containsKey("error")) {
                throw new McpApiException("MCP返回错误: " + root.get("error"));
            }

            Map<String, Object> result = (Map<String, Object>) root.get("result");
            if (result == null) {
                throw new McpApiException("缺少result字段");
            }

            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            if (content == null || content.isEmpty()) {
                throw new McpApiException("缺少content字段");
            }

            String baziText = (String) content.get(0).get("text");
            McpBaziResponse resp = new McpBaziResponse();
            resp.setBaziText(baziText);
            resp.setRawResponse(json);
            resp.setBaziData(parseBaziData(baziText));

            log.debug("八字响应解析成功，文本长度：{}", baziText == null ? 0 : baziText.length());
            return resp;
        } catch (McpApiException e) {
            throw e;
        } catch (Exception e) {
            throw new McpApiException("解析SSE响应失败", e);
        }
    }

    /**
     * 解析八字文本为结构化数据
     */
    private Map<String, Object> parseBaziData(String baziText) {
        if (!hasText(baziText)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(baziText, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("八字文本反序列化失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 构建八字请求体
     */
    private String buildMcpRequestBodyAsJson(McpBaziRequest request) {
        Integer sect = request.getEightCharProviderSect() == null ? 2 : request.getEightCharProviderSect();
        if (sect < 1 || sect > 2) {
            throw new McpApiException("eightCharProviderSect 必须为 1 或 2");
        }
        Integer gender = request.getGender();
        if (gender == null || gender < 0 || gender > 1) {
            throw new McpApiException("gender 必须为 0 或 1");
        }

        int id = requestIdCounter.incrementAndGet();
        String dt;
        if (hasText(request.getLunarDatetime())) {
            dt = request.getLunarDatetime().replaceAll("[+-]\\d{2}:\\d{2}$", "").replace("T", " ");
            dt = dt.replaceAll("-(0)(\\d)", "-$2");   // 去前导零
            dt = "\"lunarDatetime\":\"" + dt + "\"";
        } else if (hasText(request.getSolarDatetime())) {
            dt = request.getSolarDatetime().replaceAll("[+-]\\d{2}:\\d{2}$", "").replace("T", " ");
            dt = "\"solarDatetime\":\"" + dt + "\"";
        } else {
            throw new McpApiException("solarDatetime 和 lunarDatetime 必须填一个");
        }

        return "{\"method\":\"tools/call\",\"params\":{\"name\":\"getBaziDetail\",\"arguments\":{\"eightCharProviderSect\":" + sect +
                ",\"gender\":" + gender + "," + dt + "},\"_meta\":{\"progressToken\":0}},\"jsonrpc\":\"2.0\",\"id\":" + id + "}";
    }

    /**
     * 会话初始化（含 initialized 通知）
     */
    private void initializeSessionIfNeeded() {
        String sessionId = mcpSessionId.get();
        if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
            log.debug("使用现有MCP会话: {}", sessionId);
            return;
        }
        // CAS机制确保只有一个线程执行初始化
        if (mcpSessionId.compareAndSet(null, INITIALIZING_MARKER)) {
            try {
                sessionId = createNewSession();
                mcpSessionId.set(sessionId);
                log.info("MCP会话初始化成功，sessionId: {}", sessionId);
                sendInitializedNotification(sessionId);
            } catch (Exception e) {
                mcpSessionId.set(null); // 初始化失败重置状态
                log.error("MCP会话初始化失败", e);
                throw new McpApiException("初始化MCP会话失败: " + e.getMessage(), e);
            }
        } else {
            // 等待其他线程初始化完成
            waitForSessionInitialization();
        }
    }
    /**
     * 创建新会话
     */
    private String createNewSession() {
        String initBody = buildInitRequestBody();
        log.debug("初始化MCP会话请求体: \n{}", initBody);

        return mcpWebClient.post()
                .headers(this::setInitRequestHeaders)
                .body(BodyInserters.fromValue(initBody))
                .exchangeToMono(this::extractSessionId)
                .retryWhen(buildRetrySpec("会话初始化"))
                .block();
    }

    /**
     * 提取会话ID
     */
    private Mono<String> extractSessionId(ClientResponse clientResponse) {
        if (clientResponse.statusCode().is2xxSuccessful()) {
            // 尝试从响应头获取sessionId
            String sessionId = clientResponse.headers().header("Mcp-Session-Id").stream()
                    .findFirst()
                    .orElseGet(() -> clientResponse.headers().header("Mcp-Session-ID").stream()
                            .findFirst()
                            .orElse(null));

            if (hasText(sessionId)) {
                return Mono.just(sessionId);
            }

            // 从响应体提取sessionId（兼容不同返回格式）
            return clientResponse.bodyToMono(String.class)
                    .flatMap(responseBody -> {
                        try {
                            Map<String, Object> responseMap = objectMapper.readValue(
                                    responseBody, new TypeReference<Map<String, Object>>() {});
                            Map<String, Object> result = (Map<String, Object>) responseMap.get("result");
                            String bodySessionId = (String) result.get("sessionId");

                            if (hasText(bodySessionId)) {
                                return Mono.just(bodySessionId);
                            } else {
                                return Mono.error(new McpApiException("响应中未包含有效sessionId"));
                            }
                        } catch (Exception e) {
                            return Mono.error(new McpApiException("解析会话ID失败: " + e.getMessage(), e));
                        }
                    });
        } else {
            return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(
                            new McpApiException("会话初始化失败，状态码: " + clientResponse.statusCode() + ", 错误信息: " + errorBody)
                    ));
        }
    }

    /**
     * 等待会话初始化完成
     */
    private void waitForSessionInitialization() {
        int maxWaitCount = 30;
        int waitCount = 0;
        while (true) {
            String sessionId = mcpSessionId.get();
            if (StringUtils.hasText(sessionId) && !INITIALIZING_MARKER.equals(sessionId)) {
                log.debug("获取到其他线程初始化的会话: {}", sessionId);
                return;
            }
            if (waitCount >= maxWaitCount) {
                throw new McpApiException("等待会话初始化超时");
            }
            try {
                Thread.sleep(100);
                waitCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new McpApiException("等待会话初始化被中断", e);
            }
        }
    }

    /**
     * 构建初始化请求体
     */
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
            clientInfo.put("name", getClientName());
            clientInfo.put("version", "1.0.0");
            params.put("clientInfo", clientInfo);

            request.put("params", params);
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new McpApiException("构建初始化请求体失败", e);
        }
    }

    /**
     * 设置初始化请求头
     */
    private void setInitRequestHeaders(HttpHeaders headers) {
        setBaseHeaders(headers);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, "application/json");
        headers.set("x-api-key", apiKey);
    }

    /**
     * 发送初始化完成通知
     */
    private void sendInitializedNotification(String sessionId) {
        String body = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{}}";
        log.debug("发送initialized通知请求体: \n{}", body);

        try {
            mcpWebClient.post()
                    .headers(headers -> {
                        setBaseHeaders(headers);
                        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                        headers.set("mcp-session-id", sessionId);
                    })
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(buildRetrySpec("发送初始化通知"))
                    .block();
            log.debug("initialized通知发送成功");
        } catch (Exception e) {
            log.warn("initialized通知发送异常: {}", e.getMessage());
        }
    }

    /**
     * 设置通用请求头
     */
    private void setCommonRequestHeaders(HttpHeaders headers, String sessionId) {
        setBaseHeaders(headers);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, "application/json, text/event-stream");
        headers.set("mcp-session-id", sessionId);
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    }

    /**
     * 构建重试策略
     */
    private Retry buildRetrySpec(String operation) {
        return Retry.fixedDelay(MAX_RETRY, Duration.ofSeconds(RETRY_INTERVAL_SECONDS))
                .filter(ex -> ex instanceof WebClientResponseException responseEx &&
                        (responseEx.getStatusCode().is5xxServerError() ||
                                responseEx.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS))
                .doBeforeRetry(signal -> log.warn("{}失败，开始第{}次重试", operation, signal.totalRetries() + 1))
                .onRetryExhaustedThrow((spec, signal) -> {
                    Throwable cause = signal.failure();
                    return new McpApiException(String.format("%s重试%d次后仍失败", operation, MAX_RETRY), cause);
                });
    }

    /**
     * 基础请求头设置
     */
    private void setBaseHeaders(HttpHeaders headers) {
        headers.set("Origin", ORIGIN);
        headers.set("Referer", REFERER);
        headers.set("User-Agent", USER_AGENT);
        headers.set("mcp-protocol-version", MCP_PROTOCOL_VERSION);
    }
    /**
     * 参数校验
     */
    private void validateBaziRequest(McpBaziRequest request) {
        if (request == null) {
            throw new McpApiException("请求参数不能为空");
        }
        boolean hasSolar = hasText(request.getSolarDatetime());
        boolean hasLunar = hasText(request.getLunarDatetime());
        if (!hasSolar && !hasLunar) {
            throw new McpApiException("solarDatetime 和 lunarDatetime 必须填一个");
        }
        if (hasSolar && hasLunar) {
            throw new McpApiException("solarDatetime 和 lunarDatetime 不能同时填写");
        }
        if (request.getGender() != null && (request.getGender() < 0 || request.getGender() > 1)) {
            throw new McpApiException("gender 必须为 0(女) 或 1(男)");
        }
        if (request.getEightCharProviderSect() != null &&
                (request.getEightCharProviderSect() < 1 || request.getEightCharProviderSect() > 2)) {
            throw new McpApiException("eightCharProviderSect 必须为 1 或 2");
        }
    }
    /**
     * 工具方法：判断字符串是否有内容
     */
    private boolean hasText(String str) {
        return StringUtils.hasText(str);
    }
}