package com.example.demo.config.mcp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class YijingMcpConfig extends BaseMcpConfig {

    @Value("${mcp.yijing.api.endpoint}")
    private String yijingEndpoint;

    @Value("${mcp.yijing.api.api-key}")
    private String yijingApiKey;

    @Bean
    @Qualifier("yijingWebClient")
    public WebClient yijingWebClient() {
        return WebClient.builder()
                .baseUrl(yijingEndpoint)
                .defaultHeader("x-api-key", yijingApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/event-stream")
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }
}

