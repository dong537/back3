package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
final class GeminiSceneImageExecutor {

    @FunctionalInterface
    interface ProtocolAwareBodyFactory {
        Map<String, Object> build(String modelName, String prompt, String protocol);
    }

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TokenTracker tokenTracker;
    private final GeminiResponseParser responseParser;
    private final GeminiFallbackSupport fallbackSupport;
    private final String providerName;

    GeminiSceneImageExecutor(HttpClient httpClient,
                             ObjectMapper objectMapper,
                             TokenTracker tokenTracker,
                             GeminiResponseParser responseParser,
                             GeminiFallbackSupport fallbackSupport,
                             String providerName) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.tokenTracker = tokenTracker;
        this.responseParser = responseParser;
        this.fallbackSupport = fallbackSupport;
        this.providerName = providerName;
    }

    GeminiResponseParser.SceneImageExecutionPayload executeFirstStage(FirstStageRequest request) throws Exception {
        List<String> attemptedModels = new ArrayList<>();
        BusinessException lastBusinessException = null;

        for (String candidateModel : request.modelsToTry()) {
            attemptedModels.add(candidateModel);
            Map<String, Object> requestBody = request.requestBodyFactory().apply(candidateModel, request.prompt());
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(request.requestUri())
                    .header("Content-Type", "application/json")
                    .headers(request.authorizationHeaders())
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            log.info("Calling scene image generation | provider={}, protocol={}, model={}, uri={}",
                    providerName, request.protocol(), candidateModel, request.requestUri());

            LocalDateTime sceneCallStart = LocalDateTime.now();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                tokenTracker.trackFromResponse(response.body(), candidateModel, "gemini-scene-image", sceneCallStart);
                try {
                    return responseParser.parseSceneImageResponseResult(
                            response.body(),
                            candidateModel,
                            request.requestUri(),
                            request.protocol()
                    );
                } catch (BusinessException parseException) {
                    boolean hasNextModel = attemptedModels.size() < request.modelsToTry().size();
                    log.warn(
                            "Scene image generation returned 200 but no usable content | model={}, uri={}, reason={}, hasNextModel={}, rawBody={}",
                            candidateModel,
                            request.requestUri(),
                            parseException.getMessage(),
                            hasNextModel,
                            abbreviate(response.body())
                    );
                    lastBusinessException = new BusinessException(
                            fallbackSupport.appendAttemptedModels(parseException.getMessage(), attemptedModels, hasNextModel),
                            parseException.getStatus(),
                            fallbackSupport.buildFailureDetails(
                                    attemptedModels,
                                    candidateModel,
                                    200,
                                    "scene-image:" + request.protocol() + ":parse",
                                    request.requestUri()
                            )
                    );
                    if (!hasNextModel) {
                        throw lastBusinessException;
                    }
                    continue;
                }
            }

            String responseBody = response.body();
            log.error("Scene image generation failed | protocol={}, model={}, uri={}, status={}, body={}",
                    request.protocol(),
                    candidateModel,
                    request.requestUri(),
                    response.statusCode(),
                    abbreviate(responseBody));

            boolean hasNextModel = attemptedModels.size() < request.modelsToTry().size();
            lastBusinessException = new BusinessException(
                    fallbackSupport.appendAttemptedModels(
                            fallbackSupport.buildSceneImageFailureMessage(
                                    response.statusCode(),
                                    responseBody,
                                    candidateModel,
                                    request.requestUri()
                            ),
                            attemptedModels,
                            hasNextModel
                    ),
                    fallbackSupport.mapUpstreamFailureStatus(response.statusCode(), responseBody),
                    fallbackSupport.buildFailureDetails(
                            attemptedModels,
                            candidateModel,
                            response.statusCode(),
                            "scene-image:" + request.protocol(),
                            request.requestUri()
                    )
            );

            if (!hasNextModel || !fallbackSupport.shouldTryNextSceneImageModel(response.statusCode(), responseBody)) {
                throw lastBusinessException;
            }
        }

        throw lastBusinessException != null
                ? lastBusinessException
                : new BusinessException(
                fallbackSupport.appendAttemptedModels("图片生成失败", attemptedModels, false),
                HttpStatus.BAD_GATEWAY,
                fallbackSupport.buildFailureDetails(
                        attemptedModels,
                        null,
                        null,
                        "scene-image:" + request.protocol(),
                        request.requestUri()
                )
        );
    }

    SecondStageExecutionResult executeSecondStage(SecondStageRequest request) throws Exception {
        List<String> attemptedModels = new ArrayList<>();
        BusinessException lastBusinessException = null;

        for (String protocol : request.protocolsToTry()) {
            URI requestUri = request.requestUriFactory().apply(protocol);
            for (String candidateModel : request.modelsToTry()) {
                attemptedModels.add(protocol + ":" + candidateModel);
                Map<String, Object> requestBody = request.requestBodyFactory().build(
                        candidateModel,
                        request.drawingPrompt(),
                        protocol
                );
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(requestUri)
                        .header("Content-Type", "application/json")
                        .headers(request.authorizationHeaders())
                        .timeout(Duration.ofSeconds(120))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                        .build();

                log.info(
                        "Calling second-stage image generation | provider={}, protocol={}, model={}, uri={}",
                        providerName,
                        protocol,
                        candidateModel,
                        requestUri
                );

                LocalDateTime secondStageCallStart = LocalDateTime.now();
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    tokenTracker.trackFromResponse(response.body(), candidateModel, "gemini-scene-image-stage2", secondStageCallStart);
                    try {
                        GeminiResponseParser.GeneratedImagePayloadData payload =
                                responseParser.parseSceneImageResponse(response.body(), protocol);
                        if (StringUtils.hasText(payload.imageBase64()) || StringUtils.hasText(payload.imageUrl())) {
                            return new SecondStageExecutionResult(
                                    new SecondStageImageResult(candidateModel, requestUri, payload),
                                    lastBusinessException,
                                    List.copyOf(attemptedModels)
                            );
                        }
                    } catch (BusinessException parseException) {
                        log.warn(
                                "Second-stage image generation returned no usable image | protocol={}, model={}, uri={}, reason={}, rawBody={}",
                                protocol,
                                candidateModel,
                                requestUri,
                                parseException.getMessage(),
                                abbreviate(response.body())
                        );
                        lastBusinessException = parseException;
                        continue;
                    }
                }

                String responseBody = response.body();
                log.warn(
                        "Second-stage image generation failed | provider={}, protocol={}, model={}, uri={}, status={}, body={}",
                        providerName,
                        protocol,
                        candidateModel,
                        requestUri,
                        response.statusCode(),
                        abbreviate(responseBody)
                );

                boolean hasMoreCandidates = fallbackSupport.hasMoreSecondStageCandidates(
                        request.protocolsToTry(),
                        request.modelsToTry(),
                        protocol,
                        candidateModel
                );
                lastBusinessException = new BusinessException(
                        fallbackSupport.appendAttemptedModels(
                                fallbackSupport.buildSecondStageSceneImageFailureMessage(
                                        response.statusCode(),
                                        responseBody,
                                        candidateModel,
                                        requestUri,
                                        protocol
                                ),
                                attemptedModels,
                                hasMoreCandidates
                        ),
                        fallbackSupport.mapUpstreamFailureStatus(response.statusCode(), responseBody),
                        fallbackSupport.buildFailureDetails(
                                attemptedModels,
                                candidateModel,
                                response.statusCode(),
                                "second-stage:" + protocol,
                                requestUri
                        )
                );
            }
        }

        return new SecondStageExecutionResult(null, lastBusinessException, List.copyOf(attemptedModels));
    }

    private String abbreviate(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
    }

    record FirstStageRequest(List<String> modelsToTry,
                             String prompt,
                             String protocol,
                             URI requestUri,
                             String[] authorizationHeaders,
                             BiFunction<String, String, Map<String, Object>> requestBodyFactory) {
    }

    record SecondStageRequest(List<String> protocolsToTry,
                              List<String> modelsToTry,
                              String drawingPrompt,
                              Function<String, URI> requestUriFactory,
                              String[] authorizationHeaders,
                              ProtocolAwareBodyFactory requestBodyFactory) {
    }

    record SecondStageImageResult(String model,
                                  URI uri,
                                  GeminiResponseParser.GeneratedImagePayloadData payload) {
    }

    record SecondStageExecutionResult(SecondStageImageResult imageResult,
                                      BusinessException lastBusinessException,
                                      List<String> attemptedModels) {
    }
}
