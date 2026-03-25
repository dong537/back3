package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.request.user.LoginRequest;
import com.example.demo.dto.request.user.RegisterRequest;
import com.example.demo.service.UserService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器
 */
@RestController
@Slf4j
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final AuthUtil authUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}", request.getUsername());
        return Result.success(userService.register(request));
    }

    /**
     * 用户登录
     */
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public Result<?> login(@Validated @RequestBody LoginRequest request,
                           ServerHttpRequest serverHttpRequest) {
        String ip = getClientIP(serverHttpRequest);
        log.info("收到登录请求: username={}, ip={}", request.getUsername(), ip);
        return Result.success(userService.login(request, ip));
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(ServerHttpRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwardedIp = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedIp != null) {
            String trimmed = forwardedIp.trim();
            if (!trimmed.isEmpty()) {
                int commaIndex = trimmed.indexOf(',');
                return commaIndex > 0 ? trimmed.substring(0, commaIndex).trim() : trimmed;
            }
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        try {
            var remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null) {
                String hostString = remoteAddress.getHostString();
                return hostString != null ? hostString : "unknown";
            }
        } catch (Exception e) {
            // 忽略获取IP地址时的异常，返回默认值
        }
        return "unknown";
    }
}
