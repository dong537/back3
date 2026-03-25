package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterService {

    private final Map<Long, Map<String, Sinks.Many<ServerSentEvent<Map<String, Object>>>>> userSinks =
            new ConcurrentHashMap<>();

    public Flux<ServerSentEvent<Map<String, Object>>> subscribe(Long userId, String channel) {
        String safeChannel = normalizeChannel(channel);
        Map<String, Sinks.Many<ServerSentEvent<Map<String, Object>>>> channelSinks =
                userSinks.computeIfAbsent(userId, key -> new ConcurrentHashMap<>());

        log.info("user {} subscribed to SSE channel {}, active channel count={}", userId, safeChannel, channelSinks.size());

        Sinks.Many<ServerSentEvent<Map<String, Object>>> sink =
                Sinks.many().multicast().onBackpressureBuffer();

        Sinks.Many<ServerSentEvent<Map<String, Object>>> oldSink = channelSinks.put(safeChannel, sink);
        if (oldSink != null) {
            log.info("closing previous SSE channel {} for user {}", safeChannel, userId);
            oldSink.tryEmitComplete();
        }

        Map<String, Object> initData = new HashMap<>();
        initData.put("message", "SSE connection established");
        initData.put("userId", userId);
        initData.put("channel", safeChannel);

        sink.tryEmitNext(ServerSentEvent.<Map<String, Object>>builder()
                .event("INIT")
                .data(initData)
                .build());

        return sink.asFlux().doFinally(signalType -> {
            Map<String, Sinks.Many<ServerSentEvent<Map<String, Object>>>> current = userSinks.get(userId);
            if (current != null) {
                current.remove(safeChannel, sink);
                if (current.isEmpty()) {
                    userSinks.remove(userId);
                }
            }
            int remaining = current == null ? 0 : current.size();
            log.info("SSE channel {} for user {} closed, signal={}, remaining channels={}", safeChannel, userId, signalType, remaining);
        });
    }

    public void sendToUser(Long userId, String eventName, Map<String, Object> data) {
        Map<String, Sinks.Many<ServerSentEvent<Map<String, Object>>>> channelSinks = userSinks.get(userId);
        if (channelSinks == null || channelSinks.isEmpty()) {
            log.warn("no active SSE connections for user {}, skipped event {}", userId, eventName);
            return;
        }

        ServerSentEvent<Map<String, Object>> event = ServerSentEvent.<Map<String, Object>>builder()
                .event(eventName)
                .data(data)
                .build();

        channelSinks.forEach((channel, sink) -> {
            Sinks.EmitResult result = sink.tryEmitNext(event);
            if (result.isFailure()) {
                log.warn("failed to emit SSE event {} to user {} channel {}: {}", eventName, userId, channel, result);
            }
        });
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return "default";
        }
        return channel.trim().toLowerCase();
    }
}
