package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.config.security.JwtAuthenticationConverter;
import com.example.demo.config.security.JwtAuthenticationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_API_PATTERNS = {
            "/api/user/login",
            "/api/user/register",
            "/api/gemini/probe/**",
            "/api/yijing/**",
            "/api/tarot/**",
            "/api/zodiac/**",
            "/api/bazi/**",
            "/api/daily-lucky/**",
            "/api/daily-fortune-detail/**",
            "/api/standalone/yijing/**",
            "/api/liuyao/**"
    };

    private static final List<String> PUBLIC_API_PREFIXES = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/gemini/probe/",
            "/api/yijing/",
            "/api/tarot/",
            "/api/zodiac/",
            "/api/bazi/",
            "/api/daily-lucky/",
            "/api/daily-fortune-detail/",
            "/api/standalone/yijing/",
            "/api/liuyao/"
    );

    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        jwtFilter.setServerAuthenticationConverter(jwtAuthenticationConverter);
        
        jwtFilter.setRequiresAuthenticationMatcher(exchange -> {
            String path = exchange.getRequest().getPath().value();
            if (isPublicApiPath(path)) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }
            if (path.startsWith("/api/")) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        });

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(noPopupAuthenticationEntryPoint())
                )
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/achievement/sse", "/api/credit/sse", "/api/deepseek/reasoning-stream").authenticated()
                        .pathMatchers(PUBLIC_API_PATTERNS).permitAll()
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private boolean isPublicApiPath(String path) {
        return PUBLIC_API_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(100))
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * 自定义认证入口点，返回401但不带WWW-Authenticate头，避免浏览器弹出登录框
     */
    @Bean
    public ServerAuthenticationEntryPoint noPopupAuthenticationEntryPoint() {
        return (exchange, ex) -> Mono.fromRunnable(() -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        });
    }
}
