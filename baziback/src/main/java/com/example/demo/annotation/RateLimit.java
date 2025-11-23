package com.example.demo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 限流时间窗口（秒）
     */
    int timeWindow() default 60;
    
    /**
     * 时间窗口内最大请求次数
     */
    int maxCount() default 5;
    
    /**
     * 限流类型：IP或USER
     */
    LimitType limitType() default LimitType.USER;
    
    enum LimitType {
        /**
         * 基于IP限流
         */
        IP,
        /**
         * 基于用户限流
         */
        USER
    }
}
