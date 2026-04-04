package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.net.URI;

final class GeminiSceneImageGatewaySupport {

    private static final String DEFAULT_PROVIDER = "one-api-chat-completions";
    private static final String GOOGLE_OPENAI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai";

    private final GeminiSceneImageSupport sceneImageSupport;
    private final String sceneImageProvider;
    private final String sceneImageApiKey;
    private final String sceneImageApiBaseUrl;
    private final String apiKey;
    private final String apiBaseUrl;

    GeminiSceneImageGatewaySupport(GeminiSceneImageSupport sceneImageSupport,
                                   String sceneImageProvider,
                                   String sceneImageApiKey,
                                   String sceneImageApiBaseUrl,
                                   String apiKey,
                                   String apiBaseUrl) {
        this.sceneImageSupport = sceneImageSupport;
        this.sceneImageProvider = sceneImageProvider;
        this.sceneImageApiKey = sceneImageApiKey;
        this.sceneImageApiBaseUrl = sceneImageApiBaseUrl;
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
    }

    String resolveSceneImageProviderName() {
        return StringUtils.hasText(sceneImageProvider) ? sceneImageProvider.trim() : DEFAULT_PROVIDER;
    }

    String resolveSceneImageProtocol() {
        return sceneImageSupport.resolveSceneImageProtocol();
    }

    void validateSceneImageGenerationConfiguration() {
        if (isGoogleSceneImageProvider()) {
            validateGoogleOfficialSceneImageConfiguration();
            return;
        }

        String resolvedApiKey = resolveSceneImageApiKey();
        String resolvedApiBaseUrl = resolveSceneImageApiBaseUrl();

        if (!StringUtils.hasText(resolvedApiKey)) {
            throw new BusinessException("Scene image API key is missing; configure SCENE_IMAGE_API_KEY or ONE_API_KEY");
        }
        if (!resolvedApiKey.startsWith("sk-")) {
            throw new BusinessException("Scene image OneAPI token must start with sk-");
        }
        if (!StringUtils.hasText(resolvedApiBaseUrl)) {
            throw new BusinessException("Scene image API base URL is missing; configure SCENE_IMAGE_API_BASE_URL or ONE_API_BASE_URL");
        }
        if (sceneImageSupport.resolveSceneImageModelsToTry().isEmpty()) {
            throw new BusinessException("Scene image model is missing; configure SCENE_IMAGE_MODEL");
        }
    }

    URI buildSceneImageRequestUri() {
        return buildRequestUri(resolveSceneImageProtocol(), resolveSceneImageApiBaseUrl());
    }

    URI buildSecondStageSceneImageRequestUri(String protocol) {
        return buildRequestUri(protocol, resolveSecondStageApiBaseUrl());
    }

    String[] buildSceneImageAuthorizationHeaders() {
        return new String[]{"Authorization", "Bearer " + resolveSceneImageApiKey()};
    }

    String[] buildSecondStageAuthorizationHeaders() {
        return new String[]{"Authorization", "Bearer " + resolveSecondStageApiKey()};
    }

    private URI buildRequestUri(String protocol, String baseUrl) {
        String normalizedBaseUrl = normalizeSceneImageBaseUrl(baseUrl, protocol);
        String suffix = "images-generations".equals(protocol)
                ? "/images/generations"
                : "/chat/completions";
        return URI.create(normalizedBaseUrl + suffix);
    }

    private boolean isGoogleSceneImageProvider() {
        String normalized = resolveSceneImageProviderName().trim().toLowerCase();
        return "google-openai-compatible".equals(normalized)
                || "google-openai".equals(normalized)
                || "google-gemini".equals(normalized)
                || "google".equals(normalized);
    }

    private String resolveSceneImageApiKey() {
        if (StringUtils.hasText(sceneImageApiKey)) {
            return sceneImageApiKey.trim();
        }
        if (StringUtils.hasText(apiKey)) {
            return apiKey.trim();
        }
        return "";
    }

    private String resolveSceneImageApiBaseUrl() {
        if (StringUtils.hasText(sceneImageApiBaseUrl)) {
            return sceneImageApiBaseUrl.trim();
        }
        if (isGoogleSceneImageProvider()) {
            return GOOGLE_OPENAI_BASE_URL;
        }
        if (StringUtils.hasText(apiBaseUrl)) {
            return apiBaseUrl.trim();
        }
        return "";
    }

    private String resolveSecondStageApiKey() {
        if (StringUtils.hasText(sceneImageApiKey)) {
            return sceneImageApiKey.trim();
        }
        return apiKey == null ? "" : apiKey.trim();
    }

    private String resolveSecondStageApiBaseUrl() {
        if (StringUtils.hasText(sceneImageApiBaseUrl)) {
            return sceneImageApiBaseUrl.trim();
        }
        return apiBaseUrl == null ? "" : apiBaseUrl.trim();
    }

    private void validateGoogleOfficialSceneImageConfiguration() {
        if (!StringUtils.hasText(sceneImageApiKey)) {
            throw new BusinessException("Google scene image API key is missing; configure SCENE_IMAGE_API_KEY");
        }
        if (sceneImageApiKey.trim().startsWith("sk-")) {
            throw new BusinessException("Google scene image provider requires a Gemini API key, not a OneAPI sk- token");
        }
        if (sceneImageSupport.resolveSceneImageModelsToTry().isEmpty()) {
            throw new BusinessException("Google scene image model is missing; configure SCENE_IMAGE_MODEL");
        }
    }

    private String normalizeSceneImageBaseUrl(String baseUrl, String protocol) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if ("images-generations".equals(protocol) && normalized.endsWith("/images/generations")) {
            normalized = normalized.substring(0, normalized.length() - "/images/generations".length());
        }
        if ("chat-completions".equals(protocol) && normalized.endsWith("/chat/completions")) {
            normalized = normalized.substring(0, normalized.length() - "/chat/completions".length());
        }
        if (normalized.contains("generativelanguage.googleapis.com")) {
            if (normalized.endsWith("/v1beta/openai")) {
                return normalized;
            }
            if (normalized.endsWith("/openai")) {
                return normalized;
            }
            if (normalized.endsWith("/v1beta")) {
                return normalized + "/openai";
            }
            return normalized + "/v1beta/openai";
        }
        return normalizeBaseUrl(normalized);
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            normalized = normalized.substring(0, normalized.length() - "/chat/completions".length());
        }
        if (!normalized.endsWith("/v1")) {
            normalized = normalized + "/v1";
        }
        return normalized;
    }
}
