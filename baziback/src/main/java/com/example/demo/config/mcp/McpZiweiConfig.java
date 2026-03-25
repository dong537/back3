package com.example.demo.config.mcp;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
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
    public WebClient ziweiWebClient(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return createWebClientBuilder(ziweiEndpoint, ziweiApiKey, objectMapper)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/event-stream")
                .build();
    }
}
