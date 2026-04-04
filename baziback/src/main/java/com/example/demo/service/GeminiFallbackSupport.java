package com.example.demo.service;

import com.example.demo.dto.response.gemini.GeminiFailureDetails;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;

final class GeminiFallbackSupport {

    boolean shouldTryNextVisionModel(int statusCode, String responseBody) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (statusCode >= 500 || statusCode == 429) {
            return true;
        }
        return body.contains("model_not_found")
                || body.contains("model not found")
                || body.contains("not support image")
                || body.contains("does not support image")
                || body.contains("unsupported image")
                || body.contains("invalid image")
                || body.contains("content part type image_url is invalid")
                || body.contains("invalid content type")
                || body.contains("generationconfig.responsemodalities")
                || body.contains("responsemodalities")
                || body.contains("image_url");
    }

    boolean shouldTryNextSceneImageModel(int statusCode, String responseBody) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (body.contains("invalid token") || statusCode == 401) {
            return false;
        }
        return shouldTryNextVisionModel(statusCode, responseBody);
    }

    String appendAttemptedModels(String message, List<String> attemptedModels, boolean hasNextModel) {
        if (hasNextModel || attemptedModels == null || attemptedModels.isEmpty()) {
            return message;
        }
        return message + " | attemptedModels=" + String.join(",", attemptedModels);
    }

    GeminiFailureDetails buildFailureDetails(List<String> attemptedModels,
                                             String lastModel,
                                             Integer lastStatus,
                                             String lastPayloadFormat,
                                             URI requestUri) {
        return GeminiFailureDetails.builder()
                .attemptedModels(attemptedModels)
                .lastModel(lastModel)
                .lastStatus(lastStatus)
                .lastPayloadFormat(lastPayloadFormat)
                .uri(requestUri == null ? null : requestUri.toString())
                .build();
    }

    String buildFailureMessage(int statusCode, String responseBody, String targetModel, URI requestUri) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (body.contains("invalid url")) {
            return "One-API base URL 配置无效（model=" + targetModel + ", uri=" + requestUri + "），请检查 ONE_API_BASE_URL";
        }
        if (body.contains("invalid token") || body.contains("无效的令牌") || statusCode == 401) {
            return "One-API key 无效或已过期，请检查 ONE_API_KEY";
        }
        if (statusCode == 429) {
            return "One-API 触发限流（model=" + targetModel + "），请稍后重试";
        }
        if (statusCode >= 500) {
            return "One-API / Gemini 上游服务暂时异常（model=" + targetModel + ", HTTP " + statusCode + "），请稍后重试";
        }
        return "Gemini 服务调用失败（model=" + targetModel + ", HTTP " + statusCode + "）";
    }

    String buildSecondStageSceneImageFailureMessage(int statusCode,
                                                    String responseBody,
                                                    String targetModel,
                                                    URI requestUri,
                                                    String protocol) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (body.contains("invalid url")) {
            return "第二绘图接口 base URL 配置无效（protocol=" + protocol + ", model=" + targetModel + ", uri=" + requestUri + "），请检查 SCENE_IMAGE_API_BASE_URL";
        }
        if (body.contains("invalid token") || body.contains("无效的令牌") || statusCode == 401) {
            return "第二绘图接口 key 无效或已过期，请检查 SCENE_IMAGE_API_KEY";
        }
        if (body.contains("model_not_found") || body.contains("model not found")) {
            return "第二绘图接口模型不存在（protocol=" + protocol + ", model=" + targetModel + "）";
        }
        if (body.contains("no candidates returned")) {
            return "第二绘图接口没有返回可用图片结果（protocol=" + protocol + ", model=" + targetModel + "）";
        }
        if (statusCode == 429) {
            return "第二绘图接口触发限流，请稍后重试";
        }
        if (statusCode >= 500) {
            return "第二绘图接口上游服务暂时异常（protocol=" + protocol + ", model=" + targetModel + ", HTTP " + statusCode + "）";
        }
        return "第二绘图接口调用失败（protocol=" + protocol + ", model=" + targetModel + ", HTTP " + statusCode + "）";
    }

    String buildSceneImageFailureMessage(int statusCode, String responseBody, String targetModel, URI requestUri) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (body.contains("invalid url")) {
            return "YMAPI/One-API base URL 配置无效（model=" + targetModel + ", uri=" + requestUri + "），请检查 ONE_API_BASE_URL";
        }
        if (body.contains("invalid token") || body.contains("无效的令牌") || statusCode == 401) {
            return "YMAPI/One-API key 无效或已过期，请检查 ONE_API_KEY";
        }
        if (body.contains("model_not_found") || body.contains("model not found")) {
            return "场景图模型不存在，请检查 SCENE_IMAGE_MODEL 或 ONE_API_GEMINI_MODEL 是否为该 YMAPI 通道支持的 chat/completions 模型别名";
        }
        if (body.contains("no candidates returned")) {
            return "当前 YMAPI 通道没有返回图片或场景方案，请检查所选模型是否支持通过 chat/completions 输出图像内容";
        }
        if (statusCode == 429) {
            return "场景图通道触发限流，请稍后重试";
        }
        if (statusCode >= 500) {
            return "场景图上游服务暂时异常（model=" + targetModel + ", HTTP " + statusCode + "），请稍后重试";
        }
        return "场景图生成失败（model=" + targetModel + ", HTTP " + statusCode + "）";
    }

    HttpStatus mapUpstreamFailureStatus(int statusCode, String responseBody) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (body.contains("invalid token") || statusCode == 401) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (statusCode == 429) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (statusCode >= 500) {
            return HttpStatus.BAD_GATEWAY;
        }
        return HttpStatus.BAD_REQUEST;
    }

    boolean hasMoreSecondStageCandidates(List<String> protocols,
                                         List<String> models,
                                         String currentProtocol,
                                         String currentModel) {
        int protocolIndex = protocols.indexOf(currentProtocol);
        int modelIndex = models.indexOf(currentModel);
        if (protocolIndex < 0 || modelIndex < 0) {
            return false;
        }
        return modelIndex < models.size() - 1 || protocolIndex < protocols.size() - 1;
    }
}
