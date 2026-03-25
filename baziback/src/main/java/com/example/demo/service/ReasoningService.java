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
     * ✅ 新增：非流式推理 - 一次性返回完整结果
     */
    public String getReasoningResult(String userMessage) throws Exception {
        // 参数验证
        if (userMessage == null || userMessage.trim().isEmpty() || "0".equals(userMessage)) {
            log.warn("收到无效的推理消息，使用默认提示: {}", userMessage);
            userMessage = "请帮我分析一下";
        }
        
        // 检查 API 密钥
        if (apiKey == null || apiKey.isEmpty() || (apiKey.startsWith("sk-") && apiKey.length() < 20)) {
            log.warn("DeepSeek API 密钥未正确配置，使用演示模式");
            return getDemoResponse(userMessage);
        }
        
        // 构建请求
        URL url = new URL(apiEndpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        // 构建请求体
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "deepseek-reasoner");
        requestBody.put("stream", false);  // ✅ 不使用流式
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4000);

        String sysPrompt = (systemPrompt != null && !systemPrompt.isEmpty()) 
            ? systemPrompt 
            : "你是一位精通命理学、易经、八字、塔罗的专家。请详细展示你的思考过程。";
        
        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content", sysPrompt),
            Map.of("role", "user", "content", userMessage)
        );
        requestBody.put("messages", messages);

        // 发送请求
        String jsonRequest = objectMapper.writeValueAsString(requestBody);
        log.info("发送 DeepSeek API 请求到: {}", apiEndpoint);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        int responseCode = conn.getResponseCode();
        log.info("DeepSeek API 响应码: {}", responseCode);
        
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(response.toString(), Map.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    
                    if (message != null) {
                        String content = (String) message.get("content");
                        if (content != null) {
                            log.info("推理完成，结果长度: {} 字符", content.length());
                            return content;
                        }
                    }
                }
            }
        } else {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                log.error("DeepSeek API 错误响应: {}", errorResponse.toString());
            }
        }

        conn.disconnect();
        return "推理失败，请稍后重试";
    }

    /**
     * 演示模式响应
     */
    private String getDemoResponse(String userMessage) {
        return "根据您的问题，以下是我的分析结果：\n\n" +
                "1 核心观点\n" +
                "这是一个很有意思的问题。通过深入分析，我发现了几个关键要点。\n\n" +
                "2 详细分析\n" +
                "首先，从理论角度来看，这涉及到多个方面的考虑。其次，从实践角度来看，我们需要考虑具体的应用场景。\n\n" +
                "3 建议\n" +
                "基于以上分析，我建议您采取以下措施：\n" +
                "第一步：了解基本原理\n" +
                "第二步：结合实际情况\n" +
                "第三步：持续优化调整\n\n" +
                "4 总结\n" +
                "总的来说，这个问题的解决需要综合考虑多个因素。希望以上分析对您有所帮助。";
    }

    /**
     * 流式推理响应
     * 模拟 DeepSeek-R1 的推理过程输出
     */
    public Flux<String> streamReasoningResponse(String userMessage) {
        // 参数验证
        if (userMessage == null || userMessage.trim().isEmpty() || "0".equals(userMessage)) {
            log.warn("收到无效的推理消息，使用默认提示: {}", userMessage);
            userMessage = "请帮我分析一下";
        }
        
        final String finalMessage = userMessage;
        
        return Flux.<String>create(sink -> {
            try {
                // 检查 API 密钥是否配置
                if (apiKey == null || apiKey.isEmpty() || (apiKey.startsWith("sk-") && apiKey.length() < 20)) {
                    log.warn("DeepSeek API 密钥未正确配置，使用演示模式");
                    streamDemoResponse(sink, finalMessage);
                    return;
                }
                
                // 构建请求
                URL url = new URL(apiEndpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                // 构建请求体 - 使用 LinkedHashMap 保持顺序
                Map<String, Object> requestBody = new LinkedHashMap<>();
                requestBody.put("model", "deepseek-reasoner");
                requestBody.put("stream", true);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 4000);

                // 构建消息 - 使用 Map.of 创建不可变 Map
                String sysPrompt = (systemPrompt != null && !systemPrompt.isEmpty()) 
                    ? systemPrompt 
                    : "你是一位精通命理学、易经、八字、塔罗的专家。请详细展示你的思考过程。";
                
                List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", sysPrompt),
                    Map.of("role", "user", "content", finalMessage)
                );
                requestBody.put("messages", messages);

                // 发送请求
                String jsonRequest = objectMapper.writeValueAsString(requestBody);
                log.info("发送 DeepSeek API 请求到: {}", apiEndpoint);
                
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                // 清理请求体，释放内存
                requestBody.clear();
                messages = null;

                // 读取流式响应
                int responseCode = conn.getResponseCode();
                log.info("DeepSeek API 响应码: {}", responseCode);
                
                if (responseCode == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        
                        String line;
                        boolean sentStop = false;
                        int lineCount = 0;
                        int dataCount = 0;
                        
                        while ((line = reader.readLine()) != null) {
                            lineCount++;
                            
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                
                                if (data.equals("[DONE]")) {
                                    log.info("收到 [DONE] 标记");
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
                                        String finishReason = (String) choice.get("finish_reason");
                                        
                                        if (delta != null) {
                                            String content = (String) delta.get("content");
                                            String reasoningContent = (String) delta.get("reasoning_content");
                                            
                                            // 构建响应数据
                                            Map<String, Object> response = new HashMap<>();
                                            
                                            if (reasoningContent != null && !reasoningContent.isEmpty()) {
                                                response.put("reasoning_content", reasoningContent);
                                            }
                                            
                                            if (content != null && !content.isEmpty()) {
                                                response.put("content", content);
                                            }
                                            
                                            // 发送 SSE 格式数据（只有有内容时才发送）
                                            if (!response.isEmpty()) {
                                                sink.next(toSseJson(response));
                                                dataCount++;
                                            }
                                        }
                                        
                                        // 检查完成标记
                                        if ("stop".equals(finishReason) && !sentStop) {
                                            log.info("收到 stop 标记");
                                            sink.next(toSseJson(Map.of("finish_reason", "stop")));
                                            sentStop = true;
                                            sink.next("data: [DONE]\n\n");
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    log.debug("解析流式响应失败: {}", e.getMessage());
                                }
                            }
                        }
                        
                        log.info("流式响应读取完成，共 {} 行，发送 {} 条数据", lineCount, dataCount);
                    }
                } else {
                    // 错误处理
                    log.error("DeepSeek API 返回错误状态码: {}", responseCode);
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        log.error("DeepSeek API 错误响应: {}", errorResponse.toString());
                        
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
        String json = objectMapper.writeValueAsString(payload);
        // 直接返回 SSE 格式，避免中间字符串拼接
        return "data: " + json + "\n\n";
    }

    /**
     * 演示模式：返回模拟的思维链和分析结果
     */
    private void streamDemoResponse(reactor.core.publisher.FluxSink<String> sink, String userMessage) throws Exception {
        log.info("使用演示模式响应用户问题");
        
        // 使用 StringBuilder 优化字符串拼接
        StringBuilder thinkingBuilder = new StringBuilder();
        thinkingBuilder.append("让我分析一下这个问题...\n\n")
                .append("首先，我需要理解用户的具体需求。\n")
                .append("用户要求：").append(userMessage).append("\n\n")
                .append("我将从多个角度进行分析...\n")
                .append("1. 理论基础分析\n")
                .append("2. 实际应用场景\n")
                .append("3. 具体建议\n\n")
                .append("现在开始详细分析...");
        
        String thinkingContent = thinkingBuilder.toString();
        
        // 立即发送思维链（不使用 Thread.sleep）
        Map<String, Object> thinkingData = new HashMap<>();
        thinkingData.put("reasoning_content", thinkingContent);
        sink.next(toSseJson(thinkingData));
        log.debug("发送思维链内容: {} 字符", thinkingContent.length());
        
        // 模拟最终分析结果
        StringBuilder analysisBuilder = new StringBuilder();
        analysisBuilder.append("根据您的问题，以下是我的分析结果：\n\n")
                .append("1 核心观点\n")
                .append("这是一个很有意思的问题。通过深入分析，我发现了几个关键要点。\n\n")
                .append("2 详细分析\n")
                .append("首先，从理论角度来看，这涉及到多个方面的考虑。其次，从实践角度来看，我们需要考虑具体的应用场景。\n\n")
                .append("3 建议\n")
                .append("基于以上分析，我建议您采取以下措施：\n")
                .append("第一步：了解基本原理\n")
                .append("第二步：结合实际情况\n")
                .append("第三步：持续优化调整\n\n")
                .append("4 总结\n")
                .append("总的来说，这个问题的解决需要综合考虑多个因素。希望以上分析对您有所帮助。");
        
        String analysisResult = analysisBuilder.toString();
        
        // 立即发送分析结果（不使用 Thread.sleep）
        Map<String, Object> contentData = new HashMap<>();
        contentData.put("content", analysisResult);
        sink.next(toSseJson(contentData));
        log.debug("发送分析结果: {} 字符", analysisResult.length());
        
        // 发送完成标记
        Map<String, Object> stopData = new HashMap<>();
        stopData.put("finish_reason", "stop");
        sink.next(toSseJson(stopData));
        sink.next("data: [DONE]\n\n");
        log.info("演示模式响应完成");
        sink.complete();
    }
}
