package com.example.demo.config.security;

import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return Mono.empty();
        }

        String token = (String) jwtAuth.getCredentials();
        if (!jwtUtil.validateToken(token)) {
            return Mono.error(new BadCredentialsException("JWT is invalid or expired"));
        }

        Long userId = jwtUtil.extractUserId(token);
        String username = jwtUtil.extractUsername(token);
        return Mono.just(new JwtAuthenticationToken(
                userId,
                username,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }
}
