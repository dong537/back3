package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 WebFlux 的 SSE 连接管理服务
 */
@Service
@Slf4j
public class SseEmitterService {

    /**
     * 每个用户一个多播 Sink
     */
    private final Map<Long, Sinks.Many<ServerSentEvent<Map<String, Object>>>> userSinks = new ConcurrentHashMap<>();

    /**
     * 订阅用户 SSE 流
     */
    public Flux<ServerSentEvent<Map<String, Object>>> subscribe(Long userId) {
        log.info("用户 {} 订阅SSE流，当前活跃连接数: {}", userId, userSinks.size());
        
        // 为每次订阅创建新的 Sink，旧的完成掉
        Sinks.Many<ServerSentEvent<Map<String, Object>>> sink =
                Sinks.many().multicast().onBackpressureBuffer();

        Sinks.Many<ServerSentEvent<Map<String, Object>>> old = userSinks.put(userId, sink);
        if (old != null) {
            log.info("用户 {} 已有SSE连接，关闭旧连接", userId);
            old.tryEmitComplete();
        }

        // 发送一个初始化事件
        Map<String, Object> initData = new HashMap<>();
        initData.put("message", "SSE连接已建立");
        initData.put("userId", userId);
        
        Sinks.EmitResult initResult = sink.tryEmitNext(ServerSentEvent.<Map<String, Object>>builder()
                .event("INIT")
                .data(initData)
                .build());
        
        if (initResult.isSuccess()) {
            log.info("用户 {} SSE连接初始化成功", userId);
        } else {
            log.warn("用户 {} SSE连接初始化失败: {}", userId, initResult);
        }

        return sink.asFlux()
                .doFinally(signalType -> {
                    log.info("用户 {} SSE流结束，信号类型: {}，当前活跃连接数: {}", userId, signalType, userSinks.size() - 1);
                    userSinks.remove(userId);
                });
    }

    /**
     * 给指定用户发送事件
     */
    public void sendToUser(Long userId, String eventName, Map<String, Object> data) {
        Sinks.Many<ServerSentEvent<Map<String, Object>>> sink = userSinks.get(userId);
        if (sink == null) {
            log.warn("用户 {} 未建立SSE连接，无法发送事件 {}。当前活跃连接数: {}", userId, eventName, userSinks.size());
            return;
        }

        ServerSentEvent<Map<String, Object>> event = ServerSentEvent.<Map<String, Object>>builder()
                .event(eventName)
                .data(data)
                .build();

        Sinks.EmitResult result = sink.tryEmitNext(event);
        if (result.isFailure()) {
            log.warn("发送SSE事件 {} 给用户 {} 失败: {}", eventName, userId, result);
        } else {
            log.debug("成功发送SSE事件 {} 给用户 {}", eventName, userId);
        }
    }
}


