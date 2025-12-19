package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 易经推理服务 - 支持流式思维链输出
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class YijingReasoningService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.endpoint:https://api.deepseek.com/v1/chat/completions}")
    private String apiEndpoint;

    private final ObjectMapper objectMapper;

    /**
     * 流式易经占卜推理响应
     */
    public Flux<String> streamYijingDivination(String question, String method, String hexagram) {
        return Flux.<String>create(sink -> {
            try {
                URL url = new URL(apiEndpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);

                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "deepseek-reasoner");
                requestBody.put("stream", true);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 4000);

                // 构建消息
                List<Map<String, String>> messages = new ArrayList<>();
                
                // 系统提示词 - 易经专家
                String systemPrompt = buildYijingSystemPrompt(method);
                messages.add(Map.of("role", "system", "content", systemPrompt));
                
                // 用户问题
                String userContent = buildYijingUserPrompt(question, method, hexagram);
                messages.add(Map.of("role", "user", "content", userContent));
                
                requestBody.put("messages", messages);

                // 发送请求
                String jsonRequest = objectMapper.writeValueAsString(requestBody);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // 读取流式响应
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        
                        String line;
                        boolean sentStop = false;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                
                                if (data.equals("[DONE]")) {
                                    if (!sentStop) {
                                        sink.next(toSseJson(Map.of("finish_reason", "stop")));
                                        sentStop = true;
                                    }
                                    sink.next("data: [DONE]\n\n");
                                    break;
                                }

                                try {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                                    
                                    if (choices != null && !choices.isEmpty()) {
                                        Map<String, Object> choice = choices.get(0);
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                        
                                        if (delta != null) {
                                            String content = (String) delta.get("content");
                                            String reasoningContent = (String) delta.get("reasoning_content");
                                            String finishReason = (String) choice.get("finish_reason");
                                            
                                            // 构建响应数据
                                            Map<String, Object> response = new HashMap<>();
                                            
                                            if (reasoningContent != null && !reasoningContent.isEmpty()) {
                                                response.put("reasoning_content", reasoningContent);
                                            }
                                            
                                            if (content != null && !content.isEmpty()) {
                                                response.put("content", content);
                                            }
                                            
                                            if (finishReason != null) {
                                                response.put("finish_reason", finishReason);
                                            }
                                            
                                            // 发送 SSE 格式数据
                                            if (!response.isEmpty()) {
                                                sink.next(toSseJson(response));
                                            }

                                            if ("stop".equals(finishReason) && !sentStop) {
                                                sink.next(toSseJson(Map.of("finish_reason", "stop")));
                                                sentStop = true;
                                                sink.next("data: [DONE]\n\n");
                                                break;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // 上游偶发的非 JSON 片段/心跳，不中断流
                                    log.debug("解析流式响应失败: {}", e.getMessage());
                                }
                            }
                        }
                    }
                } else {
                    // 错误处理
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        log.error("DeepSeek API 错误: {}", errorResponse.toString());
                        
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("content", "抱歉，易经解读服务暂时不可用。错误码: " + responseCode);
                        errorData.put("finish_reason", "error");
                        
                        sink.next(toSseJson(errorData));
                        sink.next("data: [DONE]\n\n");
                    }
                }

                sink.complete();
                conn.disconnect();

            } catch (Exception e) {
                log.error("易经流式推理请求失败", e);
                
                try {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("content", "易经解读服务异常: " + e.getMessage());
                    errorData.put("finish_reason", "error");
                    
                    sink.next(toSseJson(errorData));
                    sink.next("data: [DONE]\n\n");
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
                
                sink.complete();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String toSseJson(Map<String, Object> payload) throws Exception {
        return "data: " + objectMapper.writeValueAsString(payload) + "\n\n";
    }

    /**
     * 构建易经系统提示词
     */
    private String buildYijingSystemPrompt(String method) {
        return String.format("""
            你是一位精通《周易》的易经大师，深谙六十四卦的卦辞、爻辞和象传。
            
            当前使用的起卦方式是：%s
            
            请按照以下步骤进行解读：
            1. 分析卦象的基本含义（卦名、卦辞、卦象）
            2. 解读各爻的爻辞和变化
            3. 结合问题背景，给出针对性的分析
            4. 提供具体的建议和行动指引
            
            请展示你的完整思考过程，让求卦者理解你的推理逻辑。
            最后给出清晰、实用的建议。
            """, getMethodName(method));
    }

    /**
     * 构建用户提示词
     */
    private String buildYijingUserPrompt(String question, String method, String hexagram) {
        return String.format("""
            问题：%s
            
            起卦方式：%s
            卦象编码：%s
            
            请为我解读这次易经占卜，展示你的思考过程，并给出具体建议。
            """, question, getMethodName(method), hexagram);
    }

    /**
     * 获取起卦方式名称
     */
    private String getMethodName(String method) {
        Map<String, String> methodNames = Map.of(
            "random", "随机起卦",
            "time", "时间起卦",
            "number", "数字起卦",
            "plum_blossom", "梅花易数"
        );
        return methodNames.getOrDefault(method, method);
    }
}
