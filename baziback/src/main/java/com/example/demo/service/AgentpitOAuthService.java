package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentpitOAuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Value("${agentpit.oauth.client-id}")
    private String clientId;

    @Value("${agentpit.oauth.client-secret}")
    private String clientSecret;

    @Value("${agentpit.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${agentpit.oauth.authorize-url}")
    private String authorizeUrl;

    @Value("${agentpit.oauth.token-url}")
    private String tokenUrl;

    @Value("${agentpit.oauth.userinfo-url}")
    private String userinfoUrl;

    @Value("${agentpit.oauth.scope:profile}")
    private String scope;

    private final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    @Data
    public static class OAuthResult {
        private boolean success;
        private String message;
        private String userJson;
        private String token;

        public static OAuthResult ok(String userJson, String token) {
            OAuthResult r = new OAuthResult();
            r.success = true;
            r.userJson = userJson;
            r.token = token;
            return r;
        }

        public static OAuthResult fail(String message) {
            OAuthResult r = new OAuthResult();
            r.success = false;
            r.message = message;
            return r;
        }
    }

    /**
     * 构建 AgentPit 授权 URL
     */
    public String buildAuthorizeUrl() {
        return buildAuthorizeUrl(null);
    }

    /**
     * 构建 AgentPit 授权 URL，支持 state 参数
     */
    public String buildAuthorizeUrl(String state) {
        return buildAuthorizeUrl(state, null);
    }

    /**
     * 构建 AgentPit 授权 URL，支持 state 和 prompt 参数
     * @param prompt "none" 表示静默授权（不弹确认页），null 表示默认行为
     */
    public String buildAuthorizeUrl(String state, String prompt) {
        String url = authorizeUrl
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
        if (state != null && !state.isEmpty()) {
            url += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        }
        if (prompt != null && !prompt.isEmpty()) {
            url += "&prompt=" + URLEncoder.encode(prompt, StandardCharsets.UTF_8);
        }
        return url;
    }

    /**
     * 全响应式处理 OAuth2 回调：code → access_token → 用户信息 → 本地用户 → JWT
     */
    public Mono<OAuthResult> handleCallback(String code) {
        return exchangeCodeForToken(code)
                .flatMap(accessToken -> fetchUserInfo(accessToken)
                        .flatMap(userInfo -> Mono.fromCallable(() -> findOrCreateUser(userInfo))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(user -> Mono.fromCallable(() -> {
                                    String jwt = jwtUtil.generateToken(user.getId(), user.getUsername());
                                    Map<String, Object> userVO = buildUserVO(user);
                                    String userJson = objectMapper.writeValueAsString(userVO);
                                    log.info("AgentPit OAuth 登录成功: userId={}, username={}", user.getId(), user.getUsername());
                                    return OAuthResult.ok(userJson, jwt);
                                }).subscribeOn(Schedulers.boundedElastic()))
                        )
                )
                .onErrorResume(e -> {
                    log.error("AgentPit OAuth 回调处理异常", e);
                    return Mono.just(OAuthResult.fail("服务器内部错误，请稍后重试"));
                });
    }

    /**
     * 用 code 换取 access_token（响应式，无 block）
     */
    private Mono<String> exchangeCodeForToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    log.debug("AgentPit token response: {}", response);
                    try {
                        JsonNode json = objectMapper.readTree(response);
                        String token = null;
                        if (json.has("access_token")) token = json.get("access_token").asText();
                        else if (json.has("data") && json.get("data").has("access_token"))
                            token = json.get("data").get("access_token").asText();
                        else if (json.has("token")) token = json.get("token").asText();

                        if (token != null && !token.isEmpty()) {
                            return Mono.just(token);
                        }
                        log.error("AgentPit token response 未找到 access_token: {}", response);
                        return Mono.error(new RuntimeException("获取授权令牌失败，请重试"));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("解析 token 响应失败: " + e.getMessage()));
                    }
                })
                .onErrorMap(e -> !(e instanceof RuntimeException),
                        e -> new RuntimeException("请求 token 接口失败: " + e.getMessage()));
    }

    /**
     * 用 access_token 获取用户信息（响应式，无 block）
     */
    private Mono<Map<String, Object>> fetchUserInfo(String accessToken) {
        return webClient.get()
                .uri(userinfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    log.debug("AgentPit userinfo response: {}", response);
                    try {
                        JsonNode json = objectMapper.readTree(response);
                        JsonNode dataNode = json.has("data") ? json.get("data") : json;

                        Map<String, Object> result = new HashMap<>();
                        if (dataNode.has("id")) result.put("id", dataNode.get("id").asText());
                        else if (dataNode.has("userId")) result.put("id", dataNode.get("userId").asText());
                        else if (dataNode.has("sub")) result.put("id", dataNode.get("sub").asText());

                        if (dataNode.has("name")) result.put("name", dataNode.get("name").asText());
                        else if (dataNode.has("username")) result.put("name", dataNode.get("username").asText());
                        else if (dataNode.has("nickname")) result.put("name", dataNode.get("nickname").asText());

                        if (dataNode.has("email")) result.put("email", dataNode.get("email").asText());
                        if (dataNode.has("image")) result.put("avatar", dataNode.get("image").asText());
                        else if (dataNode.has("avatar")) result.put("avatar", dataNode.get("avatar").asText());

                        if (!result.containsKey("id")) {
                            return Mono.error(new RuntimeException("获取用户信息失败，响应中无用户ID"));
                        }
                        return Mono.just(result);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("解析用户信息失败: " + e.getMessage()));
                    }
                })
                .onErrorMap(e -> !(e instanceof RuntimeException),
                        e -> new RuntimeException("请求用户信息接口失败: " + e.getMessage()));
    }

    /**
     * 查找或创建本地用户（阻塞 DB 操作，需在 boundedElastic 线程池中执行）
     */
    private User findOrCreateUser(Map<String, Object> userInfo) {
        String oauthId = String.valueOf(userInfo.get("id"));
        String name = userInfo.getOrDefault("name", "AgentPit用户").toString();
        String email = userInfo.containsKey("email") ? userInfo.get("email").toString() : null;
        String avatar = userInfo.containsKey("avatar") ? userInfo.get("avatar").toString() : null;

        User existing = userMapper.findByOauthId("agentpit", oauthId);
        if (existing != null) {
            existing.setNickname(name);
            existing.setAvatar(avatar);
            existing.setLastLoginTime(LocalDateTime.now());
            userMapper.updateOauthUserInfo(existing);
            return existing;
        }

        String username = generateUniqueUsername(oauthId);
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(null);
        newUser.setEmail(email);
        newUser.setNickname(name);
        newUser.setAvatar(avatar);
        newUser.setStatus(1);
        newUser.setOauthProvider("agentpit");
        newUser.setOauthId(oauthId);

        int rows = userMapper.insertOauthUser(newUser);
        if (rows <= 0) {
            throw new RuntimeException("创建用户失败，oauthId=" + oauthId);
        }
        log.info("AgentPit OAuth: 创建新用户 id={}, username={}", newUser.getId(), newUser.getUsername());
        return newUser;
    }

    private String generateUniqueUsername(String oauthId) {
        String shortId = oauthId.length() > 8 ? oauthId.substring(0, 8) : oauthId;
        String base = "ap_" + shortId;
        if (userMapper.findByUsername(base) == null) return base;
        return "ap_" + shortId + "_" + UUID.randomUUID().toString().substring(0, 4);
    }

    private Map<String, Object> buildUserVO(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("username", user.getUsername() != null ? user.getUsername() : "");
        vo.put("email", user.getEmail() != null ? user.getEmail() : "");
        vo.put("phone", "");
        vo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        vo.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        vo.put("currentPoints", 0);
        vo.put("totalPoints", 0);
        return vo;
    }

    @Data
    public static class AgentpitProfile {
        private String id;
        private String name;
        private String email;
        private String avatar;
    }
}
