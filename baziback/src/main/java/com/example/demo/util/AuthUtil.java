package com.example.demo.util;

import com.example.demo.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 统一鉴权工具：从 Authorization header 中解析 userId
 */
@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final JwtUtil jwtUtil;

    public Long requireUserId(String authorizationHeader) {
        Long userId = tryGetUserId(authorizationHeader);
        if (userId == null) {
            throw new UnauthorizedException("未登录或token无效");
        }
        return userId;
    }

    public Long tryGetUserId(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return null;
        }
        try {
            String token = authorizationHeader;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.extractUserId(token);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }
}
