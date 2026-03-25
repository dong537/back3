package com.example.demo.service;

import com.example.demo.dto.request.gemini.GeminiFaceAnalysisRequest;
import com.example.demo.dto.request.yijing.YijingSceneImageRequest;
import com.example.demo.dto.response.gemini.GeminiProbeResponse;
import com.example.demo.dto.response.gemini.GeminiFaceAnalysisResponse;
import com.example.demo.dto.response.gemini.GeminiFailureDetails;
import com.example.demo.dto.response.gemini.GeminiFaceResponseMapper;
import com.example.demo.dto.response.yijing.YijingSceneImageResponse;
import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ONE_API_CHAT_IMAGE_MODELS = Set.of(
            "gemini-2.0-flash-exp",
            "gemini-3-pro-image-preview"
    );

    private static final Set<String> ONE_API_CHAT_IMAGE_UNSUPPORTED_MODELS = Set.of(
            "gemini-2.0-pro-exp-02-05",
            "gemini-3-flash-preview",
            "gemini-3-pro-preview",
            "gemini-3.1-pro-preview"
    );

    private static final String DEFAULT_FACE_CULTURAL_PROMPT = """
            你是精通东方相理、人伦气象的传统玄学相学顾问，兼具古典文化底蕴与现代审美，分析风格清雅玄奥，充满东方神秘气韵。
            请对本次面相进行【传统文化娱乐赏析】。
            分析结构严格遵循：
            一、形神总论
            以古典相理词汇，论面格局、气脉、神姿，用词雅致玄幽。
            二、五岳四渎品鉴
            依额头、眉、眼、鼻、颧、口、颏之形，解析骨相气韵与心性禀赋。
            三、精气神与心性投射
            从神态、眉眼气场解读内在性情、才思、人缘格局。
            四、流年气象与运势意象赏析
            以文化意象描述运势氛围，不做绝对断语，只用“气韵”“兆象”“格局”等柔和表述。
            五、相宜调和之法
            给出符合玄学意境的穿搭、妆容、气场提升建议。
            六、结语
            清雅玄意收尾。

            输出要求：
            文风古朴典雅，气韵神秘东方，措辞含蓄优美，结构工整，字数600字左右。
            文末必须标注：本分析为传统文化趣味赏析，仅供娱乐参考。
            """;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.base-url:http://104.197.139.51:3000/v1}")
    private String apiBaseUrl;

    @Value("${gemini.text-model:gemini-3-flash-preview}")
    private String textModel;

    @Value("${gemini.vision-model:gemini-3-flash-preview}")
    private String visionModel;

    @Value("${gemini.vision-models:}")
    private String visionModels;

    @Value("${gemini.vision-payload-formats:openai-image-url,openai-image-url-string}")
    private String visionPayloadFormats;

    @Value("${gemini.temperature:0.2}")
    private double temperature;

    @Value("${gemini.max-tokens:2000}")
    private int maxTokens;

    @Value("${gemini.max-image-bytes:5242880}")
    private long maxImageBytes;

    @Value("${gemini.image-model:}")
    private String imageModel;

    @Value("${gemini.image-models:}")
    private String imageModels;

    @Value("${scene-image.provider:openai-compatible}")
    private String sceneImageProvider;

    @Value("${scene-image.protocol:disabled}")
    private String sceneImageProtocol;

    @Value("${scene-image.api.key:}")
    private String sceneImageApiKey;

    @Value("${scene-image.api.base-url:}")
    private String sceneImageApiBaseUrl;

    @Value("${scene-image.model:}")
    private String sceneImageModel;

    @Value("${scene-image.models:}")
    private String sceneImageModels;

    @Value("${scene-image.response-format:b64_json}")
    private String sceneImageResponseFormat;

    @Value("${scene-image.size:1024x1024}")
    private String sceneImageSize;

    @Value("${scene-image.count:1}")
    private int sceneImageCount;

    @Value("${scene-image.chat-max-tokens:4096}")
    private int sceneImageChatMaxTokens;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YijingSceneImageResponse generateYijingSceneImage(YijingSceneImageRequest request) throws Exception {
        validateSceneImageGenerationConfiguration();

        String prompt = buildYijingSceneImagePrompt(request);
        SceneImageExecutionResult executionResult = executeSceneImageGenerationRequest(prompt);
        boolean hasImage = StringUtils.hasText(executionResult.imageBase64())
                || StringUtils.hasText(executionResult.imageUrl());

        if (!hasImage) {
            log.error(
                    "Scene image request finished without image payload | provider={}, model={}, sceneCategory={}, generationMode={}, uri={}, revisedPromptLength={}, visualSummaryLength={}, displayText={}",
                    executionResult.provider(),
                    executionResult.model(),
                    resolveSceneCategory(request.getQuestion(), request.getInterpretation()),
                    executionResult.generationMode(),
                    executionResult.uri(),
                    executionResult.revisedPrompt() == null ? 0 : executionResult.revisedPrompt().length(),
                    executionResult.visualSummary() == null ? 0 : executionResult.visualSummary().length(),
                    abbreviate(executionResult.displayText())
            );
            throw new BusinessException(
                    "场景图生成失败：模型没有返回任何图片内容，请查看服务端日志中的上游响应详情",
                    HttpStatus.BAD_GATEWAY
            );
        }

        YijingSceneImageResponse response = YijingSceneImageResponse.builder()
                .provider(executionResult.provider())
                .model(executionResult.model())
                .sceneCategory(resolveSceneCategory(request.getQuestion(), request.getInterpretation()))
                .prompt(prompt)
                .revisedPrompt(executionResult.revisedPrompt())
                .imageBase64(executionResult.imageBase64())
                .imageUrl(executionResult.imageUrl())
                .generationMode(executionResult.generationMode())
                .imageSupported(hasImage)
                .visualSummary(executionResult.visualSummary())
                .negativePrompt(executionResult.negativePrompt())
                .displayText(executionResult.displayText())
                .build();

        log.info(
                "Scene image result ready | provider={}, model={}, sceneCategory={}, generationMode={}, imageSupported={}, hasImageUrl={}, hasImageBase64={}, revisedPromptLength={}, visualSummaryLength={}, displayTextLength={}",
                response.getProvider(),
                response.getModel(),
                response.getSceneCategory(),
                response.getGenerationMode(),
                response.getImageSupported(),
                StringUtils.hasText(response.getImageUrl()),
                StringUtils.hasText(response.getImageBase64()),
                response.getRevisedPrompt() == null ? 0 : response.getRevisedPrompt().length(),
                response.getVisualSummary() == null ? 0 : response.getVisualSummary().length(),
                response.getDisplayText() == null ? 0 : response.getDisplayText().length()
        );

        return response;
    }

    public GeminiFaceAnalysisResponse analyzeFace(GeminiFaceAnalysisRequest request) throws Exception {
        validateOneApiConfiguration();

        String mimeType = normalizeMimeType(request.getMimeType());
        if (!SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            throw new BusinessException("仅支持 JPG、PNG、WEBP 图片");
        }

        String imageBase64 = sanitizeBase64(request.getImageBase64());
        if (!StringUtils.hasText(imageBase64)) {
            throw new BusinessException("图片数据无效");
        }

        long imageBytes = estimateDecodedBytes(imageBase64);
        if (imageBytes <= 0) {
            throw new BusinessException("图片数据无效");
        }
        if (imageBytes > maxImageBytes) {
            throw new BusinessException("图片不能超过 5MB");
        }

        VisionExecutionResult executionResult = executeVisionRequest(
                imageBase64,
                mimeType,
                buildEnhancedPrompt(request.getPrompt()),
                maxTokens,
                "face analysis"
        );
        return GeminiFaceResponseMapper.fromMap(parseResponse(executionResult.responseBody(), executionResult.model()));
    }

    public GeminiProbeResponse probeText(String prompt) throws Exception {
        validateOneApiConfiguration();

        String effectivePrompt = StringUtils.hasText(prompt)
                ? prompt.trim()
                : "Reply with exactly OK.";

        Map<String, Object> requestBody = buildTextProbeRequestBody(effectivePrompt);
        return executeProbe(textModel, requestBody, "text");
    }

    public GeminiProbeResponse probeVision(GeminiFaceAnalysisRequest request) throws Exception {
        validateOneApiConfiguration();

        String mimeType = normalizeMimeType(request.getMimeType());
        if (!SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            throw new BusinessException("仅支持 JPG、PNG、WEBP 图片");
        }

        String imageBase64 = sanitizeBase64(request.getImageBase64());
        if (!StringUtils.hasText(imageBase64)) {
            throw new BusinessException("图片数据无效");
        }

        long imageBytes = estimateDecodedBytes(imageBase64);
        if (imageBytes <= 0) {
            throw new BusinessException("图片数据无效");
        }
        if (imageBytes > maxImageBytes) {
            throw new BusinessException("图片不能超过 5MB");
        }

        String effectivePrompt = StringUtils.hasText(request.getPrompt())
                ? request.getPrompt().trim()
                : "Describe this image in one short sentence.";

        VisionExecutionResult executionResult = executeVisionRequest(
                imageBase64,
                mimeType,
                effectivePrompt,
                Math.min(maxTokens, 300),
                "vision probe"
        );
        String content = parseRawResponseText(executionResult.responseBody());
        return GeminiProbeResponse.builder()
                .model(executionResult.model())
                .uri(executionResult.uri().toString())
                .content(content)
                .contentLength(content == null ? 0 : content.length())
                .build();
    }

    private void validateOneApiConfiguration() {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("Gemini API Key 未配置，请先设置 ONE_API_KEY 或 GEMINI_API_KEY");
        }
        if (!apiKey.startsWith("sk-")) {
            throw new BusinessException("One-API key 格式错误，必须以 sk- 开头");
        }
        if (!StringUtils.hasText(visionModel)) {
            throw new BusinessException("Vision model 未配置，请先设置 ONE_API_GEMINI_VISION_MODEL 或 GEMINI_VISION_MODEL");
        }
        if (!StringUtils.hasText(apiBaseUrl)) {
            throw new BusinessException("One-API base URL 未配置，请先设置 ONE_API_BASE_URL");
        }
    }

    private void validateSceneImageGenerationConfiguration() {
        if (isGoogleSceneImageProvider()) {
            validateGoogleOfficialSceneImageConfiguration();
            return;
        }

        String resolvedApiKey = resolveSceneImageApiKey();
        String resolvedApiBaseUrl = resolveSceneImageApiBaseUrl();

        if (!StringUtils.hasText(resolvedApiKey)) {
            throw new BusinessException("场景图功能现在统一走 OneAPI 的 chat/completions 图片通道，请先配置 SCENE_IMAGE_API_KEY 或 ONE_API_KEY");
        }
        if (!resolvedApiKey.startsWith("sk-")) {
            throw new BusinessException("OneAPI 场景图 token 格式错误，必须以 sk- 开头");
        }
        if (!StringUtils.hasText(resolvedApiBaseUrl)) {
            throw new BusinessException("场景图功能现在统一走 OneAPI 的 chat/completions 图片通道，请先配置 SCENE_IMAGE_API_BASE_URL 或 ONE_API_BASE_URL");
        }
        if (resolveSceneImageModelsToTry().isEmpty()) {
            throw new BusinessException("场景图功能缺少可用模型，请先配置支持图片生成的 SCENE_IMAGE_MODEL");
        }
    }

    private URI buildRequestUri() {
        return URI.create(normalizeBaseUrl(apiBaseUrl) + "/chat/completions");
    }

    private URI buildImagesRequestUri() {
        return URI.create(normalizeBaseUrl(apiBaseUrl) + "/images/generations");
    }

    private URI buildSceneImageRequestUri() {
        String protocol = resolveSceneImageProtocol();
        String baseUrl = normalizeSceneImageBaseUrl(resolveSceneImageApiBaseUrl(), protocol);
        String suffix = "images-generations".equals(protocol)
                ? "/images/generations"
                : "/chat/completions";
        return URI.create(baseUrl + suffix);
    }

    private String[] buildAuthorizationHeaders() {
        return new String[]{"Authorization", "Bearer " + apiKey};
    }

    private String[] buildSceneImageAuthorizationHeaders() {
        return new String[]{"Authorization", "Bearer " + resolveSceneImageApiKey()};
    }

    private String[] buildSecondStageAuthorizationHeaders() {
        return new String[]{"Authorization", "Bearer " + resolveSecondStageApiKey()};
    }

    private String resolveSecondStageApiKey() {
        if (StringUtils.hasText(sceneImageApiKey)) {
            return sceneImageApiKey.trim();
        }
        return apiKey;
    }

    private URI buildSecondStageSceneImageRequestUri(String protocol) {
        String baseUrl = normalizeSceneImageBaseUrl(resolveSecondStageApiBaseUrl(), protocol);
        String suffix = "images-generations".equals(protocol)
                ? "/images/generations"
                : "/chat/completions";
        return URI.create(baseUrl + suffix);
    }

    private String resolveSecondStageApiBaseUrl() {
        if (StringUtils.hasText(sceneImageApiBaseUrl)) {
            return sceneImageApiBaseUrl.trim();
        }
        return apiBaseUrl;
    }

    private VisionExecutionResult executeVisionRequest(String imageBase64,
                                                       String mimeType,
                                                       String prompt,
                                                       int tokenLimit,
                                                       String scenario) throws Exception {
        List<String> modelsToTry = resolveVisionModelsToTry();
        List<String> payloadFormatsToTry = resolveVisionPayloadFormatsToTry();
        List<String> attemptedModels = new ArrayList<>();
        BusinessException lastBusinessException = null;
        String protocol = resolveSceneImageProtocol();
        URI requestUri = buildSceneImageRequestUri();

        for (int modelIndex = 0; modelIndex < modelsToTry.size(); modelIndex++) {
            String candidateModel = modelsToTry.get(modelIndex);
            attemptedModels.add(candidateModel);
            for (int formatIndex = 0; formatIndex < payloadFormatsToTry.size(); formatIndex++) {
                String payloadFormat = payloadFormatsToTry.get(formatIndex);
                Map<String, Object> requestBody = buildVisionRequestBody(
                        imageBase64,
                        mimeType,
                        prompt,
                        tokenLimit,
                        candidateModel,
                        payloadFormat
                );
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(requestUri)
                        .header("Content-Type", "application/json")
                        .headers(buildAuthorizationHeaders())
                        .timeout(Duration.ofSeconds(100))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                        .build();

                log.info("Calling Gemini {} via One-API | model={}, uri={}, payloadFormat={}, modelAttempt={}/{}, formatAttempt={}/{}",
                        scenario,
                        candidateModel,
                        requestUri,
                        payloadFormat,
                        modelIndex + 1,
                        modelsToTry.size(),
                        formatIndex + 1,
                        payloadFormatsToTry.size());

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return new VisionExecutionResult(candidateModel, requestUri, response.body());
                }

                String responseBody = response.body();
                log.error("Gemini {} failed | model={}, uri={}, payloadFormat={}, status={}, body={}",
                        scenario,
                        candidateModel,
                        requestUri,
                        payloadFormat,
                        response.statusCode(),
                        abbreviate(responseBody));

                boolean hasNextFormat = formatIndex < payloadFormatsToTry.size() - 1;
                boolean hasNextModel = modelIndex < modelsToTry.size() - 1;
                lastBusinessException = new BusinessException(
                        appendAttemptedModels(
                                buildFailureMessage(response.statusCode(), responseBody, candidateModel, requestUri),
                                attemptedModels,
                                hasNextFormat || hasNextModel
                        ),
                        mapUpstreamFailureStatus(response.statusCode(), responseBody),
                        buildFailureDetails(attemptedModels, candidateModel, response.statusCode(), payloadFormat, requestUri)
                );

                if (hasNextFormat && shouldTryNextVisionModel(response.statusCode(), responseBody)) {
                    log.warn("Gemini vision payload format {} failed for model {}, switching to the next payload format",
                            payloadFormat, candidateModel);
                    continue;
                }

                if (hasNextModel && shouldTryNextVisionModel(response.statusCode(), responseBody)) {
                    log.warn("Gemini vision model {} failed, switching to the next configured model", candidateModel);
                    break;
                }

                throw lastBusinessException;
            }
        }

        throw lastBusinessException != null
                ? lastBusinessException
                : new BusinessException(
                        appendAttemptedModels("Gemini vision call failed", attemptedModels, false),
                        HttpStatus.BAD_GATEWAY,
                        buildFailureDetails(attemptedModels, null, null, null, requestUri)
                );
    }

    private SceneImageExecutionResult executeSceneImageGenerationRequest(String prompt) throws Exception {
        List<String> modelsToTry = resolveSceneImageModelsToTry();
        List<String> attemptedModels = new ArrayList<>();
        BusinessException lastBusinessException = null;
        String protocol = resolveSceneImageProtocol();
        URI requestUri = buildSceneImageRequestUri();

        for (String candidateModel : modelsToTry) {
            attemptedModels.add(candidateModel);
            Map<String, Object> requestBody = buildSceneImageGenerationRequestBody(candidateModel, prompt);
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(requestUri)
                    .header("Content-Type", "application/json")
                    .headers(buildSceneImageAuthorizationHeaders())
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            log.info("Calling scene image generation | provider={}, protocol={}, model={}, uri={}",
                    resolveSceneImageProviderName(), protocol, candidateModel, requestUri);

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                try {
                    SceneImageExecutionResult executionResult = parseSceneImageResponseResult(response.body(), candidateModel, requestUri, protocol);
                    if ("prompt_only".equals(executionResult.generationMode())) {
                        log.warn(
                                "Scene image first-stage returned plan instead of image | model={}, uri={}, revisedPromptLength={}, visualSummaryLength={}, displayText={}, rawBody={}",
                                candidateModel,
                                requestUri,
                                executionResult.revisedPrompt() == null ? 0 : executionResult.revisedPrompt().length(),
                                executionResult.visualSummary() == null ? 0 : executionResult.visualSummary().length(),
                                abbreviate(executionResult.displayText()),
                                abbreviate(response.body())
                        );
                        SceneImageExecutionResult secondStageResult = executeSecondStageImageGeneration(executionResult, prompt);
                        if (secondStageResult != null) {
                            return secondStageResult;
                        }
                        throw new BusinessException(
                                "场景图模型仅返回了场景方案文本，二次绘图也没有拿到图片结果",
                                HttpStatus.BAD_GATEWAY,
                                buildFailureDetails(attemptedModels, candidateModel, 200, "scene-image:" + protocol + ":prompt-only", requestUri)
                        );
                    }
                    return executionResult;
                } catch (BusinessException parseException) {
                    boolean hasNextModel = attemptedModels.size() < modelsToTry.size();
                    log.warn(
                            "Scene image generation returned 200 but no usable content | model={}, uri={}, reason={}, hasNextModel={}, rawBody={}",
                            candidateModel,
                            requestUri,
                            parseException.getMessage(),
                            hasNextModel,
                            abbreviate(response.body())
                    );
                    lastBusinessException = new BusinessException(
                            appendAttemptedModels(parseException.getMessage(), attemptedModels, hasNextModel),
                            parseException.getStatus(),
                            buildFailureDetails(attemptedModels, candidateModel, 200, "scene-image:" + protocol + ":parse", requestUri)
                    );
                    if (!hasNextModel) {
                        throw lastBusinessException;
                    }
                    continue;
                }
            }

            String responseBody = response.body();
            log.error("Scene image generation failed | protocol={}, model={}, uri={}, status={}, body={}",
                    protocol,
                    candidateModel,
                    requestUri,
                    response.statusCode(),
                    abbreviate(responseBody));

            boolean hasNextModel = attemptedModels.size() < modelsToTry.size();
            lastBusinessException = new BusinessException(
                    appendAttemptedModels(
                            buildSceneImageFailureMessage(response.statusCode(), responseBody, candidateModel, requestUri),
                            attemptedModels,
                            hasNextModel
                    ),
                    mapUpstreamFailureStatus(response.statusCode(), responseBody),
                    buildFailureDetails(attemptedModels, candidateModel, response.statusCode(), "scene-image:" + protocol, requestUri)
            );

            if (!hasNextModel || !shouldTryNextSceneImageModel(response.statusCode(), responseBody)) {
                throw lastBusinessException;
            }
        }

        throw lastBusinessException != null
                ? lastBusinessException
                : new BusinessException(
                appendAttemptedModels("图片生成失败", attemptedModels, false),
                HttpStatus.BAD_GATEWAY,
                buildFailureDetails(attemptedModels, null, null, "scene-image:" + protocol, requestUri)
        );
    }

    private SceneImageExecutionResult executeSecondStageImageGeneration(SceneImageExecutionResult planningResult,
                                                                       String originalPrompt) throws Exception {
        List<String> protocolsToTry = resolveSecondStageProtocolsToTry();
        List<String> modelsToTry = resolveSecondStageImageModelsToTry();
        List<String> attemptedModels = new ArrayList<>();
        BusinessException lastBusinessException = null;

        String drawingPrompt = buildSecondStageDrawingPrompt(
                planningResult.revisedPrompt(),
                planningResult.negativePrompt(),
                originalPrompt
        );

        for (String protocol : protocolsToTry) {
            URI requestUri = buildSecondStageSceneImageRequestUri(protocol);
            for (String candidateModel : modelsToTry) {
                attemptedModels.add(protocol + ":" + candidateModel);
                Map<String, Object> requestBody = buildSecondStageSceneImageRequestBody(candidateModel, drawingPrompt, protocol);
                String requestBodyJson = objectMapper.writeValueAsString(requestBody);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(requestUri)
                        .header("Content-Type", "application/json")
                        .headers(buildSecondStageAuthorizationHeaders())
                        .timeout(Duration.ofSeconds(120))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                        .build();

                log.info(
                        "Calling second-stage image generation | provider={}, protocol={}, model={}, uri={}",
                        resolveSceneImageProviderName(),
                        protocol,
                        candidateModel,
                        requestUri
                );

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    try {
                        GeneratedImagePayload payload = parseSceneImageResponse(response.body(), protocol);
                        if (StringUtils.hasText(payload.imageBase64()) || StringUtils.hasText(payload.imageUrl())) {
                            return new SceneImageExecutionResult(
                                    resolveSceneImageProviderName(),
                                    candidateModel,
                                    requestUri,
                                    payload.imageBase64(),
                                    payload.imageUrl(),
                                    StringUtils.hasText(payload.revisedPrompt()) ? payload.revisedPrompt() : planningResult.revisedPrompt(),
                                    planningResult.visualSummary(),
                                    planningResult.negativePrompt(),
                                    "已自动串联第二绘图接口并生成真实场景图。",
                                    "second_stage_image"
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
                        resolveSceneImageProviderName(),
                        protocol,
                        candidateModel,
                        requestUri,
                        response.statusCode(),
                        abbreviate(responseBody)
                );

                boolean hasMoreCandidates = hasMoreSecondStageCandidates(protocolsToTry, modelsToTry, protocol, candidateModel);
                lastBusinessException = new BusinessException(
                        appendAttemptedModels(
                                buildSecondStageSceneImageFailureMessage(response.statusCode(), responseBody, candidateModel, requestUri, protocol),
                                attemptedModels,
                                hasMoreCandidates
                        ),
                        mapUpstreamFailureStatus(response.statusCode(), responseBody),
                        buildFailureDetails(attemptedModels, candidateModel, response.statusCode(), "second-stage:" + protocol, requestUri)
                );
            }
        }

        log.warn(
                "Second-stage image generation exhausted all models, returning prompt-only result | attemptedModels={}, fallbackMode={}",
                String.join(",", attemptedModels),
                planningResult.generationMode()
        );
        if (lastBusinessException != null) {
            log.warn("Second-stage final failure reason: {}", lastBusinessException.getMessage());
        }
        return null;
    }

    private GeminiProbeResponse executeProbe(String model, Map<String, Object> requestBody, String probeType) throws Exception {
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        URI requestUri = buildRequestUri();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(requestUri)
                .header("Content-Type", "application/json")
                .headers(buildAuthorizationHeaders())
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        log.info("Calling Gemini {} probe via One-API | model={}, uri={}", probeType, model, requestUri);

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String responseBody = response.body();
            log.error("Gemini {} probe failed | model={}, uri={}, status={}, body={}",
                    probeType, model, requestUri, response.statusCode(), abbreviate(responseBody));
            throw new BusinessException(
                    buildFailureMessage(response.statusCode(), responseBody, model, requestUri),
                    mapUpstreamFailureStatus(response.statusCode(), responseBody)
            );
        }

        String content = parseRawResponseText(response.body());
        return GeminiProbeResponse.builder()
                .model(model)
                .uri(requestUri.toString())
                .content(content)
                .contentLength(content == null ? 0 : content.length())
                .build();
    }

    private List<String> resolveVisionModelsToTry() {
        List<String> models = new ArrayList<>();
        appendVisionModel(models, visionModel);
        if (StringUtils.hasText(visionModels)) {
            for (String candidate : visionModels.split(",")) {
                appendVisionModel(models, candidate);
            }
        }
        if (models.isEmpty()) {
            models.add("gemini-3-flash-preview");
        }
        return models;
    }

    private List<String> resolveImageModelsToTry() {
        List<String> models = new ArrayList<>();
        appendImageGenerationModel(models, imageModel);
        appendImageGenerationModel(models, visionModel);
        if (StringUtils.hasText(imageModels)) {
            for (String candidate : imageModels.split(",")) {
                appendImageGenerationModel(models, candidate);
            }
        }
        if (StringUtils.hasText(visionModels)) {
            for (String candidate : visionModels.split(",")) {
                appendImageGenerationModel(models, candidate);
            }
        }
        if (models.isEmpty()) {
            models.add("gemini-3-pro-image-preview");
        }
        return models;
    }

    private List<String> resolveSceneImageModelsToTry() {
        List<String> models = new ArrayList<>();
        appendSceneImageModel(models, sceneImageModel);
        if (StringUtils.hasText(sceneImageModels)) {
            for (String candidate : sceneImageModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        appendSceneImageModel(models, imageModel);
        for (String candidate : resolveImageModelsToTry()) {
            appendSceneImageModel(models, candidate);
        }
        if (models.isEmpty()) {
            models.add("gemini-3-pro-image-preview");
        }
        return models;
    }

    private List<String> resolveSecondStageImageModelsToTry() {
        List<String> models = new ArrayList<>();
        appendSceneImageModel(models, sceneImageModel);
        if (StringUtils.hasText(sceneImageModels)) {
            for (String candidate : sceneImageModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        appendSceneImageModel(models, imageModel);
        if (StringUtils.hasText(imageModels)) {
            for (String candidate : imageModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        appendSceneImageModel(models, visionModel);
        if (StringUtils.hasText(visionModels)) {
            for (String candidate : visionModels.split(",")) {
                appendSceneImageModel(models, candidate);
            }
        }
        if (models.isEmpty()) {
            models.add("gemini-3-pro-image-preview");
        }
        return models;
    }

    private List<String> resolveSecondStageProtocolsToTry() {
        List<String> protocols = new ArrayList<>();
        appendProtocol(protocols, resolveSceneImageProtocol());
        appendProtocol(protocols, "images-generations");
        appendProtocol(protocols, "chat-completions");
        return protocols;
    }

    private void appendProtocol(List<String> protocols, String candidate) {
        if (!StringUtils.hasText(candidate)) {
            return;
        }
        if ("disabled".equals(candidate)) {
            return;
        }
        if (!protocols.contains(candidate)) {
            protocols.add(candidate);
        }
    }

    private void appendAnyModel(List<String> models, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        if (!models.contains(normalized)) {
            models.add(normalized);
        }
    }

    private void appendImageGenerationModel(List<String> models, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        if (!isLikelyImageGenerationModel(normalized.toLowerCase())) {
            return;
        }
        if (!models.contains(normalized)) {
            models.add(normalized);
        }
    }

    private void appendSceneImageModel(List<String> models, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        if (!isSupportedSceneImageModel(normalized)) {
            return;
        }
        if (!models.contains(normalized)) {
            models.add(normalized);
        }
    }

    private boolean isLikelyImageGenerationModel(String normalizedModelName) {
        if (ONE_API_CHAT_IMAGE_MODELS.contains(normalizedModelName)) {
            return true;
        }
        if (ONE_API_CHAT_IMAGE_UNSUPPORTED_MODELS.contains(normalizedModelName)) {
            return false;
        }
        return normalizedModelName.contains("image")
                || normalizedModelName.contains("imagen")
                || normalizedModelName.contains("generate");
    }

    private boolean isSupportedSceneImageModel(String modelName) {
        String normalized = modelName == null ? "" : modelName.trim().toLowerCase();
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        if (ONE_API_CHAT_IMAGE_UNSUPPORTED_MODELS.contains(normalized)) {
            return false;
        }
        return isLikelyImageGenerationModel(normalized);
    }

    private List<String> resolveVisionPayloadFormatsToTry() {
        List<String> formats = new ArrayList<>();
        if (StringUtils.hasText(visionPayloadFormats)) {
            for (String candidate : visionPayloadFormats.split(",")) {
                appendVisionPayloadFormat(formats, candidate);
            }
        }
        if (formats.isEmpty()) {
            formats.add("openai-image-url");
        }
        return formats;
    }

    private void appendVisionModel(List<String> models, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        String lowered = normalized.toLowerCase();
        if (lowered.contains("embedding")) {
            return;
        }
        if (!models.contains(normalized)) {
            models.add(normalized);
        }
    }

    private void appendVisionPayloadFormat(List<String> formats, String candidate) {
        String normalized = candidate == null ? "" : candidate.trim().toLowerCase();
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        if (!normalized.equals("openai-image-url") && !normalized.equals("openai-image-url-string")) {
            return;
        }
        if (!formats.contains(normalized)) {
            formats.add(normalized);
        }
    }

    private boolean shouldTryNextVisionModel(int statusCode, String responseBody) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (statusCode == 401) {
            return false;
        }
        return statusCode == 400
                || statusCode == 404
                || statusCode == 429
                || statusCode >= 500
                || body.contains("unsupported")
                || body.contains("invalid model")
                || body.contains("model_not_found")
                || body.contains("no available channel")
                || body.contains("upstream")
                || body.contains("bad gateway")
                || body.contains("internal server error");
    }

    private boolean shouldTryNextSceneImageModel(int statusCode, String responseBody) {
        String body = responseBody == null ? "" : responseBody.toLowerCase();
        if (body.contains("no candidates returned")) {
            return false;
        }
        return shouldTryNextVisionModel(statusCode, responseBody);
    }

    private String appendAttemptedModels(String message, List<String> attemptedModels, boolean hasNextModel) {
        if (attemptedModels == null || attemptedModels.isEmpty()) {
            return message;
        }
        if (hasNextModel) {
            return message;
        }
        return message + " | attemptedModels=" + String.join(",", attemptedModels);
    }

    private GeminiFailureDetails buildFailureDetails(List<String> attemptedModels,
                                                     String lastModel,
                                                     Integer lastStatus,
                                                     String lastPayloadFormat,
                                                     URI requestUri) {
        return GeminiFailureDetails.builder()
                .attemptedModels(attemptedModels == null ? List.of() : List.copyOf(attemptedModels))
                .lastModel(lastModel)
                .lastStatus(lastStatus)
                .lastPayloadFormat(lastPayloadFormat)
                .uri(requestUri == null ? null : requestUri.toString())
                .build();
    }

    private Map<String, Object> buildTextProbeRequestBody(String prompt) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", textModel.trim());
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", Math.min(maxTokens, 200));
        return requestBody;
    }

    private Map<String, Object> buildImageGenerationRequestBody(String modelName, String prompt) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName.trim());
        requestBody.put("prompt", prompt);
        requestBody.put("response_format", "b64_json");
        return requestBody;
    }

    private Map<String, Object> buildSceneImageGenerationRequestBody(String modelName, String prompt) {
        if ("images-generations".equals(resolveSceneImageProtocol())) {
            return buildSceneImageImagesRequestBody(modelName, prompt);
        }
        return buildSceneImageChatRequestBody(modelName, prompt);
    }

    private Map<String, Object> buildSceneImageImagesRequestBody(String modelName, String prompt) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName.trim());
        requestBody.put("prompt", prompt);
        requestBody.put("response_format", StringUtils.hasText(sceneImageResponseFormat) ? sceneImageResponseFormat.trim() : "b64_json");
        requestBody.put("n", Math.max(sceneImageCount, 1));
        if (StringUtils.hasText(sceneImageSize)) {
            requestBody.put("size", sceneImageSize.trim());
        }
        return requestBody;
    }

    private Map<String, Object> buildSceneImageChatRequestBody(String modelName, String prompt) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("responseModalities", List.of("TEXT", "IMAGE"));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName.trim());
        requestBody.put("messages", List.of(message));
        requestBody.put("generationConfig", generationConfig);
        requestBody.put("modalities", List.of("text", "image"));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", Math.max(2000, sceneImageChatMaxTokens));
        return requestBody;
    }

    private Map<String, Object> buildSecondStageSceneImageRequestBody(String modelName, String prompt, String protocol) {
        if ("images-generations".equals(protocol)) {
            return buildSceneImageImagesRequestBody(modelName, prompt);
        }
        return buildSceneImageChatRequestBody(modelName, prompt);
    }

    private Map<String, Object> buildVisionProbeRequestBody(String imageBase64, String mimeType, String prompt) {
        return buildVisionRequestBody(
                imageBase64,
                mimeType,
                prompt,
                Math.min(maxTokens, 300),
                visionModel,
                resolveVisionPayloadFormatsToTry().get(0)
        );
    }

    private Map<String, Object> buildVisionRequestBody(String imageBase64,
                                                       String mimeType,
                                                       String prompt,
                                                       int tokenLimit,
                                                       String modelName) {
        return buildVisionRequestBody(
                imageBase64,
                mimeType,
                prompt,
                tokenLimit,
                modelName,
                resolveVisionPayloadFormatsToTry().get(0)
        );
    }

    private Map<String, Object> buildVisionRequestBody(String imageBase64,
                                                       String mimeType,
                                                       String prompt,
                                                       int tokenLimit,
                                                       String modelName,
                                                       String payloadFormat) {
        String dataUrl = "data:" + mimeType + ";base64," + imageBase64;

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);

        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("type", "image_url");
        if ("openai-image-url-string".equalsIgnoreCase(payloadFormat)) {
            imagePart.put("image_url", dataUrl);
        } else {
            imagePart.put("image_url", Map.of("url", dataUrl));
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textPart, imagePart));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName.trim());
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", tokenLimit);
        return requestBody;
    }

    private Map<String, Object> buildRequestBody(String imageBase64, String mimeType, String prompt) {
        return buildVisionRequestBody(
                imageBase64,
                mimeType,
                buildEnhancedPrompt(prompt),
                maxTokens,
                visionModel,
                resolveVisionPayloadFormatsToTry().get(0)
        );
    }

    private String buildPrompt(String userPrompt) {
        String safePrompt = StringUtils.hasText(userPrompt)
                ? userPrompt.trim()
                : "请结合传统面相学的文化表达方式，先描述可见五官特征，再给出娱乐性的文化解读和一份报告。";

        return """
                你是一个“传统面相学文化说明”助手。
                你只能根据图片中可见的五官与脸部轮廓，输出文化娱乐性的说明报告。
                你不能进行身份识别，严禁猜测具体人物姓名、背景或与任何数据库做比对。
                你不能断言此人的真实性格、命运、财富、婚恋结果、智力、信用、健康、精神状态或其他敏感属性。
                你可以介绍“在传统面相学语境中，这类外貌常被怎样解读”，但必须明确这只是传统文化视角，不是事实判断。
                如果图中没有清晰可见的人脸，请明确说明。
                请严格输出 JSON，对象结构如下：
                {
                  "hasFace": true,
                  "faceCount": 1,
                  "visualSummary": "先总结图片中可见的人脸与五官特征",
                  "observedFeatures": [
                    {
                      "region": "额头",
                      "observation": "看到的客观外观特征",
                      "clarity": "清晰/一般/不清晰"
                    }
                  ],
                  "physiognomyReport": {
                    "forehead": "介绍传统相学里对额头区域的常见文化说法，使用“常被视为/常被联想到”措辞",
                    "eyesAndBrows": "介绍眉眼区域的传统文化说法",
                    "nose": "介绍鼻部区域的传统文化说法",
                    "mouthAndChin": "介绍口唇与下巴区域的传统文化说法",
                    "overallImpression": "给出整体的文化风格总结，但不要断言事实或命运"
                  },
                  "imageQuality": "对清晰度、光线、角度的简短判断",
                  "reportSummary": "输出一段完整总结，语气克制，强调仅供文化娱乐参考",
                  "suggestions": ["拍摄建议1", "拍摄建议2"],
                  "disclaimer": "本报告为基于可见外观生成的传统文化娱乐性说明，不构成对性格、命运、能力、健康或身份的事实判断。"
                }

                用户补充要求：
                """ + safePrompt;
    }

    private String buildEnhancedPrompt(String userPrompt) {
        String safePrompt = StringUtils.hasText(userPrompt)
                ? userPrompt.trim()
                : DEFAULT_FACE_CULTURAL_PROMPT;

        return """
                你是一个“传统面相学文化说明”助手。
                你只能根据图片中可见的五官与脸部轮廓，输出文化娱乐性的说明报告。
                你不能进行身份识别，严禁猜测具体人物姓名、背景，或与任何数据库做比对。
                你不能断言此人的真实性格、命运、财富、婚恋结果、智力、信用、健康、精神状态或其他敏感属性。
                你可以介绍“在传统面相学语境中，这类外貌常被怎样解读”，但必须明确这只是传统文化视角，不是事实判断。
                如果图中没有清晰可见的人脸，请明确说明。
                请严格输出 JSON，对象结构如下：
                {
                  "hasFace": true,
                  "faceCount": 1,
                  "visualSummary": "先总结图片中可见的面部特征与神态，为后续赏析铺垫",
                  "observedFeatures": [
                    {
                      "region": "额头",
                      "observation": "看到的客观外观特征",
                      "clarity": "清晰/一般/不清晰"
                    }
                  ],
                  "physiognomyReport": {
                    "forehead": "聚焦额头与上庭的传统文化赏析",
                    "eyesAndBrows": "聚焦眉眼与神采的传统文化赏析",
                    "nose": "聚焦鼻梁、鼻头、颧势相关的传统文化赏析",
                    "mouthAndChin": "聚焦口唇、下颏与收势的传统文化赏析",
                    "overallImpression": "以古典相理语汇概括整体气韵与神姿，但不要断言事实或命运"
                  },
                  "imageQuality": "对清晰度、光线、角度的简短判断",
                  "reportSummary": "请将用户要求的六个部分尽量完整落实在这里，采用古朴典雅、含蓄优美、带有东方神秘气韵的文风，约600字，并以“本分析为传统文化趣味赏析，仅供娱乐参考。”收尾",
                  "suggestions": ["穿搭或妆容建议1", "气场提升建议2"],
                  "disclaimer": "本报告为基于可见外观生成的传统文化娱乐性说明，不构成对性格、命运、能力、健康或身份的事实判断。"
                }

                用户补充要求：
                %s
                """.formatted(safePrompt);
    }

    private String buildYijingSceneImagePrompt(YijingSceneImageRequest request) {
        String sceneCategory = resolveSceneCategory(request.getQuestion(), request.getInterpretation());
        String sceneSuggestion = resolveSceneSuggestion(sceneCategory);
        String originalName = resolveHexagramName(request.getOriginal());
        String changedName = request.getChanged() == null ? "" : resolveHexagramName(request.getChanged());
        String keywords = request.getOriginal() == null || request.getOriginal().getKeywords() == null
                ? ""
                : String.join("、", request.getOriginal().getKeywords());
        String changingLines = request.getChangingLines() == null || request.getChangingLines().isEmpty()
                ? "无明显动爻，整体气机偏稳。"
                : "动爻为第 " + request.getChangingLines().stream().map(String::valueOf).reduce((a, b) -> a + "、" + b).orElse("") + " 爻，画面要带有局势流转、将变未变的张力。";

        String changedClause = StringUtils.hasText(changedName)
                ? "变卦参考：" + changedName + "，让画面在主体情绪与环境走势中体现从本卦向变卦过渡的感觉。"
                : "没有变卦时，画面更强调当下处境本身的气场与停顿感。";

        return """
                你正在通过 YMAPI/One-API 的 OpenAI 兼容 chat.completions 接口工作。
                请优先尝试直接返回一张适合“易经占卜结果页”的东方玄学场景图。
                如果当前代理通道不能直接输出图片，请不要报错，也不要解释限制，改为严格输出一个 JSON 对象，供前端展示和后续二次绘图使用。

                用户问题：%s
                场景类别：%s
                本卦：%s
                卦意参考：%s
                卦象意境：%s
                关键词：%s
                解读摘要：%s
                简要提示：%s
                %s
                %s

                画面要求：
                1. 采用东方神秘美学，电影级光影，氛围浓郁，细节丰富。
                2. 主体是与问题相关的当代场景人物或背影，处于“%s”这类真实处境中。
                3. 场景可融入卦盘、铜钱、香雾、烛火、月色、流动光纹、八卦纹理等元素，但要克制自然，不要堆满道具。
                4. 要体现求测者此刻的处境、心理张力与转机感，让人一眼看出这幅图与占卜结果高度相关。
                5. 构图完整，适合手机端竖版展示，视觉中心明确。
                6. 不要出现任何文字、水印、UI、边框、二维码、logo。
                7. 不要低幼卡通，不要夸张宗教符号，不要惊悚恐怖，不要肢体畸形，不要多余手指。

                风格建议：
                %s

                如果返回的是文本而不是图片，必须严格输出 JSON，不要使用 Markdown，不要输出额外说明，格式如下：
                {
                  "visual_summary": "用 2-3 句话概括这张画面的核心意境、人物处境和视觉焦点",
                  "revised_prompt": "一段可以直接交给图像模型继续绘制的完整中文提示词",
                  "negative_prompt": "需要规避的元素，使用中文短语，以逗号分隔",
                  "display_text": "一句给前端用户看的说明，明确表示当前已生成场景方案，可继续用于绘图"
                }
                """.formatted(
                normalizePromptText(request.getQuestion(), 100),
                sceneCategory,
                normalizePromptText(originalName, 40),
                normalizePromptText(extractHexagramMeaning(request.getOriginal()), 180),
                normalizePromptText(extractHexagramImage(request.getOriginal()), 160),
                normalizePromptText(keywords, 80),
                normalizePromptText(request.getInterpretation(), 360),
                normalizePromptText(request.getInterpretationHint(), 120),
                changingLines,
                changedClause,
                normalizePromptText(sceneSuggestion, 60),
                normalizePromptText(resolveStyleSuggestion(sceneCategory), 180)
        );
    }

    private Map<String, Object> parseResponse(String responseBody, String actualModel) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("Gemini 返回内容为空");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        if (message == null) {
            throw new BusinessException("Gemini 返回内容格式错误");
        }

        String text = extractMessageText(message.get("content"));
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("Gemini 返回内容为空");
        }

        Map<String, Object> parsedResult = tryParseJson(text);
        parsedResult.put("provider", "gemini");
        parsedResult.put("model", actualModel);
        parsedResult.put("rawText", text);
        return parsedResult;
    }

    private String parseRawResponseText(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("Gemini 返回内容为空");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        if (message == null) {
            throw new BusinessException("Gemini 返回内容格式错误");
        }

        String text = extractMessageText(message.get("content"));
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("Gemini 返回内容为空");
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private SceneImageExecutionResult parseSceneImageChatResponse(String responseBody,
                                                                  String actualModel,
                                                                  URI requestUri) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("场景图通道未返回可用结果");
        }

        Map<String, Object> firstChoice = choices.get(0);
        String finishReason = trimToNull(objectToString(firstChoice.get("finish_reason")));
        Map<String, Object> message = firstChoice.get("message") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : null;
        if (message == null || message.isEmpty()) {
            throw new BusinessException("场景图通道返回内容格式错误");
        }

        GeneratedImagePayload imagePayload = extractImagePayloadFromChatMessage(message);
        String text = extractMessageText(message.get("content"));
        SceneImagePlanPayload planPayload = StringUtils.hasText(text)
                ? parseSceneImagePlanPayload(text)
                : null;

        log.info(
                "Scene image chat response parsed | model={}, uri={}, finishReason={}, hasImagePayload={}, textLength={}, hasPlanPayload={}, contentType={}, rawBody={}",
                actualModel,
                requestUri,
                finishReason,
                imagePayload != null,
                text == null ? 0 : text.length(),
                planPayload != null,
                message.get("content") == null ? "null" : message.get("content").getClass().getSimpleName(),
                abbreviate(responseBody)
        );

        if (imagePayload != null) {
            String revisedPrompt = StringUtils.hasText(imagePayload.revisedPrompt())
                    ? imagePayload.revisedPrompt()
                    : (planPayload == null ? null : planPayload.revisedPrompt());
            String visualSummary = planPayload == null ? null : planPayload.visualSummary();
            String negativePrompt = planPayload == null ? null : planPayload.negativePrompt();
            String displayText = planPayload == null
                    ? "已通过 YMAPI chat.completions 返回场景图。"
                    : planPayload.displayText();

            return new SceneImageExecutionResult(
                    resolveSceneImageProviderName(),
                    actualModel,
                    requestUri,
                    imagePayload.imageBase64(),
                    imagePayload.imageUrl(),
                    revisedPrompt,
                    visualSummary,
                    negativePrompt,
                    displayText,
                    "direct_image"
            );
        }

        if (planPayload != null) {
            return new SceneImageExecutionResult(
                    resolveSceneImageProviderName(),
                    actualModel,
                    requestUri,
                    null,
                    null,
                    planPayload.revisedPrompt(),
                    planPayload.visualSummary(),
                    planPayload.negativePrompt(),
                    planPayload.displayText(),
                    "prompt_only"
            );
        }

        if (StringUtils.hasText(finishReason) && "max_tokens".equalsIgnoreCase(finishReason)) {
            throw new BusinessException("当前 OneAPI 已返回 200，但输出在图片落地前被 max_tokens 截断了；请提高 SCENE_IMAGE_CHAT_MAX_TOKENS，建议至少 2000");
        }

        throw new BusinessException("当前 YMAPI 通道既没有返回图片，也没有返回可用的场景方案");
    }

    private SceneImagePlanPayload parseSceneImagePlanPayload(String rawText) {
        String normalizedText = stripMarkdownCodeFence(rawText);
        Map<String, Object> parsed = parseJsonObject(normalizedText);
        if (parsed == null) {
            parsed = extractStructuredJsonObject(normalizedText);
        }

        if (parsed == null || parsed.isEmpty()) {
            String fallbackText = trimToNull(normalizedText);
            return new SceneImagePlanPayload(
                    fallbackText,
                    fallbackText,
                    null,
                    "当前 YMAPI 通道未直接返回图片，已改为返回可继续绘图的场景方案。"
            );
        }

        String visualSummary = trimToNull(firstNonBlank(
                objectToString(parsed.get("visual_summary")),
                objectToString(parsed.get("scene_summary")),
                objectToString(parsed.get("summary"))
        ));
        String revisedPrompt = trimToNull(firstNonBlank(
                objectToString(parsed.get("revised_prompt")),
                objectToString(parsed.get("image_prompt")),
                objectToString(parsed.get("prompt"))
        ));
        String negativePrompt = trimToNull(firstNonBlank(
                objectToString(parsed.get("negative_prompt")),
                objectToString(parsed.get("avoid")),
                objectToString(parsed.get("negative"))
        ));
        String displayText = trimToNull(firstNonBlank(
                objectToString(parsed.get("display_text")),
                objectToString(parsed.get("message"))
        ));

        if (!StringUtils.hasText(visualSummary)) {
            visualSummary = revisedPrompt;
        }
        if (!StringUtils.hasText(revisedPrompt)) {
            revisedPrompt = visualSummary;
        }
        if (!StringUtils.hasText(displayText)) {
            displayText = "当前 YMAPI 通道未直接返回图片，已改为返回可继续绘图的场景方案。";
        }

        return new SceneImagePlanPayload(
                visualSummary,
                revisedPrompt,
                negativePrompt,
                displayText
        );
    }

    private GeneratedImagePayload parseSceneImageResponse(String responseBody, String protocol) throws Exception {
        if ("images-generations".equals(protocol)) {
            return parseImageGenerationResponse(responseBody);
        }
        return parseChatImageResponse(responseBody);
    }

    private SceneImageExecutionResult parseSceneImageResponseResult(String responseBody,
                                                                    String actualModel,
                                                                    URI requestUri,
                                                                    String protocol) throws Exception {
        if ("images-generations".equals(protocol)) {
            GeneratedImagePayload payload = parseImageGenerationResponse(responseBody);
            return new SceneImageExecutionResult(
                    resolveSceneImageProviderName(),
                    actualModel,
                    requestUri,
                    payload.imageBase64(),
                    payload.imageUrl(),
                    payload.revisedPrompt(),
                    null,
                    null,
                    "已通过图片生成接口返回场景图。",
                    "direct_image"
            );
        }
        return parseSceneImageChatResponse(responseBody, actualModel, requestUri);
    }

    private GeneratedImagePayload parseImageGenerationResponse(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
        if (data == null || data.isEmpty()) {
            throw new BusinessException("Gemini 返回图片内容为空");
        }

        Map<String, Object> firstData = data.get(0);
        String imageBase64 = objectToString(firstData.get("b64_json"));
        String imageUrl = objectToString(firstData.get("url"));
        String revisedPrompt = objectToString(firstData.get("revised_prompt"));

        if (!StringUtils.hasText(imageBase64) && !StringUtils.hasText(imageUrl)) {
            throw new BusinessException("Gemini 返回图片内容为空");
        }

        return new GeneratedImagePayload(
                sanitizeBase64(imageBase64),
                StringUtils.hasText(imageUrl) ? imageUrl.trim() : null,
                StringUtils.hasText(revisedPrompt) ? revisedPrompt.trim() : null
        );
    }

    @SuppressWarnings("unchecked")
    private GeneratedImagePayload parseChatImageResponse(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("聊天协议未返回可用结果");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = firstChoice.get("message") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : null;
        if (message == null || message.isEmpty()) {
            throw new BusinessException("聊天协议返回内容格式错误");
        }

        GeneratedImagePayload payload = extractImagePayloadFromChatMessage(message);
        if (payload != null) {
            return payload;
        }

        String text = extractMessageText(message.get("content"));
        if (StringUtils.hasText(text)) {
            throw new BusinessException("当前 OneAPI /chat/completions 只返回了文本，没有图片内容；请检查 OneAPI 是否已按文档为该模型注入 generationConfig.responseModalities，并正确把 inlineData 转成 image_url");
        }

        throw new BusinessException("当前 OneAPI /chat/completions 返回为空，未携带任何图片内容；请检查 OneAPI 网关是否正确透传 Gemini 图片响应");
    }

    @SuppressWarnings("unchecked")
    private GeneratedImagePayload extractImagePayloadFromChatMessage(Map<String, Object> message) {
        String directImageBase64 = sanitizeBase64(objectToString(message.get("b64_json")));
        String directImageUrl = normalizeImageUrl(objectToString(message.get("image_url")));
        String directRevisedPrompt = trimToNull(objectToString(message.get("revised_prompt")));
        if (StringUtils.hasText(directImageBase64) || StringUtils.hasText(directImageUrl)) {
            return new GeneratedImagePayload(directImageBase64, directImageUrl, directRevisedPrompt);
        }

        Object images = message.get("images");
        if (images instanceof List<?> list) {
            for (Object item : list) {
                GeneratedImagePayload payload = extractImagePayloadFromUnknownItem(item);
                if (payload != null) {
                    return payload;
                }
            }
        }

        Object content = message.get("content");
        if (content instanceof List<?> list) {
            return extractImagePayloadFromContentItems(list);
        }
        if (content instanceof Map<?, ?> contentMap) {
            Object parts = contentMap.get("parts");
            if (parts instanceof List<?> list) {
                return extractImagePayloadFromContentItems(list);
            }
        }

        return null;
    }

    private GeneratedImagePayload extractImagePayloadFromContentItems(List<?> items) {
        String revisedPrompt = null;
        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                String type = objectToString(map.get("type")).trim().toLowerCase();
                if (!StringUtils.hasText(revisedPrompt) && ("text".equals(type) || "output_text".equals(type))) {
                    revisedPrompt = trimToNull(objectToString(map.get("text")));
                }
            }
            GeneratedImagePayload payload = extractImagePayloadFromUnknownItem(item);
            if (payload != null) {
                return new GeneratedImagePayload(
                        payload.imageBase64(),
                        payload.imageUrl(),
                        payload.revisedPrompt() != null ? payload.revisedPrompt() : revisedPrompt
                );
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private GeneratedImagePayload extractImagePayloadFromUnknownItem(Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof String text) {
            String normalizedText = text.trim();
            if (normalizedText.startsWith("data:image/")) {
                return new GeneratedImagePayload(extractBase64FromDataUrl(normalizedText), normalizedText, null);
            }
            if (normalizedText.startsWith("http://") || normalizedText.startsWith("https://")) {
                return new GeneratedImagePayload(null, normalizedText, null);
            }
            return null;
        }
        if (!(item instanceof Map<?, ?> rawMap)) {
            return null;
        }

        Map<String, Object> map = (Map<String, Object>) rawMap;
        String imageBase64 = sanitizeBase64(objectToString(map.get("b64_json")));
        if (!StringUtils.hasText(imageBase64)) {
            imageBase64 = sanitizeBase64(objectToString(map.get("image_base64")));
        }
        if (!StringUtils.hasText(imageBase64)) {
            Object inlineData = map.get("inline_data");
            if (inlineData instanceof Map<?, ?> inlineMap) {
                imageBase64 = sanitizeBase64(objectToString(inlineMap.get("data")));
            }
        }
        if (!StringUtils.hasText(imageBase64)) {
            Object inlineData = map.get("inlineData");
            if (inlineData instanceof Map<?, ?> inlineMap) {
                imageBase64 = sanitizeBase64(objectToString(inlineMap.get("data")));
            }
        }

        String imageUrl = normalizeImageUrl(objectToString(map.get("url")));
        if (!StringUtils.hasText(imageUrl)) {
            Object imageUrlValue = map.get("image_url");
            if (imageUrlValue instanceof Map<?, ?> imageUrlMap) {
                imageUrl = normalizeImageUrl(objectToString(imageUrlMap.get("url")));
            } else {
                imageUrl = normalizeImageUrl(objectToString(imageUrlValue));
            }
        }

        if (!StringUtils.hasText(imageBase64) && !StringUtils.hasText(imageUrl)) {
            return null;
        }

        String revisedPrompt = trimToNull(objectToString(map.get("revised_prompt")));
        return new GeneratedImagePayload(imageBase64, imageUrl, revisedPrompt);
    }

    private String extractMessageText(Object contentObj) {
        if (contentObj == null) {
            return "";
        }
        if (contentObj instanceof String content) {
            return content.trim();
        }
        if (contentObj instanceof List<?> items) {
            StringBuilder builder = new StringBuilder();
            for (Object item : items) {
                if (item instanceof Map<?, ?> map) {
                    Object text = map.get("text");
                    if (text != null) {
                        if (!builder.isEmpty()) {
                            builder.append('\n');
                        }
                        builder.append(text);
                    }
                }
            }
            return builder.toString().trim();
        }
        if (contentObj instanceof Map<?, ?> map) {
            Object parts = map.get("parts");
            if (parts instanceof List<?> items) {
                return extractMessageText(items);
            }
        }
        return String.valueOf(contentObj).trim();
    }

    private String buildFailureMessage(int statusCode, String responseBody, String targetModel, URI requestUri) {
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

    private String buildSecondStageSceneImageFailureMessage(int statusCode,
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

    private String buildSceneImageFailureMessage(int statusCode, String responseBody, String targetModel, URI requestUri) {
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

    private String resolveSceneImageProviderName() {
        return StringUtils.hasText(sceneImageProvider) ? sceneImageProvider.trim() : "one-api-chat-completions";
    }

    private boolean isGoogleSceneImageProvider() {
        String normalized = resolveSceneImageProviderName().trim().toLowerCase();
        return "google-openai-compatible".equals(normalized)
                || "google-openai".equals(normalized)
                || "google-gemini".equals(normalized)
                || "google".equals(normalized);
    }

    private String resolveSceneImageApiKey() {
        if (StringUtils.hasText(sceneImageApiKey)) {
            return sceneImageApiKey.trim();
        }
        if (StringUtils.hasText(apiKey)) {
            return apiKey.trim();
        }
        return "";
    }

    private void validateGoogleOfficialSceneImageConfiguration() {
        if (!StringUtils.hasText(sceneImageApiKey)) {
            throw new BusinessException("Google 图片生成 API Key 未配置，请先设置 SCENE_IMAGE_API_KEY");
        }
        if (sceneImageApiKey.trim().startsWith("sk-")) {
            throw new BusinessException("当前已切换为 Google 官方图片模型，但你提供的是 sk- 开头的 One-API token。Google 官方图片模型需要 Google Gemini API Key，不能继续使用这把 sk key。");
        }
        if (resolveSceneImageModelsToTry().isEmpty()) {
            throw new BusinessException("Google 图片生成模型未配置，请先设置 SCENE_IMAGE_MODEL");
        }
    }

    private String resolveSceneImageApiBaseUrl() {
        if (StringUtils.hasText(sceneImageApiBaseUrl)) {
            return sceneImageApiBaseUrl.trim();
        }
        if (isGoogleSceneImageProvider()) {
            return "https://generativelanguage.googleapis.com/v1beta/openai";
        }
        return "";
    }

    private String normalizeSceneImageBaseUrl(String baseUrl, String protocol) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if ("images-generations".equals(protocol) && normalized.endsWith("/images/generations")) {
            normalized = normalized.substring(0, normalized.length() - "/images/generations".length());
        }
        if ("chat-completions".equals(protocol) && normalized.endsWith("/chat/completions")) {
            normalized = normalized.substring(0, normalized.length() - "/chat/completions".length());
        }
        if (normalized.contains("generativelanguage.googleapis.com")) {
            if (normalized.endsWith("/v1beta/openai")) {
                return normalized;
            }
            if (normalized.endsWith("/openai")) {
                return normalized;
            }
            if (normalized.endsWith("/v1beta")) {
                return normalized + "/openai";
            }
            return normalized + "/v1beta/openai";
        }
        return normalizeBaseUrl(normalized);
    }

    private String resolveSceneImageProtocol() {
        String normalized = sceneImageProtocol == null ? "" : sceneImageProtocol.trim().toLowerCase();
        if ("disabled".equals(normalized) || "none".equals(normalized) || "doc-only".equals(normalized)) {
            return "disabled";
        }
        if ("images".equals(normalized)
                || "images.generate".equals(normalized)
                || "images/generations".equals(normalized)
                || "images-generations".equals(normalized)) {
            return "images-generations";
        }
        return "chat-completions";
    }

    private HttpStatus mapUpstreamFailureStatus(int statusCode, String responseBody) {
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

    private Map<String, Object> tryParseJson(String text) {
        String normalizedText = stripMarkdownCodeFence(text);
        Map<String, Object> direct = parseJsonObject(normalizedText);
        if (direct != null) {
            return direct;
        }

        Map<String, Object> extracted = extractStructuredJsonObject(normalizedText);
        if (extracted != null) {
            return extracted;
        }

        log.warn("Gemini returned non-JSON content, using text fallback");
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("hasFace", null);
        fallback.put("faceCount", null);
        fallback.put("visualSummary", normalizedText);
        fallback.put("observedFeatures", List.of());
        fallback.put("physiognomyReport", Map.of(
                "forehead", "",
                "eyesAndBrows", "",
                "nose", "",
                "mouthAndChin", "",
                "overallImpression", ""
        ));
        fallback.put("imageQuality", "");
        fallback.put("reportSummary", normalizedText);
        fallback.put("suggestions", List.of());
        fallback.put("disclaimer", "结果为文本回退模式，仅供文化娱乐参考，不包含身份识别，也不构成事实判断。");
        return fallback;
    }

    private Map<String, Object> parseJsonObject(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, Object> extractStructuredJsonObject(String text) {
        List<String> candidates = extractJsonObjectCandidates(text);
        Map<String, Object> best = null;
        int bestScore = -1;

        for (String candidate : candidates) {
            Map<String, Object> parsed = parseJsonObject(candidate);
            if (parsed == null || parsed.isEmpty()) {
                continue;
            }
            int score = scoreFaceAnalysisPayload(parsed);
            if (score > bestScore) {
                best = parsed;
                bestScore = score;
            }
        }

        return bestScore > 0 ? best : null;
    }

    private List<String> extractJsonObjectCandidates(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        List<String> candidates = new ArrayList<>();
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        int startIndex = -1;

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\') {
                escaped = true;
                continue;
            }

            if (current == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (current == '{') {
                if (depth == 0) {
                    startIndex = index;
                }
                depth++;
                continue;
            }

            if (current == '}') {
                if (depth <= 0) {
                    continue;
                }
                depth--;
                if (depth == 0 && startIndex >= 0) {
                    candidates.add(text.substring(startIndex, index + 1));
                    startIndex = -1;
                }
            }
        }

        return candidates;
    }

    private int scoreFaceAnalysisPayload(Map<String, Object> payload) {
        int score = 0;
        if (payload.containsKey("hasFace")) score += 3;
        if (payload.containsKey("faceCount")) score += 2;
        if (payload.containsKey("visualSummary")) score += 4;
        if (payload.containsKey("observedFeatures")) score += 4;
        if (payload.containsKey("physiognomyReport")) score += 4;
        if (payload.containsKey("imageQuality")) score += 2;
        if (payload.containsKey("reportSummary")) score += 4;
        if (payload.containsKey("suggestions")) score += 2;
        if (payload.containsKey("disclaimer")) score += 2;
        return score;
    }

    private String stripMarkdownCodeFence(String text) {
        String stripped = text == null ? "" : text.trim();
        if (!stripped.startsWith("```")) {
            return stripped;
        }

        List<String> lines = stripped.lines().toList();
        if (lines.size() >= 2 && lines.get(lines.size() - 1).trim().startsWith("```")) {
            return String.join("\n", lines.subList(1, lines.size() - 1)).trim();
        }
        return stripped;
    }

    private String resolveSceneCategory(String question, String interpretation) {
        String normalized = (objectToString(question) + " " + objectToString(interpretation)).toLowerCase();
        if (normalized.contains("财") || normalized.contains("钱") || normalized.contains("投资") || normalized.contains("生意")) {
            return "财运机遇";
        }
        if (normalized.contains("事业") || normalized.contains("工作") || normalized.contains("升职") || normalized.contains("求职") || normalized.contains("职场")) {
            return "事业抉择";
        }
        if (normalized.contains("感情") || normalized.contains("恋爱") || normalized.contains("婚姻") || normalized.contains("复合") || normalized.contains("暧昧")) {
            return "感情关系";
        }
        if (normalized.contains("考试") || normalized.contains("学习") || normalized.contains("学业") || normalized.contains("读书")) {
            return "学业前路";
        }
        if (normalized.contains("健康") || normalized.contains("身体") || normalized.contains("病") || normalized.contains("恢复")) {
            return "身心调养";
        }
        if (normalized.contains("出行") || normalized.contains("旅行") || normalized.contains("远行") || normalized.contains("搬家")) {
            return "出行变动";
        }
        return "人生处境";
    }

    private String resolveSceneSuggestion(String sceneCategory) {
        return switch (sceneCategory) {
            case "财运机遇" -> "账册、商铺、夜色城市与微亮金光交织，表现机会与风险并存";
            case "事业抉择" -> "办公室、会议室、楼宇天台或深夜案头，表现压力、选择与上升势能";
            case "感情关系" -> "雨夜街头、窗边双人剪影或留白中的单人凝望，表现靠近与犹疑";
            case "学业前路" -> "书桌、灯火、纸笔与清晨微光，表现专注、等待结果与突破";
            case "身心调养" -> "晨雾、山石、静室、茶烟与柔和天光，表现恢复、沉淀与修整";
            case "出行变动" -> "车站、长路、桥梁、风起云动的远景，表现行程与命运转折";
            default -> "处在人生十字路口的当代人物，周遭环境映照内心变化与命运流向";
        };
    }

    private String resolveStyleSuggestion(String sceneCategory) {
        return switch (sceneCategory) {
            case "财运机遇" -> "中式电影感写实插画，冷暖金青对比，细节精致，带流动财气与卦象微光";
            case "事业抉择" -> "现代东方电影感，城市夜景与室内光影交错，克制而有压迫感";
            case "感情关系" -> "诗意写实风，氤氲雾气与柔和侧光，情绪含蓄但张力明显";
            case "学业前路" -> "安静、清透、带一点灵光降临的东方氛围，纸页与灯火细节明确";
            case "身心调养" -> "东方疗愈感美学，空气通透，柔光、山水、香雾与静定气场";
            case "出行变动" -> "电影分镜感，远景开阔，风与路的方向感强，玄学光纹隐约浮现";
            default -> "东方神秘现实主义，古意与当代场景融合，色调克制、空灵且有命运感";
        };
    }

    private String resolveHexagramName(YijingSceneImageRequest.HexagramSnapshot hexagram) {
        if (hexagram == null) {
            return "未知卦象";
        }
        String chinese = objectToString(hexagram.getChinese());
        String name = objectToString(hexagram.getName());
        if (StringUtils.hasText(chinese) && StringUtils.hasText(name) && !chinese.equals(name)) {
            return chinese + "（" + name + "）";
        }
        return StringUtils.hasText(chinese) ? chinese : (StringUtils.hasText(name) ? name : "未知卦象");
    }

    private String extractHexagramMeaning(YijingSceneImageRequest.HexagramSnapshot hexagram) {
        if (hexagram == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(hexagram.getMeaning())) {
            builder.append(hexagram.getMeaning().trim());
        }
        if (StringUtils.hasText(hexagram.getJudgment())) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append("卦辞：").append(hexagram.getJudgment().trim());
        }
        return builder.toString();
    }

    private String extractHexagramImage(YijingSceneImageRequest.HexagramSnapshot hexagram) {
        if (hexagram == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(hexagram.getImage())) {
            builder.append(hexagram.getImage().trim());
        }
        if (StringUtils.hasText(hexagram.getSymbol())) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append("卦象符号：").append(hexagram.getSymbol().trim());
        }
        return builder.toString();
    }

    private String normalizePromptText(String value, int maxLength) {
        String normalized = objectToString(value).replaceAll("\\s+", " ").trim();
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        return normalized.length() > maxLength ? normalized.substring(0, maxLength) + "..." : normalized;
    }

    private String objectToString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null ? "" : mimeType.trim().toLowerCase();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String buildSecondStageDrawingPrompt(String revisedPrompt, String negativePrompt, String originalPrompt) {
        String basePrompt = StringUtils.hasText(revisedPrompt) ? revisedPrompt.trim() : originalPrompt;
        String avoidPrompt = trimToNull(negativePrompt);
        if (!StringUtils.hasText(avoidPrompt)) {
            return basePrompt;
        }
        return basePrompt + "\n\n避免元素：" + avoidPrompt;
    }

    private boolean hasMoreSecondStageCandidates(List<String> protocols,
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

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeImageUrl(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (trimmed.startsWith("data:image/")) {
            return trimmed;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return null;
    }

    private String extractBase64FromDataUrl(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null || !trimmed.startsWith("data:image/")) {
            return "";
        }
        int commaIndex = trimmed.indexOf(',');
        if (commaIndex < 0 || commaIndex >= trimmed.length() - 1) {
            return "";
        }
        return sanitizeBase64(trimmed.substring(commaIndex + 1));
    }

    private String sanitizeBase64(String rawBase64) {
        if (!StringUtils.hasText(rawBase64)) {
            return "";
        }
        String value = rawBase64.trim();
        int commaIndex = value.indexOf(',');
        if (value.startsWith("data:") && commaIndex >= 0) {
            value = value.substring(commaIndex + 1);
        }
        return value.replaceAll("\\s+", "");
    }

    private long estimateDecodedBytes(String base64) {
        int length = base64.length();
        if (length == 0) {
            return 0;
        }
        int padding = 0;
        if (base64.endsWith("==")) {
            padding = 2;
        } else if (base64.endsWith("=")) {
            padding = 1;
        }
        return (length * 3L) / 4L - padding;
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            normalized = normalized.substring(0, normalized.length() - "/chat/completions".length());
        }
        if (!normalized.endsWith("/v1")) {
            normalized = normalized + "/v1";
        }
        return normalized;
    }

    private String abbreviate(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
    }

    private record VisionExecutionResult(String model, URI uri, String responseBody) {
    }

    private record SceneImageExecutionResult(String provider,
                                             String model,
                                             URI uri,
                                             String imageBase64,
                                             String imageUrl,
                                             String revisedPrompt,
                                             String visualSummary,
                                             String negativePrompt,
                                             String displayText,
                                             String generationMode) {
    }

    private record SceneImagePlanPayload(String visualSummary,
                                         String revisedPrompt,
                                         String negativePrompt,
                                         String displayText) {
    }

    private record GeneratedImagePayload(String imageBase64, String imageUrl, String revisedPrompt) {
    }
}
