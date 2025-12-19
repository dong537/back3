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
 * 推理服务 - 支持流式思维链输出
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReasoningService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.endpoint:https://api.deepseek.com/v1/chat/completions}")
    private String apiEndpoint;

    @Value("${deepseek.system-prompt:}")
    private String systemPrompt;

    private final ObjectMapper objectMapper;

    /**
     * 流式推理响应
     * 模拟 DeepSeek-R1 的推理过程输出
     */
    public Flux<String> streamReasoningResponse(String userMessage) {
        return Flux.<String>create(sink -> {
            try {
                // 构建请求
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
                
                // 系统提示词
                String sysPrompt = (systemPrompt != null && !systemPrompt.isEmpty()) 
                    ? systemPrompt 
                    : "你是一位精通命理学、易经、八字、塔罗的专家。请详细展示你的思考过程。";
                
                messages.add(Map.of("role", "system", "content", sysPrompt));
                messages.add(Map.of("role", "user", "content", userMessage));
                
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
                                    Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                                    
                                    if (choices != null && !choices.isEmpty()) {
                                        Map<String, Object> choice = choices.get(0);
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
                                                // Aipyqchat 风格：显式 stop + [DONE]
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
                        errorData.put("content", "抱歉，推理服务暂时不可用。错误码: " + responseCode);
                        errorData.put("finish_reason", "error");
                        
                        sink.next(toSseJson(errorData));
                        sink.next("data: [DONE]\n\n");
                    }
                }

                sink.complete();
                conn.disconnect();

            } catch (Exception e) {
                log.error("流式推理请求失败", e);
                
                try {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("content", "推理服务异常: " + e.getMessage());
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
}
