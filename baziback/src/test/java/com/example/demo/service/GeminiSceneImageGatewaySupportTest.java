package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GeminiSceneImageGatewaySupportTest {

    @Test
    void resolveSceneImageProviderNameShouldFallbackToOneApiChannel() {
        GeminiSceneImageGatewaySupport support = new GeminiSceneImageGatewaySupport(
                buildSupport("chat-completions"),
                "",
                "",
                "",
                "sk-main",
                "https://gemini.agentpit.io"
        );

        assertEquals("one-api-chat-completions", support.resolveSceneImageProviderName());
    }

    @Test
    void buildSceneImageRequestUriShouldNormalizeGoogleOfficialBaseUrl() {
        GeminiSceneImageGatewaySupport support = new GeminiSceneImageGatewaySupport(
                buildSupport("chat-completions"),
                "google-openai-compatible",
                "AIza-test",
                "",
                "",
                ""
        );

        URI uri = support.buildSceneImageRequestUri();

        assertEquals(
                "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
                uri.toString()
        );
    }

    @Test
    void buildSecondStageAuthorizationHeadersShouldPreferSceneImageKey() {
        GeminiSceneImageGatewaySupport support = new GeminiSceneImageGatewaySupport(
                buildSupport("images-generations"),
                "",
                "sk-scene",
                "https://scene.agentpit.io",
                "sk-main",
                "https://gemini.agentpit.io"
        );

        String[] headers = support.buildSecondStageAuthorizationHeaders();

        assertEquals("Authorization", headers[0]);
        assertEquals("Bearer sk-scene", headers[1]);
    }

    @Test
    void validateSceneImageGenerationConfigurationShouldAllowInheritedOneApiSettings() {
        GeminiSceneImageGatewaySupport support = new GeminiSceneImageGatewaySupport(
                buildSupport("chat-completions"),
                "",
                "",
                "",
                "sk-main",
                "https://gemini.agentpit.io"
        );

        assertDoesNotThrow(support::validateSceneImageGenerationConfiguration);
    }

    private GeminiSceneImageSupport buildSupport(String protocol) {
        return new GeminiSceneImageSupport(
                "gemini-3-pro-image-preview",
                "gemini-2.5-flash-image",
                "gemini-3-pro-preview",
                "gemini-3-flash-preview",
                "gemini-3-pro-image-preview",
                "gemini-2.5-flash-image",
                protocol,
                "b64_json",
                "1024x1024",
                1,
                640,
                0.3d
        );
    }
}
