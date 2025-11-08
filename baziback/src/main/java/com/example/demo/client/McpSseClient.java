package com.example.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class McpSseClient {
    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public McpSseClient(@Value("${mcp.bazi.api.endpoint}") String baseUrl,
                        @Value("${mcp.bazi.api.api-key}") String apiKey) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, "text/event-stream") // ① SSE
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    /**
     * 发送一行 JSON-RPC 并取第一条结果
     */
    public Mono<String> call(String method, Object params) {
        return call(method, params, null);
    }
    
    /**
     * 发送一行 JSON-RPC 并取第一条结果，支持传递额外的请求头
     */
    public Mono<String> call(String method, Object params, Map<String, String> additionalHeaders) {
        return call(method, params, additionalHeaders, null);
    }
    
    /**
     * 发送一行 JSON-RPC 并取第一条结果，支持传递额外的请求头和自定义解析器
     */
    public Mono<String> call(String method, Object params, Map<String, String> additionalHeaders, java.util.function.Function<String, String> customExtractor) {
        String frame = """
                {"jsonrpc":"2.0","id":%d,"method":"%s","params":%s}
                """.formatted(System.nanoTime(), method, toJson(params));

        WebClient.RequestHeadersSpec<?> request = client.post()
                .bodyValue(frame);               // ② 整行发
                
        // 添加额外的请求头
        if (additionalHeaders != null) {
            additionalHeaders.forEach(request::header);
        }

        return request.retrieve()
                .bodyToFlux(String.class)       // ③ 收 SSE
                .take(1)
                .single()
                .map(customExtractor != null ? customExtractor : this::extractText);
    }
    
    /**
     * 列出所有可用的工具
     */
    public Mono<String> listTools(Map<String, String> additionalHeaders) {
        Map<String, Object> params = Map.of(); // tools/list通常不需要参数
        return call("tools/list", params, additionalHeaders, this::extractToolInfo);
    }
    
    /**
     * 获取工具信息
     */
    public Mono<String> getToolInfo(String toolName, Map<String, String> additionalHeaders) {
        Map<String, Object> params = Map.of("name", toolName);
        return call("tools/get", params, additionalHeaders, this::extractToolInfo);
    }
    
    /**
     * 提取工具信息
     */
    private String extractToolInfo(String json) {
        try {
            // 尝试解析为MCP响应格式
            Map<?, ?> body = mapper.readValue(json, Map.class);
            
            // 检查是否包含content字段（标准SSE格式）
            if (body.containsKey("content")) {
                List<?> content = (List<?>) body.get("content");
                return (String) ((Map<?, ?>) content.get(0)).get("text");
            }
            
            // 如果没有content字段，直接返回整个JSON字符串
            return json;
        } catch (Exception e) {
            log.error("解析工具信息失败: {}", json, e);
            return "解析工具信息失败: " + json;
        }
    }

    private String extractText(String json) {
        try {
            // 尝试解析为MCP响应格式
            Map<?, ?> body = mapper.readValue(json, Map.class);
            
            // 检查是否包含content字段（标准SSE格式）
            if (body.containsKey("content")) {
                List<?> content = (List<?>) body.get("content");
                return (String) ((Map<?, ?>) content.get(0)).get("text");
            }
            
            // 如果没有content字段，直接返回整个JSON字符串
            return json;
        } catch (Exception e) {
            log.error("解析失败: {}", json, e);
            return "解析失败: " + json;
        }
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}