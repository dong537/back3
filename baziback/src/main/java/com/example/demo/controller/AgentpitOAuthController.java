package com.example.demo.controller;

import com.example.demo.service.AgentpitOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * AgentPit OAuth2 弹窗授权登录控制器
 * 流程：弹窗打开 /api/auth/agentpit → 重定向到 AgentPit 授权页
 *      → 用户授权后回调 /api/auth/agentpit/callback
 *      → 返回 HTML 页面通过 postMessage 通知主窗口并关闭弹窗
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/agentpit")
@RequiredArgsConstructor
@CrossOrigin
public class AgentpitOAuthController {

    private final AgentpitOAuthService agentpitOAuthService;

    /**
     * 弹窗入口：重定向到 AgentPit 授权页
     */
    @GetMapping("")
    public Mono<Void> authorize(ServerHttpResponse response) {
        String url = agentpitOAuthService.buildAuthorizeUrl();
        log.info("AgentPit OAuth 重定向到授权页: {}", url);
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(url));
        return response.setComplete();
    }

    /**
     * OAuth2 回调：全响应式处理，不调用任何 block()
     */
    @GetMapping("/callback")
    public Mono<String> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription) {

        if (error != null) {
            log.warn("AgentPit OAuth 回调错误: error={}, description={}", error, errorDescription);
            return Mono.just(buildResultPage(false, "授权被拒绝: " + error, null, null));
        }

        if (code == null || code.isBlank()) {
            log.warn("AgentPit OAuth 回调未收到 code");
            return Mono.just(buildResultPage(false, "未收到授权码", null, null));
        }

        log.info("AgentPit OAuth 回调收到 code，开始处理...");

        return agentpitOAuthService.handleCallback(code)
                .map(result -> {
                    if (!result.isSuccess()) {
                        return buildResultPage(false, result.getMessage(), null, null);
                    }
                    return buildResultPage(true, null, result.getUserJson(), result.getToken());
                })
                .onErrorResume(e -> {
                    log.error("AgentPit OAuth callback 异常", e);
                    return Mono.just(buildResultPage(false, "服务器内部错误，请稍后重试", null, null));
                });
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
