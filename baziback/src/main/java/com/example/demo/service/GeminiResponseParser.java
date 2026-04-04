package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
final class GeminiResponseParser {

    private final ObjectMapper objectMapper;
    private final String sceneImageProviderName;

    GeminiResponseParser(ObjectMapper objectMapper, String sceneImageProviderName) {
        this.objectMapper = objectMapper;
        this.sceneImageProviderName = sceneImageProviderName;
    }

    Map<String, Object> parseResponse(String responseBody, String actualModel) throws Exception {
        String text = parseRawResponseText(responseBody);
        Map<String, Object> parsedResult = tryParseJson(text);
        parsedResult.put("provider", "gemini");
        parsedResult.put("model", actualModel);
        parsedResult.put("rawText", text);
        return parsedResult;
    }

    @SuppressWarnings("unchecked")
    String parseRawResponseText(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("Gemini 返回内容为空");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = firstChoice.get("message") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : null;
        if (message == null || message.isEmpty()) {
            throw new BusinessException("Gemini 返回内容格式错误");
        }

        String text = extractMessageText(message.get("content"));
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("Gemini 返回内容为空");
        }
        return text;
    }

    SceneImageExecutionPayload parseSceneImageResponseResult(String responseBody,
                                                             String actualModel,
                                                             URI requestUri,
                                                             String protocol) throws Exception {
        if ("images-generations".equals(protocol)) {
            GeneratedImagePayloadData payload = parseImageGenerationResponse(responseBody);
            return new SceneImageExecutionPayload(
                    sceneImageProviderName,
                    actualModel,
                    requestUri,
                    payload.imageBase64(),
                    payload.imageUrl(),
                    payload.revisedPrompt(),
                    null,
                    null,
                    "已通过图片生成接口返回场景图。",
                    "direct_image"
            );
        }
        return parseSceneImageChatResponse(responseBody, actualModel, requestUri);
    }

    GeneratedImagePayloadData parseSceneImageResponse(String responseBody, String protocol) throws Exception {
        if ("images-generations".equals(protocol)) {
            return parseImageGenerationResponse(responseBody);
        }
        return parseChatImageResponse(responseBody);
    }

    SceneImagePlanPayloadData parseSceneImagePlanPayload(String rawText) {
        String normalizedText = stripMarkdownCodeFence(rawText);
        Map<String, Object> parsed = parseJsonObject(normalizedText);
        if (parsed == null) {
            parsed = extractStructuredJsonObject(normalizedText);
        }

        if (parsed == null || parsed.isEmpty()) {
            String fallbackText = trimToNull(normalizedText);
            return new SceneImagePlanPayloadData(
                    fallbackText,
                    fallbackText,
                    null,
                    "当前 YMAPI 通道未直接返回图片，已改为返回可继续绘图的场景方案。"
            );
        }

        String visualSummary = trimToNull(firstNonBlank(
                objectToString(parsed.get("visual_summary")),
                objectToString(parsed.get("scene_summary")),
                objectToString(parsed.get("summary"))
        ));
        String revisedPrompt = trimToNull(firstNonBlank(
                objectToString(parsed.get("revised_prompt")),
                objectToString(parsed.get("image_prompt")),
                objectToString(parsed.get("prompt"))
        ));
        String negativePrompt = trimToNull(firstNonBlank(
                objectToString(parsed.get("negative_prompt")),
                objectToString(parsed.get("avoid")),
                objectToString(parsed.get("negative"))
        ));
        String displayText = trimToNull(firstNonBlank(
                objectToString(parsed.get("display_text")),
                objectToString(parsed.get("message"))
        ));

        if (!StringUtils.hasText(visualSummary)) {
            visualSummary = revisedPrompt;
        }
        if (!StringUtils.hasText(revisedPrompt)) {
            revisedPrompt = visualSummary;
        }
        if (!StringUtils.hasText(displayText)) {
            displayText = "当前 YMAPI 通道未直接返回图片，已改为返回可继续绘图的场景方案。";
        }

        return new SceneImagePlanPayloadData(
                visualSummary,
                revisedPrompt,
                negativePrompt,
                displayText
        );
    }

    @SuppressWarnings("unchecked")
    private SceneImageExecutionPayload parseSceneImageChatResponse(String responseBody,
                                                                  String actualModel,
                                                                  URI requestUri) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("场景图通道未返回可用结果");
        }

        Map<String, Object> firstChoice = choices.get(0);
        String finishReason = trimToNull(objectToString(firstChoice.get("finish_reason")));
        Map<String, Object> message = firstChoice.get("message") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : null;
        if (message == null || message.isEmpty()) {
            throw new BusinessException("场景图通道返回内容格式错误");
        }

        GeneratedImagePayloadData imagePayload = extractImagePayloadFromChatMessage(message);
        String text = extractMessageText(message.get("content"));
        SceneImagePlanPayloadData planPayload = StringUtils.hasText(text)
                ? parseSceneImagePlanPayload(text)
                : null;

        log.info(
                "Scene image chat response parsed | model={}, uri={}, finishReason={}, hasImagePayload={}, textLength={}, hasPlanPayload={}, contentType={}, rawBody={}",
                actualModel,
                requestUri,
                finishReason,
                imagePayload != null,
                text == null ? 0 : text.length(),
                planPayload != null,
                message.get("content") == null ? "null" : message.get("content").getClass().getSimpleName(),
                abbreviate(responseBody)
        );

        if (imagePayload != null) {
            String revisedPrompt = StringUtils.hasText(imagePayload.revisedPrompt())
                    ? imagePayload.revisedPrompt()
                    : (planPayload == null ? null : planPayload.revisedPrompt());
            String visualSummary = planPayload == null ? null : planPayload.visualSummary();
            String negativePrompt = planPayload == null ? null : planPayload.negativePrompt();
            String displayText = planPayload == null
                    ? "已通过 YMAPI chat.completions 返回场景图。"
                    : planPayload.displayText();

            return new SceneImageExecutionPayload(
                    sceneImageProviderName,
                    actualModel,
                    requestUri,
                    imagePayload.imageBase64(),
                    imagePayload.imageUrl(),
                    revisedPrompt,
                    visualSummary,
                    negativePrompt,
                    displayText,
                    "direct_image"
            );
        }

        if (planPayload != null) {
            return new SceneImageExecutionPayload(
                    sceneImageProviderName,
                    actualModel,
                    requestUri,
                    null,
                    null,
                    planPayload.revisedPrompt(),
                    planPayload.visualSummary(),
                    planPayload.negativePrompt(),
                    planPayload.displayText(),
                    "prompt_only"
            );
        }

        if (StringUtils.hasText(finishReason) && "max_tokens".equalsIgnoreCase(finishReason)) {
            throw new BusinessException("当前 OneAPI 已返回 200，但输出在图片落地前被 max_tokens 截断了；请提高 SCENE_IMAGE_CHAT_MAX_TOKENS，建议至少 2000");
        }

        throw new BusinessException("当前 YMAPI 通道既没有返回图片，也没有返回可用的场景方案");
    }

    @SuppressWarnings("unchecked")
    private GeneratedImagePayloadData parseImageGenerationResponse(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
        if (data == null || data.isEmpty()) {
            throw new BusinessException("Gemini 返回图片内容为空");
        }

        Map<String, Object> firstData = data.get(0);
        String imageBase64 = objectToString(firstData.get("b64_json"));
        String imageUrl = objectToString(firstData.get("url"));
        String revisedPrompt = objectToString(firstData.get("revised_prompt"));

        if (!StringUtils.hasText(imageBase64) && !StringUtils.hasText(imageUrl)) {
            throw new BusinessException("Gemini 返回图片内容为空");
        }

        return new GeneratedImagePayloadData(
                sanitizeBase64(imageBase64),
                StringUtils.hasText(imageUrl) ? imageUrl.trim() : null,
                StringUtils.hasText(revisedPrompt) ? revisedPrompt.trim() : null
        );
    }

    @SuppressWarnings("unchecked")
    private GeneratedImagePayloadData parseChatImageResponse(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("聊天协议未返回可用结果");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = firstChoice.get("message") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : null;
        if (message == null || message.isEmpty()) {
            throw new BusinessException("聊天协议返回内容格式错误");
        }

        GeneratedImagePayloadData payload = extractImagePayloadFromChatMessage(message);
        if (payload != null) {
            return payload;
        }

        String text = extractMessageText(message.get("content"));
        if (StringUtils.hasText(text)) {
            throw new BusinessException("当前 OneAPI /chat/completions 只返回了文本，没有图片内容；请检查 OneAPI 是否已按文档为该模型注入 generationConfig.responseModalities，并正确把 inlineData 转成 image_url");
        }

        throw new BusinessException("当前 OneAPI /chat/completions 返回为空，未携带任何图片内容；请检查 OneAPI 网关是否正确透传 Gemini 图片响应");
    }

    @SuppressWarnings("unchecked")
    private GeneratedImagePayloadData extractImagePayloadFromChatMessage(Map<String, Object> message) {
        String directImageBase64 = sanitizeBase64(objectToString(message.get("b64_json")));
        String directImageUrl = normalizeImageUrl(objectToString(message.get("image_url")));
        String directRevisedPrompt = trimToNull(objectToString(message.get("revised_prompt")));
        if (StringUtils.hasText(directImageBase64) || StringUtils.hasText(directImageUrl)) {
            return new GeneratedImagePayloadData(directImageBase64, directImageUrl, directRevisedPrompt);
        }

        Object images = message.get("images");
        if (images instanceof List<?> list) {
            for (Object item : list) {
                GeneratedImagePayloadData payload = extractImagePayloadFromUnknownItem(item);
                if (payload != null) {
                    return payload;
                }
            }
        }

        Object content = message.get("content");
        if (content instanceof List<?> list) {
            return extractImagePayloadFromContentItems(list);
        }
        if (content instanceof Map<?, ?> contentMap) {
            Object parts = contentMap.get("parts");
            if (parts instanceof List<?> list) {
                return extractImagePayloadFromContentItems(list);
            }
        }

        return null;
    }

    private GeneratedImagePayloadData extractImagePayloadFromContentItems(List<?> items) {
        String revisedPrompt = null;
        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                String type = objectToString(map.get("type")).trim().toLowerCase();
                if (!StringUtils.hasText(revisedPrompt) && ("text".equals(type) || "output_text".equals(type))) {
                    revisedPrompt = trimToNull(objectToString(map.get("text")));
                }
            }
            GeneratedImagePayloadData payload = extractImagePayloadFromUnknownItem(item);
            if (payload != null) {
                return new GeneratedImagePayloadData(
                        payload.imageBase64(),
                        payload.imageUrl(),
                        payload.revisedPrompt() != null ? payload.revisedPrompt() : revisedPrompt
                );
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private GeneratedImagePayloadData extractImagePayloadFromUnknownItem(Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof String text) {
            String normalizedText = text.trim();
            if (normalizedText.startsWith("data:image/")) {
                return new GeneratedImagePayloadData(extractBase64FromDataUrl(normalizedText), normalizedText, null);
            }
            if (normalizedText.startsWith("http://") || normalizedText.startsWith("https://")) {
                return new GeneratedImagePayloadData(null, normalizedText, null);
            }
            return null;
        }
        if (!(item instanceof Map<?, ?> rawMap)) {
            return null;
        }

        Map<String, Object> map = (Map<String, Object>) rawMap;
        String imageBase64 = sanitizeBase64(objectToString(map.get("b64_json")));
        if (!StringUtils.hasText(imageBase64)) {
            imageBase64 = sanitizeBase64(objectToString(map.get("image_base64")));
        }
        if (!StringUtils.hasText(imageBase64)) {
            Object inlineData = map.get("inline_data");
            if (inlineData instanceof Map<?, ?> inlineMap) {
                imageBase64 = sanitizeBase64(objectToString(inlineMap.get("data")));
            }
        }
        if (!StringUtils.hasText(imageBase64)) {
            Object inlineData = map.get("inlineData");
            if (inlineData instanceof Map<?, ?> inlineMap) {
                imageBase64 = sanitizeBase64(objectToString(inlineMap.get("data")));
            }
        }

        String imageUrl = normalizeImageUrl(objectToString(map.get("url")));
        if (!StringUtils.hasText(imageUrl)) {
            Object imageUrlValue = map.get("image_url");
            if (imageUrlValue instanceof Map<?, ?> imageUrlMap) {
                imageUrl = normalizeImageUrl(objectToString(imageUrlMap.get("url")));
            } else {
                imageUrl = normalizeImageUrl(objectToString(imageUrlValue));
            }
        }

        if (!StringUtils.hasText(imageBase64) && !StringUtils.hasText(imageUrl)) {
            return null;
        }

        String revisedPrompt = trimToNull(objectToString(map.get("revised_prompt")));
        return new GeneratedImagePayloadData(imageBase64, imageUrl, revisedPrompt);
    }

    private String extractMessageText(Object contentObj) {
        if (contentObj == null) {
            return "";
        }
        if (contentObj instanceof String content) {
            return content.trim();
        }
        if (contentObj instanceof List<?> items) {
            StringBuilder builder = new StringBuilder();
            for (Object item : items) {
                if (item instanceof Map<?, ?> map) {
                    Object text = map.get("text");
                    if (text != null) {
                        if (!builder.isEmpty()) {
                            builder.append('\n');
                        }
                        builder.append(text);
                    }
                }
            }
            return builder.toString().trim();
        }
        if (contentObj instanceof Map<?, ?> map) {
            Object parts = map.get("parts");
            if (parts instanceof List<?> items) {
                return extractMessageText(items);
            }
        }
        return String.valueOf(contentObj).trim();
    }

    private Map<String, Object> tryParseJson(String text) {
        String normalizedText = stripMarkdownCodeFence(text);
        Map<String, Object> direct = parseJsonObject(normalizedText);
        if (direct != null) {
            return direct;
        }

        Map<String, Object> extracted = extractStructuredJsonObject(normalizedText);
        if (extracted != null) {
            return extracted;
        }

        log.warn("Gemini returned non-JSON content, using text fallback");
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("hasFace", null);
        fallback.put("faceCount", null);
        fallback.put("visualSummary", normalizedText);
        fallback.put("observedFeatures", List.of());
        fallback.put("physiognomyReport", Map.of(
                "forehead", "",
                "eyesAndBrows", "",
                "nose", "",
                "mouthAndChin", "",
                "overallImpression", ""
        ));
        fallback.put("imageQuality", "");
        fallback.put("reportSummary", normalizedText);
        fallback.put("suggestions", List.of());
        fallback.put("disclaimer", "结果为文本回退模式，仅供文化娱乐参考，不包含身份识别，也不构成事实判断。");
        return fallback;
    }

    private Map<String, Object> parseJsonObject(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, Object> extractStructuredJsonObject(String text) {
        List<String> candidates = extractJsonObjectCandidates(text);
        Map<String, Object> best = null;
        int bestScore = -1;

        for (String candidate : candidates) {
            Map<String, Object> parsed = parseJsonObject(candidate);
            if (parsed == null || parsed.isEmpty()) {
                continue;
            }
            int score = scoreFaceAnalysisPayload(parsed);
            if (score > bestScore) {
                best = parsed;
                bestScore = score;
            }
        }

        return bestScore > 0 ? best : null;
    }

    private List<String> extractJsonObjectCandidates(String text) {
        List<String> candidates = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return candidates;
        }

        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\') {
                escaped = true;
                continue;
            }

            if (current == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (current == '{') {
                if (depth == 0) {
                    start = index;
                }
                depth++;
            } else if (current == '}') {
                if (depth > 0) {
                    depth--;
                    if (depth == 0 && start >= 0) {
                        candidates.add(text.substring(start, index + 1));
                        start = -1;
                    }
                }
            }
        }

        return candidates;
    }

    private int scoreFaceAnalysisPayload(Map<String, Object> payload) {
        int score = 0;
        if (payload.containsKey("hasFace")) {
            score += 2;
        }
        if (payload.containsKey("faceCount")) {
            score += 2;
        }
        if (payload.containsKey("visualSummary")) {
            score += 1;
        }
        if (payload.containsKey("physiognomyReport")) {
            score += 2;
        }
        if (payload.containsKey("reportSummary")) {
            score += 1;
        }
        return score;
    }

    private String stripMarkdownCodeFence(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            if (firstLineBreak >= 0) {
                trimmed = trimmed.substring(firstLineBreak + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }

    private String objectToString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
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

    private String normalizeImageUrl(String value) {
        String trimmed = trimToNull(value);
        if (!StringUtils.hasText(trimmed)) {
            return null;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("data:image/")) {
            return trimmed;
        }
        return null;
    }

    private String extractBase64FromDataUrl(String value) {
        String trimmed = trimToNull(value);
        if (!StringUtils.hasText(trimmed)) {
            return null;
        }
        int commaIndex = trimmed.indexOf(',');
        if (commaIndex < 0 || commaIndex >= trimmed.length() - 1) {
            return null;
        }
        return sanitizeBase64(trimmed.substring(commaIndex + 1));
    }

    private String sanitizeBase64(String rawBase64) {
        if (!StringUtils.hasText(rawBase64)) {
            return null;
        }
        String base64 = rawBase64.trim();
        int commaIndex = base64.indexOf(',');
        if (base64.startsWith("data:") && commaIndex >= 0 && commaIndex < base64.length() - 1) {
            base64 = base64.substring(commaIndex + 1);
        }
        base64 = base64.replaceAll("\\s+", "");
        return StringUtils.hasText(base64) ? base64 : null;
    }

    private String abbreviate(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
    }

    static record SceneImageExecutionPayload(String provider,
                                             String model,
                                             URI uri,
                                             String imageBase64,
                                             String imageUrl,
                                             String revisedPrompt,
                                             String visualSummary,
                                             String negativePrompt,
                                             String displayText,
                                             String generationMode) {
    }

    static record SceneImagePlanPayloadData(String visualSummary,
                                            String revisedPrompt,
                                            String negativePrompt,
                                            String displayText) {
    }

    static record GeneratedImagePayloadData(String imageBase64, String imageUrl, String revisedPrompt) {
    }
}
