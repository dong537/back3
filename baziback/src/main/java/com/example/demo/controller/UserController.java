package com.example.demo.controller;

import com.example.demo.dto.request.user.LoginRequest;
import com.example.demo.dto.request.user.RegisterRequest;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@Slf4j
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     * POST http://localhost:8080/api/user/register
     */                              
    @PostMapping("/register")
    public Map<String, Object> register(@Validated @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}", request.getUsername());
        return userService.register(request);
    }
    /**
     * 用户登录
     * POST http://localhost:8080/api/user/login
     */
    @PostMapping("/login")
    public Map<String, Object> login(@Validated @RequestBody LoginRequest request,
                                     ServerHttpRequest serverHttpRequest) {
        String ip = getClientIP(serverHttpRequest);
        log.info("收到登录请求: username={}, ip={}", request.getUsername(), ip);
        return userService.login(request, ip);
    }

    /**
     * 获取用户信息
     * GET http://localhost:8080/api/user/info
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.getUserInfo(token);
    }
    /**
     * 获取客户端IP地址
     */
    private String getClientIP(ServerHttpRequest request) {
        if (request == null) {
            return "unknown";
        }
        HttpHeaders headers = request.getHeaders();
        String forwardedIp = headers.getFirst("X-Forwarded-For");
        if (forwardedIp != null) {
            String trimmed = forwardedIp.trim();
            if (!trimmed.isEmpty()) {
                int commaIndex = trimmed.indexOf(',');
                return commaIndex > 0 ? trimmed.substring(0, commaIndex).trim() : trimmed;
            }
        }
        String realIp = headers.getFirst("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        var remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }
}
