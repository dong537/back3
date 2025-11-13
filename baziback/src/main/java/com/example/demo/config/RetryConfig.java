package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;


/**
 * 重试配置：应对网络波动和API临时不可用
 */
@Configuration
@EnableRetry // 启用Spring重试机制
public class RetryConfig {

    @Bean
    public RetryTemplate deepSeekRetryTemplate() {
        // 1. 重试策略：最多重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        // 2. 退避策略：每次重试间隔1秒（避免触发限流）
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000); // 单位：毫秒

        // 3. 组装重试模板
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}