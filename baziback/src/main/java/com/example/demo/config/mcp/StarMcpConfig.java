package com.example.demo.config.mcp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
public class StarMcpConfig extends BaseMcpConfig {


    @Value("${mcp.star.api.endpoint}")
    private String starEndpoint;

    @Value("${mcp.star.api.api-key}")
    private String starApiKey;

    @Bean
    @Qualifier("starWebClient")
    public WebClient starWebClient(org.springframework.context.ApplicationContext ctx) {
        // 复用 BaseMcpConfig 中已经配置了 JavaTimeModule 的 ObjectMapper + ExchangeStrategies
        com.fasterxml.jackson.databind.ObjectMapper mapper = ctx.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);

        return createWebClientBuilder(starEndpoint, starApiKey, mapper)
                .build();
    }
}
