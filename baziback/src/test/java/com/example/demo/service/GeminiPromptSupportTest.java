package com.example.demo.service;

import com.example.demo.dto.request.yijing.YijingSceneImageRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiPromptSupportTest {

    private static final String DEFAULT_PROMPT = "default cultural face reading prompt";

    @Test
    void resolveSceneCategoryShouldMatchChineseCareerKeyword() {
        GeminiPromptSupport support = new GeminiPromptSupport(DEFAULT_PROMPT);

        String category = support.resolveSceneCategory("我最近的事业会怎么发展", "想看工作变化");

        assertEquals("career turning point", category);
    }

    @Test
    void buildSecondStageDrawingPromptShouldAppendNegativePrompt() {
        GeminiPromptSupport support = new GeminiPromptSupport(DEFAULT_PROMPT);

        String prompt = support.buildSecondStageDrawingPrompt(
                "东方电影感夜桥独行者",
                "卡通, 低清晰度, 多余手指",
                "fallback"
        );

        assertEquals("东方电影感夜桥独行者\n\nAvoid elements: 卡通, 低清晰度, 多余手指", prompt);
    }

    @Test
    void buildYijingSceneImagePromptShouldIncludeCategoryAndStructuredFields() {
        GeminiPromptSupport support = new GeminiPromptSupport(DEFAULT_PROMPT);
        YijingSceneImageRequest request = YijingSceneImageRequest.builder()
                .question("近期财运怎么样")
                .method("coins")
                .interpretation("财运正在变化，需要观察机会与风险。")
                .interpretationHint("重点看是否适合投资。")
                .changingLines(List.of(2, 5))
                .original(YijingSceneImageRequest.HexagramSnapshot.builder()
                        .name("Qian")
                        .chinese("乾")
                        .meaning("元亨利贞")
                        .image("天行健")
                        .keywords(List.of("主动", "机会"))
                        .applications(Map.of("wealth", "宜把握机会"))
                        .build())
                .changed(YijingSceneImageRequest.HexagramSnapshot.builder()
                        .name("Tai")
                        .chinese("泰")
                        .build())
                .build();

        String prompt = support.buildYijingSceneImagePrompt(request);

        assertTrue(prompt.contains("Scene category: wealth opportunity"));
        assertTrue(prompt.contains("\"visual_summary\": \"...\""));
        assertTrue(prompt.contains("Question: 近期财运怎么样"));
        assertTrue(prompt.contains("Style suggestion:"));
    }
}
