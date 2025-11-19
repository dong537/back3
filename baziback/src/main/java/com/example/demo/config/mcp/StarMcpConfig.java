package com.example.demo.config.mcp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class StarMcpConfig extends BaseMcpConfig {


    @Value("${mcp.star.api.endpoint}")
    private String starEndpoint;

    @Value("${mcp.star.api.api-key}")
    private String starApiKey;

    @Bean
    @Qualifier("starWebClient")
    public WebClient starWebClient() {
        return WebClient.builder()
                .baseUrl(starEndpoint)
                .defaultHeader("x-api-key", starApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())  // 复用父类日志过滤器
                .filter(logResponse())
                .build();
    }
}
