package com.example.demo.config.mcp;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class McpZiweiConfig extends BaseMcpConfig {

    @Value("${mcp.ziwei.api.endpoint}")
    private String ziweiEndpoint;

    @Value("${mcp.ziwei.api.api-key}")
    private String ziweiApiKey;

    /**
     * 紫微斗数专属WebClient，使用@Qualifier区分
     */
    @Bean
    @Qualifier("ziweiWebClient")
    public WebClient ziweiWebClient() {
        return WebClient.builder()
                .baseUrl(ziweiEndpoint)
                .defaultHeader("x-api-key", ziweiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/event-stream")
                .filter(logRequest())   // 复用父类日志过滤器
                .filter(logResponse())
                .build();
    }
}