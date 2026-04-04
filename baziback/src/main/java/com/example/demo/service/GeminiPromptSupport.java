package com.example.demo.service;

import com.example.demo.dto.request.yijing.YijingSceneImageRequest;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

final class GeminiPromptSupport {

    private final String defaultFaceCulturalPrompt;

    GeminiPromptSupport(String defaultFaceCulturalPrompt) {
        this.defaultFaceCulturalPrompt = defaultFaceCulturalPrompt;
    }

    String buildPrompt(String userPrompt) {
        String safePrompt = StringUtils.hasText(userPrompt)
                ? userPrompt.trim()
                : "Please analyze only visible facial features and return a cultural-entertainment reading in JSON.";

        return """
                You are a traditional face-reading culture assistant.
                Only describe visible facial features from the uploaded image.
                Do not identify the person or infer sensitive traits as facts.
                Return strict JSON with these keys:
                {
                  "hasFace": true,
                  "faceCount": 1,
                  "visualSummary": "...",
                  "observedFeatures": [{"region":"...", "observation":"...", "clarity":"clear/partial/unclear"}],
                  "physiognomyReport": {
                    "forehead": "...",
                    "eyesAndBrows": "...",
                    "nose": "...",
                    "mouthAndChin": "...",
                    "overallImpression": "..."
                  },
                  "imageQuality": "...",
                  "reportSummary": "...",
                  "suggestions": ["...", "..."],
                  "disclaimer": "This is a cultural-entertainment reading, not a factual judgment."
                }
                User request:
                %s
                """.formatted(safePrompt);
    }

    String buildEnhancedPrompt(String userPrompt) {
        return buildPrompt(StringUtils.hasText(userPrompt) ? userPrompt.trim() : defaultFaceCulturalPrompt);
    }

    String buildYijingSceneImagePrompt(YijingSceneImageRequest request) {
        String sceneCategory = resolveSceneCategory(request.getQuestion(), request.getInterpretation());
        String sceneSuggestion = resolveSceneSuggestion(sceneCategory);
        String originalName = resolveHexagramName(request.getOriginal());
        String changedName = request.getChanged() == null ? "" : resolveHexagramName(request.getChanged());
        String keywords = request.getOriginal() == null || request.getOriginal().getKeywords() == null
                ? ""
                : String.join(", ", request.getOriginal().getKeywords());
        String changingLines = request.getChangingLines() == null || request.getChangingLines().isEmpty()
                ? "No changing lines."
                : "Changing lines: " + request.getChangingLines().stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + ", " + b)
                .orElse("") + ".";
        String changedClause = StringUtils.hasText(changedName)
                ? "Changed hexagram reference: " + changedName + "."
                : "No changed hexagram reference.";

