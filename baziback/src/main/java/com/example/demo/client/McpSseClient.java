package com.example.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class McpSseClient {
    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public McpSseClient(@Value("${mcp.bazi.api.endpoint}") String baseUrl,
                        @Value("${mcp.bazi.api.api-key}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }
    /**
     * 发送一行 JSON-RPC 并取第一条结果
     */
    public String call(String method, Object params) throws IOException {
        return call(method, params, null);
    }
    
    /**
     * 发送一行 JSON-RPC 并取第一条结果，支持传递额外的请求头
     */
    public String call(String method, Object params, Map<String, String> additionalHeaders) throws IOException {
        return call(method, params, additionalHeaders, null);
    }
    
    /**
     * 发送一行 JSON-RPC 并取第一条结果，支持传递额外的请求头和自定义解析器
     */
    public String call(String method, Object params, Map<String, String> additionalHeaders, java.util.function.Function<String, String> customExtractor) throws IOException {
        String frame = """
                {"jsonrpc":"2.0","id":%d,"method":"%s","params":%s}
                """.formatted(System.nanoTime(), method, toJson(params));

        HttpPost request = new HttpPost(baseUrl);
        request.setEntity(new StringEntity(frame, ContentType.APPLICATION_JSON));
        request.addHeader("Accept", "text/event-stream");
        request.addHeader("x-api-key", apiKey);
        
        // 添加额外的请求头
        if (additionalHeaders != null) {
            additionalHeaders.forEach(request::addHeader);
        }

        @SuppressWarnings("deprecation")
        var response = httpClient.execute(request);
        try (response) {
            var entity = response.getEntity();
            if (entity != null) {
                String responseBody = new String(entity.getContent().readAllBytes());
                return customExtractor != null ? customExtractor.apply(responseBody) : extractText(responseBody);
            }
            return "No response body";
        }
    }
    
    /**
     * 列出所有可用的工具
     */
    public String listTools(Map<String, String> additionalHeaders) throws IOException {
        Map<String, Object> params = Map.of(); // tools/list通常不需要参数
        return call("tools/list", params, additionalHeaders, this::extractToolInfo);
    }
    
    /**
     * 获取工具信息
     */
    public String getToolInfo(String toolName, Map<String, String> additionalHeaders) throws IOException {
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