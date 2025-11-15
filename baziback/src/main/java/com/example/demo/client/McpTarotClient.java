package com.example.demo.client;

import com.example.demo.dto.request.tarot.*;
import com.example.demo.dto.response.tarot.*;
import com.example.demo.exception.McpApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class McpTarotClient {
    private static final String MCP_PROTOCOL_VERSION = "2025-11-15";
    private static final int MAX_RETRY = 3;
    private static final long RETRY_INTERVAL_SECONDS = 1;

    private final WebClient tarotWebClient;
    private final ObjectMapper objectMapper;
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    // 1. get_card_info
    public GetCardInfoResponse getCardInfo(GetCardInfoRequest request) {
        return callMcpMethod("get_card_info", request, new TypeReference<GetCardInfoResponse>() {});
    }

    // 2. list_all_cards
    public ListAllCardsResponse listAllCards(ListAllCardsRequest request) {
        return callMcpMethod("list_all_cards", request, new TypeReference<ListAllCardsResponse>() {});
    }

    // 3. perform_reading
    public PerformReadingResponse performReading(PerformReadingRequest request) {
        return callMcpMethod("perform_reading", request, new TypeReference<PerformReadingResponse>() {});
    }

    // 4. search_cards
    public SearchCardsResponse searchCards(SearchCardsRequest request) {
        return callMcpMethod("search_cards", request, new TypeReference<SearchCardsResponse>() {});
    }

    // 5. find_similar_cards
    public FindSimilarCardsResponse findSimilarCards(FindSimilarCardsRequest request) {
        return callMcpMethod("find_similar_cards", request, new TypeReference<FindSimilarCardsResponse>() {});
    }

    // 6. get_database_analytics
    public GetDatabaseAnalyticsResponse getDatabaseAnalytics(GetDatabaseAnalyticsRequest request) {
        return callMcpMethod("get_database_analytics", request, new TypeReference<GetDatabaseAnalyticsResponse>() {});
    }

    // 7. create_custom_spread
    public CreateCustomSpreadResponse createCustomSpread(CreateCustomSpreadRequest request) {
        return callMcpMethod("create_custom_spread", request, new TypeReference<CreateCustomSpreadResponse>() {});
    }

    // 8. get_random_cards
    public GetRandomCardsResponse getRandomCards(GetRandomCardsRequest request) {
        return callMcpMethod("get_random_cards", request, new TypeReference<GetRandomCardsResponse>() {});
    }

    // 通用MCP调用方法
    private <T, R> R callMcpMethod(String method, T request, TypeReference<R> responseType) {
        try {
            int requestId = requestIdCounter.incrementAndGet();
            // 替换为以下代码（通过序列化-反序列化实现DTO转Map）
            Map<String, Object> params = objectMapper.readValue(
                    objectMapper.writeValueAsString(request),
                    new TypeReference<Map<String, Object>>() {}
            );

            String body = String.format(
                    "{\"jsonrpc\":\"2.0\",\"id\":%d,\"method\":\"%s\",\"params\":%s}",
                    requestId,
                    method,
                    objectMapper.writeValueAsString(params)
            );

            log.debug("调用塔罗MCP方法[{}]，请求体：\n{}", method, body);

            return tarotWebClient.post()
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), res -> res.bodyToMono(String.class)
                            .flatMap(err -> Mono.error(new McpApiException("塔罗MCP调用失败: " + err))))
                    .bodyToFlux(String.class)
                    .take(1)
                    .single()
                    .map(sse -> parseSseResponse(sse, responseType))
                    .retryWhen(Retry.fixedDelay(MAX_RETRY, Duration.ofSeconds(RETRY_INTERVAL_SECONDS)))
                    .block();
        } catch (Exception e) {
            throw new McpApiException("塔罗MCP方法[" + method + "]调用失败", e);
        }
    }

    // 解析SSE响应
    private <R> R parseSseResponse(String sse, TypeReference<R> responseType) {
        try {
            String json = sse.startsWith("data:") ? sse.substring(5).trim() : sse.trim();
            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            if (root.containsKey("error")) {
                throw new McpApiException("塔罗MCP返回错误: " + root.get("error"));
            }

            Map<String, Object> result = (Map<String, Object>) root.get("result");
            return objectMapper.convertValue(result, responseType);
        } catch (Exception e) {
            throw new McpApiException("解析塔罗MCP响应失败", e);
        }
    }
}