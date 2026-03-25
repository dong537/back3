package com.example.demo.config.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 从请求头中提取 JWT，并将其转换为一个待处理的认证对象。
 */
@Component
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String token = extractToken(exchange);
        if (token == null || token.isBlank()) {
            return Mono.empty();
        }
        return Mono.just((Authentication) new JwtAuthenticationToken(token));
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        String path = exchange.getRequest().getPath().value();
        if (path.endsWith("/sse")) {
            String queryToken = exchange.getRequest().getQueryParams().getFirst("token");
            if (queryToken != null && !queryToken.isBlank()) {
                return queryToken;
            }
        }

        return null;
    }
}
