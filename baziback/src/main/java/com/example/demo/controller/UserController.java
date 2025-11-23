package com.example.demo.controller;

import com.example.demo.annotation.RequireAuth;
import com.example.demo.dto.request.user.LoginRequest;
import com.example.demo.dto.request.user.RegisterRequest;
import com.example.demo.dto.response.Result;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * POST http://localhost:8088/api/user/register
     */                              
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Validated @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}", request.getUsername());
        return userService.register(request);
    }
    
    /**
     * 用户登录
     * POST http://localhost:8088/api/user/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIP(httpRequest);
        log.info("收到登录请求: username={}, ip={}", request.getUsername(), ip);
        return userService.login(request, ip);
    }

    /**
     * 获取用户信息（需要登录）
     * GET http://localhost:8088/api/user/info
     */
    @GetMapping("/info")
    @RequireAuth
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        // 从拦截器设置的属性中获取userId
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户信息: userId={}", userId);
        return userService.getUserInfoById(userId);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
            return "unknown";
        }
    }
}
