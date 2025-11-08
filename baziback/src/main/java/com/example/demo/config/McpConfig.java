package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class McpConfig {

    // 紫微斗数MCP配置
    @Value("${mcp.ziwei.api.endpoint}")
    private String ziweiEndpoint;

    @Value("${mcp.ziwei.api.api-key}")
    private String ziweiApiKey;

    // 八字MCP配置
    @Value("${mcp.bazi.api.endpoint}")
    private String baziEndpoint;

    @Value("${mcp.bazi.api.api-key}")
    private String baziApiKey;

    // 八字专属WebClient，标记@Qualifier
    @Bean
    @Qualifier("baziWebClient")
    public WebClient baziWebClient() {
        return WebClient.builder()
                .baseUrl(baziEndpoint)
                .defaultHeader("x-api-key", baziApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    // 紫微斗数专属WebClient，标记@Qualifier
    @Bean
    @Qualifier("ziweiWebClient")
    public WebClient ziweiWebClient() {
        return WebClient.builder()
                .baseUrl(ziweiEndpoint)
                .defaultHeader("x-api-key", ziweiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
        return mapper;
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("MCP请求: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value ->
                    System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("MCP响应状态: " + clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}