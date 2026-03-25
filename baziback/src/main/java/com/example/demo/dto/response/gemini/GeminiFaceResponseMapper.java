package com.example.demo.dto.response.gemini;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GeminiFaceResponseMapper {

    private GeminiFaceResponseMapper() {
    }

    public static GeminiFaceAnalysisResponse fromMap(Map<String, Object> source) {
        if (source == null) {
            return GeminiFaceAnalysisResponse.builder().build();
        }

        GeminiPhysiognomyReportResponse report = buildPhysiognomyReport(asMap(source.get("physiognomyReport")));
        List<GeminiObservedFeatureResponse> features = buildObservedFeatures(source.get("observedFeatures"));

        return GeminiFaceAnalysisResponse.builder()
                .hasFace(asBoolean(source.get("hasFace")))
                .faceCount(asInteger(source.get("faceCount")))
                .faceStatusText(buildFaceStatusText(asBoolean(source.get("hasFace")), asInteger(source.get("faceCount"))))
                .visualSummary(asString(source.get("visualSummary")))
                .observedFeatures(features)
                .physiognomyReport(report)
                .detailSections(buildDetailSections(report, asString(source.get("visualSummary")), asString(source.get("reportSummary"))))
                .imageQuality(asString(source.get("imageQuality")))
                .reportSummary(asString(source.get("reportSummary")))
                .suggestions(asStringList(source.get("suggestions")))
                .disclaimer(asString(source.get("disclaimer")))
                .provider(asString(source.get("provider")))
                .model(asString(source.get("model")))
                .rawText(asString(source.get("rawText")))
                .build();
    }

    private static GeminiPhysiognomyReportResponse buildPhysiognomyReport(Map<String, Object> source) {
        return GeminiPhysiognomyReportResponse.builder()
                .forehead(asString(source.get("forehead")))
                .eyesAndBrows(asString(source.get("eyesAndBrows")))
                .nose(asString(source.get("nose")))
                .mouthAndChin(asString(source.get("mouthAndChin")))
                .overallImpression(asString(source.get("overallImpression")))
                .build();
    }

    private static List<GeminiObservedFeatureResponse> buildObservedFeatures(Object source) {
        List<GeminiObservedFeatureResponse> result = new ArrayList<>();
        for (Map<String, Object> item : asMapList(source)) {
            String clarity = asString(item.get("clarity"));
            result.add(GeminiObservedFeatureResponse.builder()
                    .region(asString(item.get("region")))
                    .observation(asString(item.get("observation")))
                    .clarity(clarity)
                    .detailLevel(buildDetailLevel(clarity))
                    .build());
        }
        return result;
    }

    private static List<GeminiNarrativeSectionResponse> buildDetailSections(GeminiPhysiognomyReportResponse report,
                                                                            String visualSummary,
                                                                            String reportSummary) {
        List<GeminiNarrativeSectionResponse> sections = new ArrayList<>();
        addSection(sections, "visualSummary", "照片整体观感", "先从照片中真实可见的面部信息做整体描述。", visualSummary);
        addSection(sections, "forehead", "额头区域解读", "基于额头可见部分，给出传统文化语境下的常见说法。", report.getForehead());
        addSection(sections, "eyesAndBrows", "眉眼区域解读", "围绕眉形、眼神和表情特征做进一步说明。", report.getEyesAndBrows());
        addSection(sections, "nose", "鼻部区域解读", "结合鼻梁、鼻头与鼻翼的可见特征来解释。", report.getNose());
        addSection(sections, "mouthAndChin", "口唇与下巴解读", "从嘴型、笑容、下巴轮廓等角度补充说明。", report.getMouthAndChin());
        addSection(sections, "overallImpression", "整体印象解读", "将局部观察汇总为更完整的传统文化视角印象。", report.getOverallImpression());
        addSection(sections, "reportSummary", "综合总结", "对本次娱乐性分析做克制、完整的总结。", reportSummary);
        return sections;
    }

    private static void addSection(List<GeminiNarrativeSectionResponse> sections,
                                   String key,
                                   String title,
                                   String summary,
                                   String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        sections.add(GeminiNarrativeSectionResponse.builder()
                .key(key)
                .title(title)
                .summary(summary)
                .content(content)
                .build());
    }

    private static String buildFaceStatusText(Boolean hasFace, Integer faceCount) {
        if (Boolean.FALSE.equals(hasFace)) {
            return "未检测到清晰人脸";
        }
        if (Boolean.TRUE.equals(hasFace)) {
            if (faceCount == null) {
                return "已检测到人脸";
            }
            return faceCount == 1 ? "检测到 1 张人脸" : "检测到 " + faceCount + " 张人脸";
        }
        return "人脸状态未明确";
    }

    private static String buildDetailLevel(String clarity) {
        if (clarity == null || clarity.isBlank()) {
            return "未标注";
        }
        return switch (clarity.trim()) {
            case "清晰" -> "该部位辨识度较高，可作为本次说明的重点参考。";
            case "一般" -> "该部位可以观察到主要轮廓，但细节有限，解读会相对保守。";
            case "不清晰" -> "该部位细节不足，结论仅作轻量参考。";
            default -> "该部位已识别到一定信息，具体清晰度以原图为准。";
        };
    }

    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(Objects.toString(key, ""), item));
            return result;
        }
        return new LinkedHashMap<>();
    }

    private static List<Map<String, Object>> asMapList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            result.add(asMap(item));
        }
        return result;
    }

    private static List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.parseBoolean(text.trim());
        }
        return null;
    }
}
