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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
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
    private static final String SSO_STATE_PREFIX = "sso:";

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

    @GetMapping("/sso")
    public ResponseEntity<Void> ssoAuthorize(@RequestParam(defaultValue = "/") String returnUrl,
                                             ServerHttpRequest request,
                                             ServerHttpResponse response) {
        String safeReturnUrl = sanitizeReturnUrl(returnUrl);
        String state = SSO_STATE_PREFIX + encodeReturnUrl(safeReturnUrl);
        String verifier = agentpitOAuthService.randomValue(48);
        String authorizationUrl = agentpitOAuthService.buildAuthorizationUrl(state, verifier, "none");

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
        String expectedState = readCookie(request, STATE_COOKIE);
        String verifier = readCookie(request, VERIFIER_COOKIE);
        boolean isSsoMode = StringUtils.hasText(state) && state.startsWith(SSO_STATE_PREFIX);

        response.addCookie(clearCookie(STATE_COOKIE, request));
        response.addCookie(clearCookie(VERIFIER_COOKIE, request));

        if (StringUtils.hasText(error)) {
            String message = StringUtils.hasText(errorDescription) ? errorDescription : error;
            return isSsoMode
                    ? buildSsoRedirectPage(false, null, message, decodeReturnUrl(state))
                    : buildPopupPage(false, null, message);
        }

        if (!StringUtils.hasText(code) || !StringUtils.hasText(state) || !state.equals(expectedState)) {
            String message = "AgentPit OAuth state validation failed";
            return isSsoMode
                    ? buildSsoRedirectPage(false, null, message, decodeReturnUrl(state))
                    : buildPopupPage(false, null, message);
        }

        try {
            Map<String, Object> loginResult = agentpitOAuthService.handleCallback(code, verifier, getClientIp(request));
            return isSsoMode
                    ? buildSsoRedirectPage(true, loginResult, null, decodeReturnUrl(state))
                    : buildPopupPage(true, loginResult, null);
        } catch (Exception ex) {
            log.error("AgentPit OAuth callback failed", ex);
            String message = StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "AgentPit OAuth failed";
            return isSsoMode
                    ? buildSsoRedirectPage(false, null, message, decodeReturnUrl(state))
                    : buildPopupPage(false, null, message);
        }
    }

    private ResponseEntity<String> buildPopupPage(boolean success, Map<String, Object> data, String message) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", RESULT_EVENT);
            payload.put("success", success);
            payload.put("data", data);
            payload.put("message", message == null ? "" : message);

            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadBase64 = Base64.getEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String html = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                      <meta charset="UTF-8" />
                      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                      <title>AgentPit OAuth</title>
                    </head>
                    <body style="font-family:sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;background:#0f172a;color:#e2e8f0;">
                      <div style="text-align:center;">
                        <p id="status">Syncing login result...</p>
                      </div>
                      <script>
                        (function () {
                          var eventName = "%s";
                          var payload = JSON.parse(atob("%s"));
                          var status = document.getElementById("status");
                          if (status) {
                            status.textContent = payload.success ? "Authorization complete. Returning..." : (payload.message || "Authorization failed.");
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

            return htmlResponse(html);
        } catch (Exception ex) {
            log.error("Failed to build AgentPit OAuth popup page", ex);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("AgentPit OAuth callback failed");
        }
    }

    private ResponseEntity<String> buildSsoRedirectPage(boolean success,
                                                        Map<String, Object> loginResult,
                                                        String message,
                                                        String returnUrl) {
        try {
            String safeReturnUrl = sanitizeReturnUrl(returnUrl);
            String redirectUrl;

            if (success && loginResult != null) {
                Object user = loginResult.get("user");
                String token = stringValue(loginResult.get("token"));
                if (user == null || !StringUtils.hasText(token)) {
                    redirectUrl = "/login?sso_error=" + urlEncode("AgentPit login payload is incomplete");
                } else {
                    String userJson = objectMapper.writeValueAsString(user);
                    redirectUrl = "/auth/sso/callback#token=" + urlEncode(token)
                            + "&user=" + urlEncode(userJson)
                            + "&returnUrl=" + urlEncode(safeReturnUrl);
                }
            } else {
                String safeMessage = StringUtils.hasText(message) ? message : "AgentPit authorization failed";
                redirectUrl = "/login?sso_error=" + urlEncode(safeMessage);
            }

            String html = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                      <meta charset="UTF-8" />
                      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                      <title>AgentPit SSO</title>
                    </head>
                    <body style="font-family:sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;background:#0f172a;color:#e2e8f0;">
                      <div style="text-align:center;">
                        <p>Completing sign-in...</p>
                      </div>
                      <script>
                        window.location.replace("%s");
                      </script>
                    </body>
                    </html>
                    """.formatted(redirectUrl.replace("\"", "%22"));

            return htmlResponse(html);
        } catch (Exception ex) {
            log.error("Failed to build AgentPit OAuth SSO redirect page", ex);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("AgentPit OAuth SSO failed");
        }
    }

    private ResponseEntity<String> htmlResponse(String html) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(html);
    }

    private String encodeReturnUrl(String returnUrl) {
        String safeReturnUrl = sanitizeReturnUrl(returnUrl);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(safeReturnUrl.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeReturnUrl(String state) {
        if (!StringUtils.hasText(state) || !state.startsWith(SSO_STATE_PREFIX)) {
            return "/";
        }

        try {
            String encoded = state.substring(SSO_STATE_PREFIX.length());
            if (!StringUtils.hasText(encoded)) {
                return "/";
            }
            return sanitizeReturnUrl(new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.warn("Failed to decode AgentPit SSO returnUrl from state: {}", ex.getMessage());
            return "/";
        }
    }

    private String sanitizeReturnUrl(String returnUrl) {
        if (!StringUtils.hasText(returnUrl)) {
            return "/";
        }

        String trimmed = returnUrl.trim();
        if (!trimmed.startsWith("/") || trimmed.startsWith("//")) {
            return "/";
        }
        return trimmed;
    }

    private String readCookie(ServerHttpRequest request, String cookieName) {
        if (request == null || !StringUtils.hasText(cookieName)) {
            return null;
        }
        return request.getCookies().getFirst(cookieName) != null
                ? request.getCookies().getFirst(cookieName).getValue()
                : null;
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
        return request != null
                && request.getURI() != null
                && "https".equalsIgnoreCase(request.getURI().getScheme());
    }

    private String getClientIp(ServerHttpRequest request) {
        if (request == null) {
            return "unknown";
        }

        String forwardedIp = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(forwardedIp)) {
            String trimmed = forwardedIp.trim();
            int commaIndex = trimmed.indexOf(',');
            return commaIndex > 0 ? trimmed.substring(0, commaIndex).trim() : trimmed;
        }

        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }

        try {
            if (request.getRemoteAddress() != null) {
                String hostString = request.getRemoteAddress().getHostString();
                return StringUtils.hasText(hostString) ? hostString : "unknown";
            }
        } catch (Exception ignored) {
        }

        return "unknown";
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
