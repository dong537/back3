package com.example.demo.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// 公共MCP配置父类
public abstract class BaseMcpConfig {

    // 公共日志过滤器：打印请求信息
    protected ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("MCP请求: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value ->
                    System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }

    // 公共日志过滤器：打印响应状态
    protected ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("MCP响应状态: " + clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    /**
     * 全局共用的 ObjectMapper（标记 @Primary 确保唯一）
     * 核心改造：注册 JavaTimeModule，支持 LocalDate
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 Java 8 时间模块（处理 LocalDate/LocalDateTime）
        mapper.registerModule(new JavaTimeModule());

        // 禁止将日期序列化为时间戳（保持 yyyy-MM-dd 格式）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
        return mapper;
    }

    /**
     * 创建支持 LocalDate 的 ExchangeStrategies
     * 所有 WebClient 必须使用此策略
     */
    protected ExchangeStrategies exchangeStrategies(ObjectMapper objectMapper) {
        return ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper)
                    );
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper)
                    );
                })
                .build();
    }

    /**
     * 模板方法：一键创建配置完整的 WebClient.Builder
     * 子类直接调用此方法，无需重复配置
     */
    protected WebClient.Builder createWebClientBuilder(
            String baseUrl,
            String apiKey,
            ObjectMapper objectMapper) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(exchangeStrategies(objectMapper))  // 关键：应用日期支持策略
                .filter(logRequest())
                .filter(logResponse());
    }
}