        return """
                You are calling an OpenAI-compatible chat.completions image channel for an I Ching result page.
                If the current channel can directly output an image, return the image result.
                If it cannot output an image, return strict JSON with these keys only:
                {
                  "visual_summary": "...",
                  "revised_prompt": "...",
                  "negative_prompt": "...",
                  "display_text": "..."
                }
                Question: %s
                Scene category: %s
                Original hexagram: %s
                Meaning reference: %s
                Image reference: %s
                Keywords: %s
                Interpretation summary: %s
                Hint: %s
                %s
                %s

                Visual requirements:
                1. Eastern mystical cinematic atmosphere.
                2. Keep the scene grounded in the user's real-life context: %s.
                3. Add hexagram-inspired symbolic elements subtly.
                4. No text, watermark, UI, logo, border, or QR code.
                5. Suitable for a vertical mobile composition.
                6. Avoid horror, distorted limbs, extra fingers, and low detail.
                Style suggestion: %s
                """.formatted(
                normalizePromptText(request.getQuestion(), 100),
                sceneCategory,
                normalizePromptText(originalName, 40),
                normalizePromptText(extractHexagramMeaning(request.getOriginal()), 180),
                normalizePromptText(extractHexagramImage(request.getOriginal()), 160),
                normalizePromptText(keywords, 80),
                normalizePromptText(request.getInterpretation(), 360),
                normalizePromptText(request.getInterpretationHint(), 120),
                changingLines,
                changedClause,
                normalizePromptText(sceneSuggestion, 60),
                normalizePromptText(resolveStyleSuggestion(sceneCategory), 180)
        );
    }

    String resolveSceneCategory(String question, String interpretation) {
        String normalized = (objectToString(question) + " " + objectToString(interpretation)).toLowerCase();
        if (normalized.contains("wealth")
                || normalized.contains("money")
                || normalized.contains("business")
                || normalized.contains("investment")
                || normalized.contains("\u8d22")
                || normalized.contains("\u94b1")
                || normalized.contains("\u751f\u610f")) {
            return "wealth opportunity";
        }
        if (normalized.contains("career")
                || normalized.contains("job")
                || normalized.contains("work")
                || normalized.contains("promotion")
                || normalized.contains("\u4e8b\u4e1a")
                || normalized.contains("\u5de5\u4f5c")
                || normalized.contains("\u804c\u573a")) {
            return "career turning point";
        }
        if (normalized.contains("relationship")
                || normalized.contains("love")
                || normalized.contains("marriage")
                || normalized.contains("partner")
                || normalized.contains("\u611f\u60c5")
                || normalized.contains("\u604b\u7231")
                || normalized.contains("\u5a5a\u59fb")) {
            return "relationship crossroads";
        }
        if (normalized.contains("study")
                || normalized.contains("exam")
                || normalized.contains("school")
                || normalized.contains("learning")
                || normalized.contains("\u8003\u8bd5")
                || normalized.contains("\u5b66\u4e60")
                || normalized.contains("\u5b66\u4e1a")) {
            return "study and growth";
        }
        if (normalized.contains("health")
                || normalized.contains("recovery")
                || normalized.contains("body")
                || normalized.contains("\u5065\u5eb7")
                || normalized.contains("\u8eab\u4f53")
                || normalized.contains("\u6062\u590d")) {
            return "healing and balance";
        }
        return "life atmosphere";
    }

    String buildSecondStageDrawingPrompt(String revisedPrompt, String negativePrompt, String originalPrompt) {
        String basePrompt = StringUtils.hasText(revisedPrompt) ? revisedPrompt.trim() : originalPrompt;
        String avoidPrompt = trimToNull(negativePrompt);
        if (!StringUtils.hasText(avoidPrompt)) {
            return basePrompt;
        }
        return basePrompt + "\n\nAvoid elements: " + avoidPrompt;
    }

    private String resolveSceneSuggestion(String sceneCategory) {
        return switch (sceneCategory) {
            case "wealth opportunity" -> "a restrained but vivid urban prosperity scene with momentum and choice";
            case "career turning point" -> "a workplace or journey scene with pressure, direction, and a visible next step";
            case "relationship crossroads" -> "an emotionally tense interpersonal scene with warmth, distance, and unresolved movement";
            case "study and growth" -> "a quiet study or transition scene with focus, patience, and gradual clarity";
            case "healing and balance" -> "a calm restorative scene with breathing room, light, and grounded stillness";
            default -> "a contemporary life scene with subtle mystery, tension, and turning-point energy";
        };
    }

    private String resolveStyleSuggestion(String sceneCategory) {
        return switch (sceneCategory) {
            case "wealth opportunity" -> "cinematic night city light, refined gold accents, restrained prosperity, realistic atmosphere";
            case "career turning point" -> "rain-soaked streets, office glow, transit motion, practical clothing, decisive framing";
            case "relationship crossroads" -> "moody warm-cool contrast, eye-level composition, emotional distance, subtle body language";
            case "study and growth" -> "quiet desk, window light, paper texture, layered shadows, contemplative rhythm";
            case "healing and balance" -> "soft daylight, natural textures, still water or breeze, low-pressure restorative mood";
            default -> "eastern mystical cinema, realistic detail, layered light, subtle symbolic texture";
        };
    }

    private String resolveHexagramName(YijingSceneImageRequest.HexagramSnapshot hexagram) {
        if (hexagram == null) {
            return "";
        }
        return firstNonBlank(hexagram.getChinese(), hexagram.getName(), hexagram.getSymbol());
    }

    private String extractHexagramMeaning(YijingSceneImageRequest.HexagramSnapshot hexagram) {
        if (hexagram == null) {
            return "";
        }
        String direct = firstNonBlank(hexagram.getMeaning(), hexagram.getJudgment());
        if (StringUtils.hasText(direct)) {
            return direct;
        }
        if (hexagram.getApplications() != null && !hexagram.getApplications().isEmpty()) {
            return String.join("; ", hexagram.getApplications().values());
        }
        return "";
    }

    private String extractHexagramImage(YijingSceneImageRequest.HexagramSnapshot hexagram) {
        if (hexagram == null) {
            return "";
        }
        String direct = firstNonBlank(hexagram.getImage(), hexagram.getSymbol());
        if (StringUtils.hasText(direct)) {
            return direct;
        }
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(hexagram.getElement())) {
            parts.add(hexagram.getElement().trim());
        }
        if (StringUtils.hasText(hexagram.getSeason())) {
            parts.add(hexagram.getSeason().trim());
        }
        if (StringUtils.hasText(hexagram.getDirection())) {
            parts.add(hexagram.getDirection().trim());
        }
        return String.join(", ", parts);
    }

    private String normalizePromptText(String value, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String objectToString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
