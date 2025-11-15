package com.example.demo.config.mcp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BaziMcpConfig extends BaseMcpConfig {

    @Value("${mcp.bazi.api.endpoint}")
    private String baziEndpoint;

    @Value("${mcp.bazi.api.api-key}")
    private String baziApiKey;

    // 八字专属WebClient，使用@Qualifier区分
    @Bean
    @Qualifier("baziWebClient")
    public WebClient baziWebClient() {
        return WebClient.builder()
                .baseUrl(baziEndpoint)
                .defaultHeader("x-api-key", baziApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())  // 复用父类日志过滤器
                .filter(logResponse())
                .build();
    }
}