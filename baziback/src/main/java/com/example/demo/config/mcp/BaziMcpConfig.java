package com.example.demo.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;  // ✅ 添加这个导入
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BaziMcpConfig extends BaseMcpConfig {

    @Value("${mcp.bazi.api.endpoint}")
    private String baziEndpoint;

    @Value("${mcp.bazi.api.api-key}")
    private String baziApiKey;

    // ✅ 使用 @Lazy 延迟注入，打破循环依赖
    private final ObjectMapper objectMapper;

    public BaziMcpConfig(@Lazy ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    @Qualifier("baziWebClient")
    public WebClient baziWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON)
                    );
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper)
                    );
                })
                .build();

        return WebClient.builder()
                .baseUrl(baziEndpoint)
                .defaultHeader("x-api-key", baziApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }
}