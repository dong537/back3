package com.example.demo.bazi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * 八字DeepSeek AI解读服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BaZiDeepSeekService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.endpoint:https://gemini.agentpit.io/v1/chat/completions}")
    private String apiEndpoint;

    @Value("${deepseek.model:gemini-2.0-flash}")
    private String model;

    @Value("${deepseek.system-prompt:}")
    private String systemPrompt;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(100))
            .build();

    /**
     * 生成八字命理报告：整合系统提示词 + 用户八字数据
     * @param baziData 八字分析数据JSON
     * @return AI生成的命理报告
     */
    public String generateBaziReport(String baziData) throws Exception {
        if (StringUtils.hasText(systemPrompt)) {
            log.info("八字解读：使用系统提示词");
        } else {
            log.warn("八字解读：系统提示词为空，使用默认提示词");
        }

        log.info("开始调用DeepSeek生成八字报告");
        return callDeepSeekWithSystemPrompt(baziData);
    }

    /**
     * 根据八字分析结果生成解读报告
     * @param analysisResult BaZiService分析结果
     * @return AI解读报告
     */
    public String interpretAnalysis(Map<String, Object> analysisResult) throws Exception {
        String jsonData = objectMapper.writeValueAsString(analysisResult);
        return generateBaziReport(jsonData);
    }

    /**
     * 生成八字综合解读（包含特定方面）
     * @param baziData 八字数据
     * @param aspects 需要解读的方面（如：事业、婚姻、财运等）
     * @return AI解读报告
     */
    public String generateAspectReport(String baziData, List<String> aspects) throws Exception {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下八字数据，重点解读以下方面：\n");
        for (String aspect : aspects) {
            prompt.append("- ").append(aspect).append("\n");
        }
        prompt.append("\n八字数据：\n").append(baziData);
        
        return callDeepSeekWithSystemPrompt(prompt.toString());
    }

    /**
     * 调用DeepSeek API（含系统提示词）
     */
    private String callDeepSeekWithSystemPrompt(String userContent) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("AI API Key未配置，请在 application.yml 中设置 deepseek.api.key 或 ONE_API_KEY");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加system提示词
        String sysPrompt = StringUtils.hasText(systemPrompt) 
            ? systemPrompt 
            : getDefaultBaziPrompt();
        messages.add(Map.of("role", "system", "content", sysPrompt));
        
        // 添加user数据
        messages.add(Map.of("role", "user", "content", userContent));
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", false);

        return executeRequest(requestBody);
    }

    /**
     * 调用DeepSeek API（无系统提示词）
     */
    public String callDeepSeekDirect(String userContent) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("AI API Key未配置");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userContent));
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", false);

        return executeRequest(requestBody);
    }

    /**
     * 执行DeepSeek API请求
     */
    @SuppressWarnings("unchecked")
    private String executeRequest(Map<String, Object> requestBody) throws Exception {
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        log.info("发送DeepSeek请求 | 内容长度: {}", requestBodyJson.length());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(100))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("DeepSeek API调用失败 | 状态码: {}, 响应: {}", response.statusCode(), response.body());
            throw new Exception("DeepSeek API调用失败: HTTP " + response.statusCode());
        }

        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new Exception("DeepSeek返回数据格式错误");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
        String content = (String) messageObj.get("content");

        log.info("DeepSeek返回内容长度: {}", content.length());
        return content;
    }

    /**
     * 默认八字解读提示词
     */
    private String getDefaultBaziPrompt() {
        return """
            你是一位精通中国传统命理学的八字大师，专注于四柱八字命理分析。
            
            你的专业领域包括：
            1. 天干地支的生克关系
            2. 十神的含义与作用
            3. 五行的平衡与调候
            4. 大运流年的推算
            5. 喜用神与忌神的判断
            6. 刑冲合会的影响
            7. 神煞的吉凶判断
            
            请根据用户提供的八字数据，给出专业、详细、准确的命理分析。
            分析应包括：性格特点、事业发展、财运状况、婚姻感情、健康状况等方面。
            语言风格要专业但易懂，既有传统命理术语，也有通俗解释。
            """;
    }
}
