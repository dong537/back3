package com.example.demo.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
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

    // 全局共用的ObjectMapper（标记@Primary确保唯一）
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
        return mapper;
    }
}