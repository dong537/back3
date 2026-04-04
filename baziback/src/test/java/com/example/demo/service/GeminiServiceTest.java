package com.example.demo.service;

import com.example.demo.dto.request.gemini.GeminiFaceAnalysisRequest;
import com.example.demo.dto.response.gemini.GeminiFailureDetails;
import com.example.demo.dto.response.gemini.GeminiProbeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeminiServiceTest {

    @Test
    void probeVisionShouldUseOneApiChatCompletionsUriEvenWhenSceneImageChannelConfigured() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": "Image described."
                      }
                    }
                  ]
                }
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        GeminiService service = new GeminiService(httpClient, new ObjectMapper());
        ReflectionTestUtils.setField(service, "apiKey", "sk-main");
        ReflectionTestUtils.setField(service, "apiBaseUrl", "https://gemini.agentpit.io");
        ReflectionTestUtils.setField(service, "visionModel", "gemini-3-flash-preview");
        ReflectionTestUtils.setField(service, "visionPayloadFormats", "openai-image-url");
        ReflectionTestUtils.setField(service, "maxImageBytes", 5_242_880L);
        ReflectionTestUtils.setField(service, "sceneImageProtocol", "images-generations");
        ReflectionTestUtils.setField(service, "sceneImageApiBaseUrl", "https://scene.agentpit.io");

        GeminiFaceAnalysisRequest request = new GeminiFaceAnalysisRequest();
        request.setImageBase64("aGVsbG8=");
        request.setMimeType("image/png");
        request.setPrompt("Describe this image in one short sentence.");

        GeminiProbeResponse probeResponse = service.probeVision(request);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        assertEquals("https://gemini.agentpit.io/v1/chat/completions", requestCaptor.getValue().uri().toString());
        assertEquals("Image described.", probeResponse.getContent());
    }

    @Test
    void resolveVisionModelsToTryShouldSkipEmbeddingModelsAndDeduplicate() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper());
        ReflectionTestUtils.setField(service, "visionModel", "gemini-3-flash-preview");
        ReflectionTestUtils.setField(
                service,
                "visionModels",
                "gemini-3-flash-preview, text-embedding-004, gemini-2.0-flash, text-embedding-v3, gemini-2.0-flash"
        );

        List<String> models = ReflectionTestUtils.invokeMethod(service, "resolveVisionModelsToTry");

        assertEquals(List.of("gemini-3-flash-preview", "gemini-2.0-flash"), models);
    }

    @Test
    void resolveVisionPayloadFormatsToTryShouldDeduplicateAndIgnoreUnsupportedValues() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper());
        ReflectionTestUtils.setField(
                service,
                "visionPayloadFormats",
                "openai-image-url, openai-image-url-string, invalid-format, openai-image-url"
        );

        List<String> formats = ReflectionTestUtils.invokeMethod(service, "resolveVisionPayloadFormatsToTry");

        assertEquals(List.of("openai-image-url", "openai-image-url-string"), formats);
    }

    @Test
    void appendAttemptedModelsShouldOnlyAppendOnFinalFailure() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper());

        String finalMessage = ReflectionTestUtils.invokeMethod(
                service,
                "appendAttemptedModels",
                "Upstream failed",
                List.of("gemini-2.0-flash", "gemini-3-flash-preview"),
                false
        );
        String retryingMessage = ReflectionTestUtils.invokeMethod(
                service,
                "appendAttemptedModels",
                "Upstream failed",
                List.of("gemini-2.0-flash"),
                true
        );

        assertEquals("Upstream failed | attemptedModels=gemini-2.0-flash,gemini-3-flash-preview", finalMessage);
        assertEquals("Upstream failed", retryingMessage);
    }

    @Test
    void buildFailureDetailsShouldExposeStructuredAttemptContext() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper());

        GeminiFailureDetails details = ReflectionTestUtils.invokeMethod(
                service,
                "buildFailureDetails",
                List.of("gemini-2.0-flash", "gemini-3-flash-preview"),
                "gemini-3-flash-preview",
                500,
                "openai-image-url-string",
                URI.create("https://gemini.agentpit.io/v1/chat/completions")
        );

        assertEquals(List.of("gemini-2.0-flash", "gemini-3-flash-preview"), details.getAttemptedModels());
        assertEquals("gemini-3-flash-preview", details.getLastModel());
        assertEquals(500, details.getLastStatus());
        assertEquals("openai-image-url-string", details.getLastPayloadFormat());
        assertEquals("https://gemini.agentpit.io/v1/chat/completions", details.getUri());
    }

    @Test
    void resolveSceneImageModelsToTryShouldIgnoreUnsupportedFallbackModels() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper());
        ReflectionTestUtils.setField(service, "sceneImageModel", "gemini-3-pro-image-preview");
        ReflectionTestUtils.setField(service, "sceneImageModels", "gemini-2.5-flash-image");
        ReflectionTestUtils.setField(service, "textModel", "gemini-3-flash-preview");
        ReflectionTestUtils.setField(service, "visionModel", "gemini-3-pro-preview");
        ReflectionTestUtils.setField(service, "visionModels", "gemini-3-flash-preview");

        List<String> models = ReflectionTestUtils.invokeMethod(service, "resolveSceneImageModelsToTry");

        assertEquals(List.of("gemini-3-pro-image-preview", "gemini-2.5-flash-image"), models);
    }

    @Test
    void buildSceneImageChatRequestBodyShouldClampTokensToProviderFloor() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper());
        ReflectionTestUtils.setField(service, "temperature", 0.3d);
        ReflectionTestUtils.setField(service, "sceneImageChatMaxTokens", 640);
        ReflectionTestUtils.setField(service, "maxTokens", 1024);

        Map<String, Object> body = ReflectionTestUtils.invokeMethod(
                service,
                "buildSceneImageChatRequestBody",
                "gemini-3-flash-preview",
                "Generate a mystical scene"
        );

        List<?> messages = (List<?>) body.get("messages");
        Map<?, ?> message = (Map<?, ?>) messages.get(0);

        assertEquals("gemini-3-flash-preview", body.get("model"));
        assertEquals(List.of("text", "image"), body.get("modalities"));
        assertEquals("Generate a mystical scene", message.get("content"));
        assertEquals(2000, body.get("max_tokens"));
    }

    @Test
    void resolveSecondStageProtocolsToTryShouldPreferConfiguredProtocolThenFallback() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper());
        ReflectionTestUtils.setField(service, "sceneImageProtocol", "images-generations");

        List<String> protocols = ReflectionTestUtils.invokeMethod(service, "resolveSecondStageProtocolsToTry");

        assertEquals(List.of("images-generations", "chat-completions"), protocols);
    }
}
