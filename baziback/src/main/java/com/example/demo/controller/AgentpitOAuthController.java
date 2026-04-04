package com.example.demo.controller;

import com.example.demo.service.AgentpitOAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/auth/agentpit")
@RequiredArgsConstructor
@CrossOrigin
public class AgentpitOAuthController {

    private static final String STATE_COOKIE = "agentpit_oauth_state";
    private static final String VERIFIER_COOKIE = "agentpit_oauth_verifier";
    private static final String RESULT_EVENT = "agentpit-oauth-result";

    private final AgentpitOAuthService agentpitOAuthService;
    private final ObjectMapper objectMapper;

    @GetMapping({"", "/"})
    public ResponseEntity<Void> authorize(ServerHttpRequest request, ServerHttpResponse response) {
        String state = agentpitOAuthService.randomValue(24);
        String verifier = agentpitOAuthService.randomValue(48);
        String authorizationUrl = agentpitOAuthService.buildAuthorizationUrl(state, verifier);

        response.addCookie(buildCookie(STATE_COOKIE, state, 600, request));
        response.addCookie(buildCookie(VERIFIER_COOKIE, verifier, 600, request));

        return ResponseEntity.status(302)
                .location(URI.create(authorizationUrl))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam(value = "code", required = false) String code,
                                           @RequestParam(value = "state", required = false) String state,
                                           @RequestParam(value = "error", required = false) String error,
                                           @RequestParam(value = "error_description", required = false) String errorDescription,
                                           ServerHttpRequest request,
                                           ServerHttpResponse response) {
        String expectedState = request.getCookies().getFirst(STATE_COOKIE) != null
                ? request.getCookies().getFirst(STATE_COOKIE).getValue()
                : null;
        String verifier = request.getCookies().getFirst(VERIFIER_COOKIE) != null
                ? request.getCookies().getFirst(VERIFIER_COOKIE).getValue()
                : null;

        response.addCookie(clearCookie(STATE_COOKIE, request));
        response.addCookie(clearCookie(VERIFIER_COOKIE, request));

        if (StringUtils.hasText(error)) {
            return buildPopupPage(false, null, errorDescription != null ? errorDescription : error);
        }

        if (!StringUtils.hasText(code) || !StringUtils.hasText(state) || !state.equals(expectedState)) {
            return buildPopupPage(false, null, "AgentPit 授权状态校验失败，请重试");
        }

        try {
            Map<String, Object> loginResult = agentpitOAuthService.handleCallback(code, verifier, getClientIP(request));
            return buildPopupPage(true, loginResult, null);
        } catch (Exception ex) {
            log.error("AgentPit OAuth callback failed", ex);
            return buildPopupPage(false, null, ex.getMessage() != null ? ex.getMessage() : "AgentPit 授权失败");
        }
    }

    private ResponseEntity<String> buildPopupPage(boolean success, Map<String, Object> data, String message) {
        try {
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("type", RESULT_EVENT);
            payload.put("success", success);
            payload.put("data", data);
            payload.put("message", message == null ? "" : message);
            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadBase64 = Base64.getEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String html = """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                      <meta charset="UTF-8" />
                      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                      <title>AgentPit 授权中</title>
                    </head>
                    <body style="font-family: sans-serif; display:flex; align-items:center; justify-content:center; min-height:100vh; background:#0f172a; color:#e2e8f0;">
                      <div style="text-align:center;">
                        <p id="status">正在同步授权结果...</p>
                      </div>
                      <script>
                        (function () {
                          var eventName = "%s";
                          var payload = JSON.parse(atob("%s"));
                          var status = document.getElementById("status");
                          if (status) {
                            status.textContent = payload.success ? "授权成功，正在返回登录页..." : (payload.message || "授权失败，请返回重试");
                          }
                          try {
                            localStorage.setItem(eventName, JSON.stringify(payload));
                            setTimeout(function () { localStorage.removeItem(eventName); }, 500);
                          } catch (e) {}
                          try {
                            if (window.opener && window.opener !== window) {
                              window.opener.postMessage(payload, window.location.origin);
                              window.close();
                              return;
                            }
                          } catch (e) {}
                          setTimeout(function () {
                            window.location.replace("/login");
                          }, 1200);
                        })();
                      </script>
                    </body>
                    </html>
                    """.formatted(RESULT_EVENT, payloadBase64);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(html);
        } catch (Exception ex) {
            log.error("Failed to build AgentPit OAuth popup page", ex);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("AgentPit OAuth callback failed");
        }
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds, ServerHttpRequest request) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearCookie(String name, ServerHttpRequest request) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    private boolean isSecureRequest(ServerHttpRequest request) {
        return request.getURI() != null && "https".equalsIgnoreCase(request.getURI().getScheme());
    }

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
        } catch (Exception ignored) {
        }
        return "unknown";
    }
}
