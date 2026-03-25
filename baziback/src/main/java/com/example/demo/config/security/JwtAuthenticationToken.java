package com.example.demo.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private Long userId;
    private String username;

    // 用于认证前
    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        setAuthenticated(false);
    }

    // 用于认证后
    public JwtAuthenticationToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = null;
        this.userId = userId;
        this.username = username;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.token;
    }

    @Override
    public Object getPrincipal() {
        return this.username;
    }

    public Long getUserId() {
        return this.userId;
    }
}

