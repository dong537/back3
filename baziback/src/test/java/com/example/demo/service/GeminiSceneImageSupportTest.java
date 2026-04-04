package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeminiSceneImageSupportTest {

    @Test
    void resolveSceneImageModelsToTryShouldKeepImageCapableModelsOnly() {
        GeminiSceneImageSupport support = new GeminiSceneImageSupport(
                "gemini-3-pro-image-preview",
                "gemini-2.5-flash-image",
                "gemini-3-pro-preview",
                "gemini-3-flash-preview",
                "gemini-3-pro-image-preview",
                "gemini-2.5-flash-image",
                "chat-completions",
                "b64_json",
                "1024x1024",
                1,
                640,
                0.3d
        );

        List<String> models = support.resolveSceneImageModelsToTry();

        assertEquals(List.of("gemini-3-pro-image-preview", "gemini-2.5-flash-image"), models);
    }

    @Test
    void buildSceneImageChatRequestBodyShouldApplyImageModalitiesAndTokenFloor() {
        GeminiSceneImageSupport support = new GeminiSceneImageSupport(
                "",
                "",
                "",
                "",
                "gemini-3-pro-image-preview",
                "",
                "chat-completions",
                "b64_json",
                "1024x1024",
                1,
                640,
                0.3d
        );

        Map<String, Object> body = support.buildSceneImageChatRequestBody(
                "gemini-3-pro-image-preview",
                "Generate a mystical scene"
        );

        assertEquals(List.of("text", "image"), body.get("modalities"));
        assertEquals(2000, body.get("max_tokens"));
    }
}
