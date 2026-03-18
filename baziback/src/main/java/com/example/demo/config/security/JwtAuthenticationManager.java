package com.example.demo.config.security;

import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * 验证 JWT 的有效性，并在成功后返回一个已认证的 Authentication 对象。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .cast(JwtAuthenticationToken.class)
                .filter(jwtAuth -> jwtUtil.validateToken((String) jwtAuth.getCredentials()))
                .map(jwtAuth -> {
                    String token = (String) jwtAuth.getCredentials();
                    Long userId = jwtUtil.extractUserId(token);
                    String username = jwtUtil.extractUsername(token);
                    // 你可以在这里根据 userId 或 username 从数据库加载用户的角色/权限
                    // 为简化，我们暂时只授予一个默认的 'ROLE_USER' 权限
                    return new JwtAuthenticationToken(
                            userId,
                            username,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                });
    }
}

