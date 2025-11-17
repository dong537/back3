package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * DeepSeek AI服务：整合系统提示词与用户八字数据（含完整日志调试）
 */
@Service
@Slf4j
public class DeepSeekService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.endpoint:https://api.deepseek.com/v1/chat/completions}")
    private String apiEndpoint;

    @Value("${deepseek.system-prompt:}")
    private String systemPrompt; // 注入系统提示词

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeepSeekService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成八字命理报告：整合系统提示词 + 用户原始数据
     */
    public String generateBaziReport(String userData) throws Exception {
        // 打印**完整系统提示词**（便于调试配置是否正确）
        if (StringUtils.hasText(systemPrompt)) {
            log.info("当前加载的完整系统提示词：\n{}", systemPrompt);
        } else {
            log.warn("系统提示词为空，请检查 application.yml 配置！");
        }

        log.info("开始调用DeepSeek生成八字报告（含系统提示词）");
        return callDeepSeekAPI(userData);
    }

    /**
     * 调用DeepSeek API核心逻辑：构建多角色消息体
     */
    private String callDeepSeekAPI(String userData) throws Exception {
        // 校验API密钥
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("DeepSeek API Key未配置，请在 application.yml 中设置 deepseek.api.key 或通过环境变量 DEEPSEEK_API_KEY 提供");
        }

        // 构建请求体（包含system提示词和user数据）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");

        List<Map<String, String>> messages = new ArrayList<>();
        // 1. 添加system角色提示词（定义AI规则）
        if (StringUtils.hasText(systemPrompt)) {
            messages.add(Map.of(
                    "role", "system",
                    "content", systemPrompt
            ));
        }
        // 2. 添加user角色数据（用户八字信息）
        messages.add(Map.of(
                "role", "user",
                "content", userData
        ));

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        requestBody.put("temperature", 0.5);
        requestBody.put("stream", false);

        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        // 打印**完整请求体JSON**（便于调试接口参数）
        log.info("发送给DeepSeek的完整请求体：\n{}", requestBodyJson);

        // 发送HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        log.info("发送DeepSeek请求 | 用户数据长度: {}", userData.length());

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 处理响应状态
        if (response.statusCode() != 200) {
            log.error("DeepSeek API调用失败 | 状态码: {}, 响应: {}", response.statusCode(), response.body());
            throw new Exception("DeepSeek API调用失败: HTTP " + response.statusCode());
        }

        // 解析响应内容
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
}