package com.example.demo.service;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class GeminiSceneImageSupport {

    private static final Set<String> ONE_API_CHAT_IMAGE_MODELS = Set.of(
            "gemini-2.0-flash-exp",
            "gemini-3-pro-image-preview"
    );

    private static final Set<String> ONE_API_CHAT_IMAGE_UNSUPPORTED_MODELS = Set.of(
            "gemini-2.0-pro-exp-02-05",
            "gemini-3-flash-preview",
            "gemini-3-pro-preview",
            "gemini-3.1-pro-preview"
    );

    private static final int MIN_SCENE_IMAGE_CHAT_MAX_TOKENS = 2000;

    private final String imageModel;
    private final String imageModels;
    private final String visionModel;
    private final String visionModels;
    private final String sceneImageModel;
    private final String sceneImageModels;
    private final String sceneImageProtocol;
    private final String sceneImageResponseFormat;
    private final String sceneImageSize;
    private final int sceneImageCount;
    private final int sceneImageChatMaxTokens;
    private final double temperature;

    GeminiSceneImageSupport(
            String imageModel,
            String imageModels,
            String visionModel,
            String visionModels,
            String sceneImageModel,
            String sceneImageModels,
            String sceneImageProtocol,
            String sceneImageResponseFormat,
            String sceneImageSize,
            int sceneImageCount,
            int sceneImageChatMaxTokens,
            double temperature
    ) {
        this.imageModel = imageModel;
        this.imageModels = imageModels;
        this.visionModel = visionModel;
        this.visionModels = visionModels;
        this.sceneImageModel = sceneImageModel;
        this.sceneImageModels = sceneImageModels;
        this.sceneImageProtocol = sceneImageProtocol;
        this.sceneImageResponseFormat = sceneImageResponseFormat;
        this.sceneImageSize = sceneImageSize;
        this.sceneImageCount = sceneImageCount;
        this.sceneImageChatMaxTokens = sceneImageChatMaxTokens;
        this.temperature = temperature;
    }

    List<String> resolveImageModelsToTry() {
        List<String> models = new ArrayList<>();
        appendImageGenerationModel(models, imageModel);
        appendImageGenerationModel(models, visionModel);
        if (StringUtils.hasText(imageModels)) {
            for (String candidate : imageModels.split(",")) {
                appendImageGenerationModel(models, candidate);
            }
        }
        if (StringUtils.hasText(visionModels)) {
            for (String candidate : visionModels.split(",")) {
                appendImageGenerationModel(models, candidate);
            }
        }
        if (models.isEmpty()) {
            models.add("gemini-3-pro-image-preview");
        }
        return models;
    }

    List<String> resolveSceneImageModelsToTry() {
        List<String> models = new ArrayList<>();
        appendSceneImageModel(models, sceneImageModel);
        if (StringUtils.hasText(sceneImageModels)) {
            for (String candidate : sceneImageModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        appendSceneImageModel(models, imageModel);
        for (String candidate : resolveImageModelsToTry()) {
            appendSceneImageModel(models, candidate);
        }
        if (models.isEmpty()) {
            models.add("gemini-3-pro-image-preview");
        }
        return models;
    }

    List<String> resolveSecondStageImageModelsToTry() {
        List<String> models = new ArrayList<>();
        appendSceneImageModel(models, sceneImageModel);
        if (StringUtils.hasText(sceneImageModels)) {
            for (String candidate : sceneImageModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        appendSceneImageModel(models, imageModel);
        if (StringUtils.hasText(imageModels)) {
            for (String candidate : imageModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        appendSceneImageModel(models, visionModel);
        if (StringUtils.hasText(visionModels)) {
            for (String candidate : visionModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        if (models.isEmpty()) {
            models.add("gemini-3-pro-image-preview");
        }
        return models;
    }

    List<String> resolveSecondStageProtocolsToTry() {
        List<String> protocols = new ArrayList<>();
        appendProtocol(protocols, resolveSceneImageProtocol());
        appendProtocol(protocols, "images-generations");
        appendProtocol(protocols, "chat-completions");
        return protocols;
    }

    String resolveSceneImageProtocol() {
        String normalized = sceneImageProtocol == null ? "" : sceneImageProtocol.trim().toLowerCase();
        if ("disabled".equals(normalized) || "none".equals(normalized) || "doc-only".equals(normalized)) {
            return "disabled";
        }
        if ("images".equals(normalized)
                || "images.generate".equals(normalized)
                || "images/generations".equals(normalized)
                || "images-generations".equals(normalized)) {
            return "images-generations";
        }
        return "chat-completions";
    }

    Map<String, Object> buildSceneImageGenerationRequestBody(String modelName, String prompt) {
        if ("images-generations".equals(resolveSceneImageProtocol())) {
            return buildSceneImageImagesRequestBody(modelName, prompt);
        }
        return buildSceneImageChatRequestBody(modelName, prompt);
    }

    Map<String, Object> buildSecondStageSceneImageRequestBody(String modelName, String prompt, String protocol) {
        if ("images-generations".equals(protocol)) {
            return buildSceneImageImagesRequestBody(modelName, prompt);
        }
        return buildSceneImageChatRequestBody(modelName, prompt);
    }

    Map<String, Object> buildSceneImageImagesRequestBody(String modelName, String prompt) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName.trim());
        requestBody.put("prompt", prompt);
        requestBody.put("response_format", StringUtils.hasText(sceneImageResponseFormat) ? sceneImageResponseFormat.trim() : "b64_json");
        requestBody.put("n", Math.max(sceneImageCount, 1));
        if (StringUtils.hasText(sceneImageSize)) {
            requestBody.put("size", sceneImageSize.trim());
        }
        return requestBody;
    }

    Map<String, Object> buildSceneImageChatRequestBody(String modelName, String prompt) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("responseModalities", List.of("TEXT", "IMAGE"));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName.trim());
        requestBody.put("messages", List.of(message));
        requestBody.put("generationConfig", generationConfig);
        requestBody.put("modalities", List.of("text", "image"));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", Math.max(MIN_SCENE_IMAGE_CHAT_MAX_TOKENS, sceneImageChatMaxTokens));
        return requestBody;
    }

    private void appendProtocol(List<String> protocols, String candidate) {
        if (!StringUtils.hasText(candidate) || "disabled".equals(candidate)) {
            return;
        }
        if (!protocols.contains(candidate)) {
            protocols.add(candidate);
        }
    }

    private void appendImageGenerationModel(List<String> models, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        if (!isLikelyImageGenerationModel(normalized.toLowerCase())) {
            return;
        }
        if (!models.contains(normalized)) {
            models.add(normalized);
        }
    }

    private void appendSceneImageModel(List<String> models, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        if (!isSupportedSceneImageModel(normalized)) {
            return;
        }
        if (!models.contains(normalized)) {
            models.add(normalized);
        }
    }

    private boolean isLikelyImageGenerationModel(String normalizedModelName) {
        if (ONE_API_CHAT_IMAGE_MODELS.contains(normalizedModelName)) {
            return true;
        }
        if (ONE_API_CHAT_IMAGE_UNSUPPORTED_MODELS.contains(normalizedModelName)) {
            return false;
        }
        return normalizedModelName.contains("image")
                || normalizedModelName.contains("imagen")
                || normalizedModelName.contains("generate");
    }

    private boolean isSupportedSceneImageModel(String modelName) {
        String normalized = modelName == null ? "" : modelName.trim().toLowerCase();
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        if (ONE_API_CHAT_IMAGE_UNSUPPORTED_MODELS.contains(normalized)) {
            return false;
        }
        return isLikelyImageGenerationModel(normalized);
    }
}
