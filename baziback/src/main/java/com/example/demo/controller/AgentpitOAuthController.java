package com.example.demo.controller;

import com.example.demo.service.AgentpitOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * AgentPit OAuth2 授权登录控制器
 * 支持两种模式：
 * 1. 弹窗模式：/api/auth/agentpit → 弹窗打开 → postMessage 通知主窗口
 * 2. SSO 重定向模式：/api/auth/agentpit/sso → 全页面重定向 → 回调后重定向回前端
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/agentpit")
@RequiredArgsConstructor
@CrossOrigin
public class AgentpitOAuthController {

    private final AgentpitOAuthService agentpitOAuthService;

    private static final String SSO_STATE_PREFIX = "sso:";

    /**
     * 弹窗入口：重定向到 AgentPit 授权页（保留原有弹窗模式）
     */
    @GetMapping("")
    public Mono<Void> authorize(ServerHttpResponse response) {
        String url = agentpitOAuthService.buildAuthorizeUrl();
        log.info("AgentPit OAuth 弹窗模式重定向到授权页: {}", url);
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(url));
        return response.setComplete();
    }

    /**
     * SSO 入口：全页面重定向到 AgentPit 授权页
     * 用户已在 AgentPit 登录时会自动跳回，无需用户交互
     */
    @GetMapping("/sso")
    public Mono<Void> ssoAuthorize(
            @RequestParam(required = false, defaultValue = "/") String returnUrl,
            ServerHttpResponse response) {
        // 将 returnUrl 编码到 state 中，回调时用于重定向
        String state = SSO_STATE_PREFIX + returnUrl;
        String url = agentpitOAuthService.buildAuthorizeUrl(state);
        log.info("AgentPit OAuth SSO 模式重定向到授权页: {}, returnUrl: {}", url, returnUrl);
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(url));
        return response.setComplete();
    }

    /**
     * OAuth2 回调：根据 state 参数区分弹窗模式和 SSO 模式
     */
    @GetMapping(value = "/callback", produces = "text/html;charset=UTF-8")
    public Mono<String> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            @RequestParam(required = false) String state,
            ServerHttpResponse response) {

        boolean isSsoMode = state != null && state.startsWith(SSO_STATE_PREFIX);

        if (error != null) {
            log.warn("AgentPit OAuth 回调错误: error={}, description={}", error, errorDescription);
            if (isSsoMode) {
                return ssoRedirect(response, null, null, "授权被拒绝: " + error, state);
            }
            return Mono.just(buildResultPage(false, "授权被拒绝: " + error, null, null));
        }

        if (code == null || code.isBlank()) {
            log.warn("AgentPit OAuth 回调未收到 code");
            if (isSsoMode) {
                return ssoRedirect(response, null, null, "未收到授权码", state);
            }
            return Mono.just(buildResultPage(false, "未收到授权码", null, null));
        }

        log.info("AgentPit OAuth 回调收到 code，模式: {}", isSsoMode ? "SSO" : "弹窗");

        return agentpitOAuthService.handleCallback(code)
                .flatMap(result -> {
                    if (isSsoMode) {
                        if (!result.isSuccess()) {
                            return ssoRedirect(response, null, null, result.getMessage(), state);
                        }
                        return ssoRedirect(response, result.getUserJson(), result.getToken(), null, state);
                    }
                    // 弹窗模式
                    if (!result.isSuccess()) {
                        return Mono.just(buildResultPage(false, result.getMessage(), null, null));
                    }
                    return Mono.just(buildResultPage(true, null, result.getUserJson(), result.getToken()));
                })
                .onErrorResume(e -> {
                    log.error("AgentPit OAuth callback 异常", e);
                    if (isSsoMode) {
                        return ssoRedirect(response, null, null, "服务器内部错误", state);
                    }
                    return Mono.just(buildResultPage(false, "服务器内部错误，请稍后重试", null, null));
                });
    }

    /**
     * SSO 模式：返回 HTML 页面，通过 JS 重定向到前端 SSO 回调页
     * 使用 JS 重定向而非 302，因为需要通过 URL hash 传递 token（hash 在 302 中不一定保留）
     */
    private Mono<String> ssoRedirect(ServerHttpResponse response, String userJson, String token, String errorMsg, String state) {
        String returnUrl = "/";
        if (state != null && state.startsWith(SSO_STATE_PREFIX)) {
            returnUrl = state.substring(SSO_STATE_PREFIX.length());
        }

        String redirectUrl;
        if (token != null && userJson != null) {
            String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            String encodedReturnUrl = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
            redirectUrl = "/auth/sso/callback#token=" + encodedToken
                    + "&user=" + encodedUser
                    + "&returnUrl=" + encodedReturnUrl;
        } else {
            String encodedError = URLEncoder.encode(
                    errorMsg != null ? errorMsg : "授权失败",
                    StandardCharsets.UTF_8
            );
            redirectUrl = "/login?sso_error=" + encodedError;
        }

        // 返回 HTML 页面通过 JS 跳转，确保 hash fragment 正确传递
        return Mono.just("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                "<p style='font-family:sans-serif;text-align:center;margin-top:40px;color:#666'>正在登录...</p>" +
                "<script>window.location.href='" + redirectUrl.replace("'", "\\'") + "';</script>" +
                "</body></html>");
    }

    /**
     * 生成弹窗关闭页：通过 postMessage 或 localStorage 把结果传回主窗口
     */
    private String buildResultPage(boolean success, String errorMsg, String userJson, String token) {
        String payload;
        if (success) {
            payload = String.format(
                "{\"type\":\"agentpit-oauth-result\",\"success\":true,\"data\":{\"user\":%s,\"token\":\"%s\"}}",
                userJson, token
            );
        } else {
            String safeMsg = (errorMsg != null ? errorMsg : "授权失败").replace("\"", "'");
            payload = String.format(
                "{\"type\":\"agentpit-oauth-result\",\"success\":false,\"message\":\"%s\"}",
                safeMsg
            );
        }

        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'>" +
               "<title>AgentPit 授权中</title></head><body>" +
               "<p style='font-family:sans-serif;text-align:center;margin-top:40px;color:#666'>正在同步授权结果...</p>" +
               "<script>" +
               "var payload = " + payload + ";" +
               "try {" +
               "  if (window.opener && !window.opener.closed) {" +
               "    window.opener.postMessage(payload, window.location.origin);" +
               "  } else {" +
               "    localStorage.setItem('agentpit-oauth-result', JSON.stringify(payload));" +
               "  }" +
               "} catch(e) {" +
               "  localStorage.setItem('agentpit-oauth-result', JSON.stringify(payload));" +
               "}" +
               "setTimeout(function(){ window.close(); }, 300);" +
               "</script></body></html>";
    }
}
