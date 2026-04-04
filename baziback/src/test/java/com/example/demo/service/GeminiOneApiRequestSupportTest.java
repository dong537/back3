package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiOneApiRequestSupportTest {

    @Test
    void buildRequestUriShouldUseOneApiBaseUrlWithV1() {
        GeminiOneApiRequestSupport support = buildSupport("sk-test-key", "https://gemini.agentpit.io/v1");

        URI uri = support.buildRequestUri();

        assertEquals("https://gemini.agentpit.io/v1/chat/completions", uri.toString());
    }

    @Test
    void buildRequestUriShouldAppendV1WhenMissing() {
        GeminiOneApiRequestSupport support = buildSupport("sk-test-key", "https://gemini.agentpit.io");

        URI uri = support.buildRequestUri();

        assertEquals("https://gemini.agentpit.io/v1/chat/completions", uri.toString());
    }

    @Test
    void authorizationHeadersShouldUseBearerToken() {
        GeminiOneApiRequestSupport support = buildSupport("sk-test-key", "https://gemini.agentpit.io");

        String[] headers = support.buildAuthorizationHeaders();

        assertEquals("Authorization", headers[0]);
        assertEquals("Bearer sk-test-key", headers[1]);
    }

    @Test
    void textProbeRequestBodyShouldUseTextModel() {
        GeminiOneApiRequestSupport support = buildSupport("sk-test-key", "https://gemini.agentpit.io");

        Map<String, Object> body = support.buildTextProbeRequestBody("Reply with OK.");

        assertEquals("gemini-3-flash-preview", body.get("model"));
        assertEquals(200, body.get("max_tokens"));
    }

    @Test
    void buildVisionRequestBodyShouldUseProvidedModelAndPayloadFormat() {
        GeminiOneApiRequestSupport support = buildSupport("sk-test-key", "https://gemini.agentpit.io");

        Map<String, Object> body = support.buildVisionRequestBody(
                "aGVsbG8=",
                "image/png",
                "Describe image",
                256,
                "gemini-3-flash-preview",
                "openai-image-url-string"
        );

        assertEquals("gemini-3-flash-preview", body.get("model"));
        assertEquals(256, body.get("max_tokens"));
        List<?> messages = (List<?>) body.get("messages");
        Map<?, ?> message = (Map<?, ?>) messages.get(0);
        List<?> contents = (List<?>) message.get("content");
        Map<?, ?> imagePart = (Map<?, ?>) contents.get(1);
        assertEquals("data:image/png;base64,aGVsbG8=", imagePart.get("image_url"));
    }

    @Test
    void validateOneApiConfigurationShouldRequireSkPrefix() {
        GeminiOneApiRequestSupport support = buildSupport("AIza-invalid", "https://gemini.agentpit.io/v1");

        try {
            support.validateOneApiConfiguration();
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("sk-"));
            return;
        }

        throw new AssertionError("Expected validateOneApiConfiguration to reject non-sk key");
    }

    private GeminiOneApiRequestSupport buildSupport(String apiKey, String apiBaseUrl) {
        return new GeminiOneApiRequestSupport(
                apiKey,
                apiBaseUrl,
                "gemini-3-flash-preview",
                "gemini-3-flash-preview",
                0.2d,
                2048
        );
    }
}
