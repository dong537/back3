package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentpitOAuthService {

    private static final String OAUTH_PROVIDER = "agentpit";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${agentpit.oauth.authorization-url:${agentpit.oauth.authorize-url:}}")
    private String authorizationUrl;

    @Value("${agentpit.oauth.token-url:}")
    private String tokenUrl;

    @Value("${agentpit.oauth.userinfo-url:}")
    private String userinfoUrl;

    @Value("${agentpit.oauth.client-id:}")
    private String clientId;

    @Value("${agentpit.oauth.client-secret:}")
    private String clientSecret;

    @Value("${agentpit.oauth.callback-url:${agentpit.oauth.redirect-uri:https://me.candaigo.com/api/auth/agentpit/callback}}")
    private String callbackUrl;

    @Value("${agentpit.oauth.scope:user.info}")
    private String scope;

    @Value("${agentpit.oauth.use-pkce:false}")
    private boolean usePkce;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public String buildAuthorizeUrl() {
        return buildAuthorizeUrl(null, null);
    }

    public String buildAuthorizeUrl(String state) {
        return buildAuthorizeUrl(state, null);
    }

    public String buildAuthorizeUrl(String state, String prompt) {
        return buildAuthorizationUrl(state, null, prompt);
    }

    public String buildAuthorizationUrl(String state, String verifier) {
        return buildAuthorizationUrl(state, verifier, null);
    }

    public String buildAuthorizationUrl(String state, String verifier, String prompt) {
        ensureCoreConfig();

        StringBuilder builder = new StringBuilder(authorizationUrl.trim());
        builder.append(builder.indexOf("?") >= 0 ? "&" : "?")
                .append("response_type=code")
                .append("&client_id=").append(urlEncode(clientId))
                .append("&redirect_uri=").append(urlEncode(callbackUrl))
                .append("&scope=").append(urlEncode(scope));

        if (StringUtils.hasText(state)) {
            builder.append("&state=").append(urlEncode(state));
        }
        if (StringUtils.hasText(prompt)) {
            builder.append("&prompt=").append(urlEncode(prompt));
        }
        if (usePkce && StringUtils.hasText(verifier)) {
            builder.append("&code_challenge=").append(urlEncode(createCodeChallenge(verifier)))
                    .append("&code_challenge_method=S256");
        }

        return builder.toString();
    }

    public Map<String, Object> handleCallback(String code, String verifier, String ip) {
        ensureCoreConfig();

        JsonNode tokenPayload = exchangeAuthorizationCode(code, verifier);
        JsonNode tokenData = unwrapDataNode(tokenPayload);
        String accessToken = firstNonBlank(
                extractString(tokenData, "access_token"),
                extractString(tokenData, "accessToken"),
                extractString(tokenData, "token")
        );

        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException("AgentPit OAuth did not return an access token");
        }

        JsonNode profilePayload = fetchProfilePayload(accessToken, tokenData);
        AgentpitProfile profile = mapProfile(profilePayload);
        User localUser = findOrCreateUser(profile);
        return userService.loginWithUser(localUser, ip);
    }

    public String randomValue(int size) {
        byte[] bytes = new byte[size];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void ensureCoreConfig() {
        if (!StringUtils.hasText(authorizationUrl)) {
            throw new BusinessException("AGENTPIT_OAUTH_AUTHORIZATION_URL is not configured");
        }
        if (!StringUtils.hasText(tokenUrl)) {
            throw new BusinessException("AGENTPIT_OAUTH_TOKEN_URL is not configured");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new BusinessException("AGENTPIT_OAUTH_CLIENT_ID is not configured");
        }
        if (!StringUtils.hasText(clientSecret)) {
            throw new BusinessException("AGENTPIT_OAUTH_CLIENT_SECRET is not configured");
        }
    }

    private JsonNode exchangeAuthorizationCode(String code, String verifier) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", code);
        form.put("redirect_uri", callbackUrl);
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);
        if (usePkce && StringUtils.hasText(verifier)) {
            form.put("code_verifier", verifier);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl.trim()))
                .header(HttpHeaders.ACCEPT, "application/json")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(buildFormBody(form)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("AgentPit token exchange failed: " + response.body());
            }
            return parseSuccessfulResponse(response.body(), "AgentPit token exchange");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AgentPit token exchange failed", ex);
            throw new BusinessException("AgentPit token exchange failed");
        }
    }

    private JsonNode fetchProfilePayload(String accessToken, JsonNode tokenPayload) {
        if (!StringUtils.hasText(userinfoUrl)) {
            return unwrapDataNode(tokenPayload);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(userinfoUrl.trim()))
                .header(HttpHeaders.ACCEPT, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("AgentPit userinfo request failed: " + response.body());
            }
            return parseSuccessfulResponse(response.body(), "AgentPit userinfo");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AgentPit userinfo request failed", ex);
            throw new BusinessException("AgentPit userinfo request failed");
        }
    }

    private AgentpitProfile mapProfile(JsonNode payload) {
        JsonNode profileNode = unwrapDataNode(payload);
        String providerId = firstNonBlank(
                extractString(profileNode, "sub"),
                extractString(profileNode, "id"),
                extractString(profileNode, "user_id"),
                extractString(profileNode, "userId"),
                extractString(profileNode, "uid")
        );
        String email = extractString(profileNode, "email");
        String nickname = firstNonBlank(
                extractString(profileNode, "name"),
                extractString(profileNode, "nickname"),
                extractString(profileNode, "preferred_username"),
                extractString(profileNode, "username"),
                extractString(profileNode, "login")
        );
        String usernameBase = firstNonBlank(
                extractString(profileNode, "preferred_username"),
                extractString(profileNode, "username"),
                extractString(profileNode, "login"),
                extractString(profileNode, "route"),
                emailLocalPart(email),
                providerId
        );
        String avatar = firstNonBlank(
                extractString(profileNode, "picture"),
                extractString(profileNode, "avatar_url"),
                extractString(profileNode, "avatar"),
                extractString(profileNode, "image")
        );

        if (!StringUtils.hasText(providerId) && !StringUtils.hasText(usernameBase) && !StringUtils.hasText(email)) {
            throw new BusinessException("AgentPit user profile is incomplete");
        }

        return new AgentpitProfile(providerId, usernameBase, nickname, email, avatar);
    }

    private User findOrCreateUser(AgentpitProfile profile) {
        if (StringUtils.hasText(profile.providerId())) {
            User oauthUser = userMapper.findByOauthId(OAUTH_PROVIDER, profile.providerId());
            if (oauthUser != null) {
                enrichExistingUser(oauthUser, profile);
                return userMapper.findById(oauthUser.getId());
            }
        }

        if (StringUtils.hasText(profile.email())) {
            User existingUser = userMapper.findByEmail(profile.email());
            if (existingUser != null) {
                enrichExistingUser(existingUser, profile);
                return userMapper.findById(existingUser.getId());
            }
        }

        User newUser = new User();
        String username = buildUniqueUsername(profile);

        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setEmail(trimToNull(profile.email()));
        newUser.setNickname(StringUtils.hasText(profile.nickname()) ? profile.nickname() : username);
        newUser.setAvatar(trimToNull(profile.avatar()));
        newUser.setStatus(1);
        newUser.setOauthProvider(StringUtils.hasText(profile.providerId()) ? OAUTH_PROVIDER : null);
        newUser.setOauthId(trimToNull(profile.providerId()));

        int rows = StringUtils.hasText(newUser.getOauthId())
                ? userMapper.insertOauthUser(newUser)
                : userMapper.insert(newUser);
        if (rows <= 0 || newUser.getId() == null) {
            throw new BusinessException("Failed to create the local AgentPit account");
        }

        log.info("Created local user for AgentPit OAuth: id={}, username={}", newUser.getId(), newUser.getUsername());
        return userMapper.findById(newUser.getId());
    }

    private void enrichExistingUser(User existingUser, AgentpitProfile profile) {
        boolean changed = false;

        if (StringUtils.hasText(profile.nickname()) && !profile.nickname().equals(existingUser.getNickname())) {
            existingUser.setNickname(profile.nickname());
            changed = true;
        }

        if (StringUtils.hasText(profile.avatar()) && !profile.avatar().equals(existingUser.getAvatar())) {
            existingUser.setAvatar(profile.avatar());
            changed = true;
        }

        if (StringUtils.hasText(profile.email()) && !profile.email().equals(existingUser.getEmail())) {
            existingUser.setEmail(profile.email());
            changed = true;
        }

        if (changed) {
            existingUser.setUpdateTime(LocalDateTime.now());
            userMapper.updateProfile(existingUser);
        }
    }

    private String buildUniqueUsername(AgentpitProfile profile) {
        String base = sanitizeUsername(firstNonBlank(
                emailLocalPart(profile.email()),
                profile.usernameBase(),
                profile.providerId(),
                "agentpit_user"
        ));

        if (!StringUtils.hasText(base)) {
            base = "agentpit_user";
        }

        String candidate = base;
        int suffix = 1;
        while (userMapper.findByUsername(candidate) != null) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private String sanitizeUsername(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }

        String normalized = raw.trim().toLowerCase()
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "")
                .replaceAll("_{2,}", "_");

        if (normalized.length() > 40) {
            normalized = normalized.substring(0, 40);
        }

        return normalized;
    }

    private String emailLocalPart(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "";
        }
        return email.substring(0, email.indexOf('@'));
    }

    private String createCodeChallenge(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(verifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create PKCE code challenge", ex);
        }
    }

    private String buildFormBody(Map<String, String> values) {
        return values.entrySet().stream()
                .filter(entry -> StringUtils.hasText(entry.getValue()))
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private JsonNode parseSuccessfulResponse(String body, String actionName) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (root == null || root.isNull()) {
            throw new BusinessException(actionName + " returned an empty response");
        }

        JsonNode codeNode = root.get("code");
        if (codeNode != null && !codeNode.isNull()) {
            int code = codeNode.asInt(-1);
            if (code != 0 && code != 200) {
                String message = firstNonBlank(
                        extractString(root, "message"),
                        extractString(root, "msg"),
                        actionName + " failed"
                );
                String subCode = extractString(root, "subCode");
                throw new BusinessException(StringUtils.hasText(subCode) ? message + " (" + subCode + ")" : message);
            }
        }

        return root;
    }

    private JsonNode unwrapDataNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return node;
        }

        JsonNode dataNode = node.get("data");
        if (dataNode != null && !dataNode.isNull()) {
            return dataNode;
        }
        return node;
    }

    private String extractString(JsonNode node, String field) {
        if (node == null || !StringUtils.hasText(field)) {
            return null;
        }
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText(null);
        return StringUtils.hasText(value) ? value : null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record AgentpitProfile(
            String providerId,
            String usernameBase,
            String nickname,
            String email,
            String avatar
    ) {
    }
}
