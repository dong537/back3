package com.example.demo.service;

import com.example.demo.dto.response.gemini.GeminiFailureDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiServiceTest {

    @Test
    void buildRequestUriShouldUseOneApiBaseUrlWithV1() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "apiBaseUrl", "https://gemini.agentpit.io/v1");

        URI uri = ReflectionTestUtils.invokeMethod(service, "buildRequestUri");

        assertEquals("https://gemini.agentpit.io/v1/chat/completions", uri.toString());
    }

    @Test
    void buildRequestUriShouldAppendV1WhenMissing() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "apiBaseUrl", "https://gemini.agentpit.io");

        URI uri = ReflectionTestUtils.invokeMethod(service, "buildRequestUri");

        assertEquals("https://gemini.agentpit.io/v1/chat/completions", uri.toString());
    }

    @Test
    void authorizationHeadersShouldUseBearerToken() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "apiKey", "sk-test-key");

        String[] headers = ReflectionTestUtils.invokeMethod(service, "buildAuthorizationHeaders");

        assertEquals("Authorization", headers[0]);
        assertEquals("Bearer sk-test-key", headers[1]);
    }

    @Test
    void requestBodyShouldUseVisionModelAndMaxTokens() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "visionModel", "gemini-3-flash-preview");
        ReflectionTestUtils.setField(service, "visionPayloadFormats", "openai-image-url");
        ReflectionTestUtils.setField(service, "temperature", 0.2d);
        ReflectionTestUtils.setField(service, "maxTokens", 2048);

        Map<String, Object> body = ReflectionTestUtils.invokeMethod(
                service,
                "buildRequestBody",
                "aGVsbG8=",
                "image/jpeg",
                "Return JSON"
        );

        assertEquals("gemini-3-flash-preview", body.get("model"));
        assertEquals(2048, body.get("max_tokens"));
    }

    @Test
    void textProbeRequestBodyShouldUseTextModel() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "textModel", "gemini-3-flash-preview");
        ReflectionTestUtils.setField(service, "temperature", 0.2d);
        ReflectionTestUtils.setField(service, "maxTokens", 2048);

        Map<String, Object> body = ReflectionTestUtils.invokeMethod(
                service,
                "buildTextProbeRequestBody",
                "Reply with OK."
        );

        assertEquals("gemini-3-flash-preview", body.get("model"));
        assertEquals(200, body.get("max_tokens"));
    }

    @Test
    void parseResponseShouldStripMarkdownJsonFence() throws Exception {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "visionModel", "gemini-3-flash-preview");

        String responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "```json\\n{\\"hasFace\\":true,\\"faceCount\\":1}\\n```"
                      }
                    }
                  ]
                }
                """;

        Map<String, Object> result = ReflectionTestUtils.invokeMethod(service, "parseResponse", responseBody, "gemini-2.0-flash");

        assertEquals(Boolean.TRUE, result.get("hasFace"));
        assertEquals(1, result.get("faceCount"));
        assertEquals("gemini-2.0-flash", result.get("model"));
    }

    @Test
    void resolveVisionModelsToTryShouldSkipEmbeddingModelsAndDeduplicate() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
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
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(
                service,
                "visionPayloadFormats",
                "openai-image-url, openai-image-url-string, invalid-format, openai-image-url"
        );

        List<String> formats = ReflectionTestUtils.invokeMethod(service, "resolveVisionPayloadFormatsToTry");

        assertEquals(List.of("openai-image-url", "openai-image-url-string"), formats);
    }

    @Test
    void buildVisionRequestBodyShouldSupportStringImageUrlPayloadFormat() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));

        Map<String, Object> body = ReflectionTestUtils.invokeMethod(
                service,
                "buildVisionRequestBody",
                "aGVsbG8=",
                "image/png",
                "Describe image",
                256,
                "gemini-3-flash-preview",
                "openai-image-url-string"
        );

        List<?> messages = (List<?>) body.get("messages");
        Map<?, ?> message = (Map<?, ?>) messages.get(0);
        List<?> contents = (List<?>) message.get("content");
        Map<?, ?> imagePart = (Map<?, ?>) contents.get(1);

        assertEquals("data:image/png;base64,aGVsbG8=", imagePart.get("image_url"));
    }

    @Test
    void validateOneApiConfigurationShouldRequireSkPrefix() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "apiKey", "AIza-invalid");
        ReflectionTestUtils.setField(service, "apiBaseUrl", "https://gemini.agentpit.io/v1");
        ReflectionTestUtils.setField(service, "visionModel", "gemini-3-flash-preview");

        try {
            ReflectionTestUtils.invokeMethod(service, "validateOneApiConfiguration");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() == null || ex.getCause().getMessage().contains("sk-"));
            return;
        }

        throw new AssertionError("Expected validateOneApiConfiguration to reject non-sk key");
    }

    @Test
    void appendAttemptedModelsShouldOnlyAppendOnFinalFailure() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));

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
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));

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
    void resolveSceneImageModelsToTryShouldAppendTextAndVisionFallbackModels() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "sceneImageModel", "gemini-3-pro-image-preview");
        ReflectionTestUtils.setField(service, "sceneImageModels", "gemini-2.5-flash-image");
        ReflectionTestUtils.setField(service, "textModel", "gemini-3-flash-preview");
        ReflectionTestUtils.setField(service, "visionModel", "gemini-3-pro-preview");
        ReflectionTestUtils.setField(service, "visionModels", "gemini-3-flash-preview");

        List<String> models = ReflectionTestUtils.invokeMethod(service, "resolveSceneImageModelsToTry");

        assertEquals(
                List.of("gemini-3-flash-preview", "gemini-3-pro-preview", "gemini-3-pro-image-preview", "gemini-2.5-flash-image"),
                models
        );
    }

    @Test
    void buildSceneImageChatRequestBodyShouldUseModalitiesAndStringContent() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
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
        assertEquals(640, body.get("max_tokens"));
    }

    @Test
    void resolveSecondStageProtocolsToTryShouldPreferConfiguredProtocolThenFallback() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));
        ReflectionTestUtils.setField(service, "sceneImageProtocol", "images-generations");

        List<String> protocols = ReflectionTestUtils.invokeMethod(service, "resolveSecondStageProtocolsToTry");

        assertEquals(List.of("images-generations", "chat-completions"), protocols);
    }

    @Test
    void buildSecondStageDrawingPromptShouldAppendNegativePrompt() {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));

        String prompt = ReflectionTestUtils.invokeMethod(
                service,
                "buildSecondStageDrawingPrompt",
                "东方电影感夜桥独行者",
                "卡通, 低清晰度, 多余手指",
                "fallback"
        );

        assertEquals("东方电影感夜桥独行者\n\n避免元素：卡通, 低清晰度, 多余手指", prompt);
    }

    @Test
    void parseSceneImagePlanPayloadShouldExtractPromptOnlyFields() throws Exception {
        GeminiService service = new GeminiService(HttpClient.newHttpClient(), new ObjectMapper(), new TokenTracker(null, new ObjectMapper()));

        Object payload = ReflectionTestUtils.invokeMethod(
                service,
                "parseSceneImagePlanPayload",
                """
                {
                  "visual_summary": "夜色中的行旅人站在桥上，风中带着将变未变的意味。",
                  "revised_prompt": "东方电影感，夜桥，独行者，卦盘微光，竖版构图",
                  "negative_prompt": "卡通, 低清晰度, 多余手指",
                  "display_text": "当前已生成场景方案，可继续交给绘图模型出图。"
                }
                """
        );

        assertEquals("夜色中的行旅人站在桥上，风中带着将变未变的意味。", readRecordValue(payload, "visualSummary"));
        assertEquals("东方电影感，夜桥，独行者，卦盘微光，竖版构图", readRecordValue(payload, "revisedPrompt"));
        assertEquals("卡通, 低清晰度, 多余手指", readRecordValue(payload, "negativePrompt"));
        assertEquals("当前已生成场景方案，可继续交给绘图模型出图。", readRecordValue(payload, "displayText"));
    }

    private Object readRecordValue(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }
}
