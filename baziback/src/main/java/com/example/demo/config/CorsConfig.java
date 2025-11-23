package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;
import org.springframework.web.cors.reactive.CorsWebFilter;

@Configuration
public class CorsConfig {
    @Bean
    public WebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 允许的前端源（开发环境常见端口）
        // 允许 file:// 协议（null origin）
        config.addAllowedOrigin("null");  // 允许 file:// 协议直接打开HTML文件
        
        // 推荐使用 addAllowedOriginPattern 而非 addAllowedOrigin（Spring 2.4+ 更推荐，支持通配符且更规范）
        // 本地开发环境 - localhost
        config.addAllowedOriginPattern("http://localhost:*");        // 所有 localhost 端口
        config.addAllowedOriginPattern("http://127.0.0.1:*");       // 所有 127.0.0.1 端口
        
        // 常见开发端口（保留具体配置以便调试）
        config.addAllowedOriginPattern("http://localhost:63342");    // WebStorm
        config.addAllowedOriginPattern("http://localhost:5173");     // Vite
        config.addAllowedOriginPattern("http://localhost:3000");     // React
        config.addAllowedOriginPattern("http://localhost:8000");     // Python HTTP Server
        config.addAllowedOriginPattern("http://localhost:8081");     // 自定义前端端口
        config.addAllowedOriginPattern("http://localhost:8080");     // 前端可能占用的端口
        config.addAllowedOriginPattern("http://127.0.0.1:63342");
        config.addAllowedOriginPattern("http://127.0.0.1:5173");
        config.addAllowedOriginPattern("http://127.0.0.1:3000");
        config.addAllowedOriginPattern("http://127.0.0.1:8000");
        config.addAllowedOriginPattern("http://127.0.0.1:8081");
        config.addAllowedOriginPattern("http://127.0.0.1:8080");
        
        // 生产环境 - 允许HTTPS域名
        config.addAllowedOriginPattern("https://lldd.click");        // 生产环境主域名
        config.addAllowedOriginPattern("https://www.lldd.click");      // 生产环境www子域名
        config.addAllowedOriginPattern("https://*.lldd.click");       // 允许所有lldd.click的子域名

        // 2. 允许的请求方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");

        // 3. 允许的请求头（如Content-Type、Authorization等）
        config.addAllowedHeader("*");

        // 4. 允许携带凭证（Cookie、Token等），与前端 credentials: 'include' 配合
        // 注意：当允许 null origin 时，某些浏览器可能不支持 credentials
        config.setAllowCredentials(true);

        // 5. 预检请求（OPTIONS）的缓存时间（1小时），减少重复预检
        config.setMaxAge(3600L);

        // 6. 对所有接口生效（/** 表示匹配所有路径）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
