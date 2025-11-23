package com.example.demo.interceptor;

import com.example.demo.annotation.RequireAuth;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    
    private final UserMapper userMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查方法上是否有@RequireAuth注解
        RequireAuth methodAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
        // 检查类上是否有@RequireAuth注解
        RequireAuth classAuth = handlerMethod.getBeanType().getAnnotation(RequireAuth.class);
        
        // 如果方法和类上都没有@RequireAuth注解，直接放行
        if (methodAuth == null && classAuth == null) {
            return true;
        }
        
        // 如果方法上有注解且required=false，直接放行
        if (methodAuth != null && !methodAuth.required()) {
            return true;
        }
        
        // 需要认证，从请求头获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.isEmpty()) {
            log.warn("未提供认证token: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
            return false;
        }
        
        // 解析token获取userId
        Long userId = parseToken(token);
        if (userId == null) {
            log.warn("无效的token: {}", token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token无效\",\"data\":null}");
            return false;
        }
        
        // 验证用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"用户不存在\",\"data\":null}");
            return false;
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: userId={}", userId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"账号已被禁用\",\"data\":null}");
            return false;
        }
        
        // 将用户ID存入request属性，供后续使用
        request.setAttribute("userId", userId);
        request.setAttribute("username", user.getUsername());
        
        log.debug("认证通过: userId={}, username={}, uri={}", userId, user.getUsername(), request.getRequestURI());
        return true;
    }
    
    /**
     * 解析token获取userId（简化版，与UserService中的方法一致）
     */
    private Long parseToken(String token) {
        try {
            if (token != null && token.startsWith("CT_")) {
                String[] parts = token.split("_");
                if (parts.length >= 2) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (Exception e) {
            log.warn("解析token失败: {}", token);
        }
        return null;
    }
}
