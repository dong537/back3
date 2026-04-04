package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeminiResponseParserTest {

    private final GeminiResponseParser parser = new GeminiResponseParser(
            new ObjectMapper(),
            "one-api-chat-completions"
    );

    @Test
    void parseResponseShouldStripMarkdownJsonFence() throws Exception {
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

        Map<String, Object> result = parser.parseResponse(responseBody, "gemini-2.0-flash");

        assertEquals(Boolean.TRUE, result.get("hasFace"));
        assertEquals(1, result.get("faceCount"));
        assertEquals("gemini-2.0-flash", result.get("model"));
    }

    @Test
    void parseSceneImagePlanPayloadShouldExtractPromptOnlyFields() {
        GeminiResponseParser.SceneImagePlanPayloadData payload = parser.parseSceneImagePlanPayload(
                """
                {
                  "visual_summary": "夜色中的行旅人站在桥上，风中带着将变未变的意味。",
                  "revised_prompt": "东方电影感，夜桥，独行者，卦盘微光，竖版构图",
                  "negative_prompt": "卡通, 低清晰度, 多余手指",
                  "display_text": "当前已生成场景方案，可继续交给绘图模型出图。"
                }
                """
        );

        assertEquals("夜色中的行旅人站在桥上，风中带着将变未变的意味。", payload.visualSummary());
        assertEquals("东方电影感，夜桥，独行者，卦盘微光，竖版构图", payload.revisedPrompt());
        assertEquals("卡通, 低清晰度, 多余手指", payload.negativePrompt());
        assertEquals("当前已生成场景方案，可继续交给绘图模型出图。", payload.displayText());
    }

    @Test
    void parseSceneImageResponseResultShouldReturnPromptOnlyPayloadForChatPlan() throws Exception {
        String responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"visual_summary\\":\\"雨夜桥上的独行者\\",\\"revised_prompt\\":\\"东方电影感雨夜桥上独行者，竖版构图\\",\\"negative_prompt\\":\\"低清晰度\\",\\"display_text\\":\\"当前已生成场景方案，可继续交给绘图模型出图。\\"}"
                      }
                    }
                  ]
                }
                """;

        GeminiResponseParser.SceneImageExecutionPayload payload = parser.parseSceneImageResponseResult(
                responseBody,
                "gemini-2.5-flash-image",
                URI.create("https://example.com/v1/chat/completions"),
                "chat-completions"
        );

        assertEquals("one-api-chat-completions", payload.provider());
        assertEquals("gemini-2.5-flash-image", payload.model());
        assertEquals("prompt_only", payload.generationMode());
        assertNull(payload.imageBase64());
        assertNull(payload.imageUrl());
        assertEquals("雨夜桥上的独行者", payload.visualSummary());
        assertEquals("东方电影感雨夜桥上独行者，竖版构图", payload.revisedPrompt());
        assertEquals("低清晰度", payload.negativePrompt());
        assertEquals("当前已生成场景方案，可继续交给绘图模型出图。", payload.displayText());
    }
}
