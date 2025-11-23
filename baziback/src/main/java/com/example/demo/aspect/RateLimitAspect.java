package com.example.demo.aspect;

import com.example.demo.annotation.RateLimit;
import com.example.demo.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流切面
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    
    /**
     * 存储限流计数器：key = limitKey, value = {count, timestamp}
     */
    private final Map<String, LimitInfo> limitMap = new ConcurrentHashMap<>();
    
    @Around("@annotation(com.example.demo.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        // 获取限流key
        String limitKey = getLimitKey(rateLimit, method);
        
        // 检查是否超过限流
        if (!tryAcquire(limitKey, rateLimit)) {
            log.warn("接口限流触发: {}, 限制: {}次/{}秒", limitKey, rateLimit.maxCount(), rateLimit.timeWindow());
            throw new RateLimitException("操作过于频繁，请稍后再试");
        }
        
        return joinPoint.proceed();
    }
    
    /**
     * 尝试获取访问许可
     */
    private boolean tryAcquire(String limitKey, RateLimit rateLimit) {
        long now = System.currentTimeMillis();
        int timeWindowMs = rateLimit.timeWindow() * 1000;
        
        limitMap.compute(limitKey, (key, limitInfo) -> {
            if (limitInfo == null) {
                // 首次访问
                return new LimitInfo(1, now);
            }
            
            // 检查时间窗口是否过期
            if (now - limitInfo.timestamp > timeWindowMs) {
                // 时间窗口过期，重置计数
                return new LimitInfo(1, now);
            }
            
            // 在时间窗口内，增加计数
            limitInfo.count.incrementAndGet();
            return limitInfo;
        });
        
        LimitInfo info = limitMap.get(limitKey);
        return info.count.get() <= rateLimit.maxCount();
    }
    
    /**
     * 获取限流key
     */
    private String getLimitKey(RateLimit rateLimit, Method method) {
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
        
        if (rateLimit.limitType() == RateLimit.LimitType.IP) {
            return methodName + ":" + getClientIP();
        } else {
            // 基于用户限流，从请求头获取userId
            Long userId = getCurrentUserId();
            return methodName + ":user:" + (userId != null ? userId : "anonymous");
        }
    }
    
    /**
     * 获取当前用户ID（从请求属性中获取，由拦截器设置）
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Object userId = attributes.getRequest().getAttribute("userId");
                if (userId != null) {
                    return Long.parseLong(userId.toString());
                }
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败", e);
        }
        return null;
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "unknown";
    }
    
    /**
     * 限流信息
     */
    private static class LimitInfo {
        AtomicInteger count;
        long timestamp;
        
        LimitInfo(int count, long timestamp) {
            this.count = new AtomicInteger(count);
            this.timestamp = timestamp;
        }
    }
}
