# Gemini 调用链路详解

## 文档目的

这份文档用于完整说明当前项目中调用 Gemini 的实际链路，包括：

- 前端入口
- 后端控制器入口
- `GeminiService` 的公共调用骨架
- 不同业务场景的分支处理
- OneAPI/YMAPI 兼容层的请求构造
- 图片与文本响应的解析规则
- 失败重试、错误映射和日志定位方法

这份文档覆盖当前项目里所有已经接入 `GeminiService` 的能力，不只限于单个页面。

---

## 一、当前项目里 Gemini 的使用范围

目前 Gemini 主要用于两类能力：

### 1. 人像文化分析

对应接口：

- `POST /api/gemini/face-analyze`
- `POST /api/gemini/probe/text`
- `POST /api/gemini/probe/vision`

相关文件：

- `src/main/java/com/example/demo/controller/GeminiController.java`
- `src/main/java/com/example/demo/service/GeminiService.java`
- `src/main/java/com/example/demo/dto/request/gemini/GeminiFaceAnalysisRequest.java`
- `src/main/java/com/example/demo/dto/response/gemini/GeminiFaceAnalysisResponse.java`
- `src/main/java/com/example/demo/dto/response/gemini/GeminiFaceResponseMapper.java`

前端页面：

- `src-frontend/pages/GeminiFacePage.jsx`

### 2. 易经场景图生成

对应接口：

- `POST /api/standalone/yijing/scene-image`

相关文件：

- `src/main/java/com/example/demo/controller/StandaloneYijingController.java`
- `src/main/java/com/example/demo/service/GeminiService.java`
- `src/main/java/com/example/demo/dto/request/yijing/YijingSceneImageRequest.java`
- `src/main/java/com/example/demo/dto/response/yijing/YijingSceneImageResponse.java`

前端页面：

- `src-frontend/pages/YijingPage.jsx`

---

## 二、Gemini 链路总览

从整体上看，项目中的 Gemini 调用链路遵循同一个骨架：

1. 前端页面收集用户输入
2. 前端通过 `api/index.js` 发起 HTTP 请求
3. Spring Controller 接口接收请求 DTO
4. Controller 调用 `GeminiService`
5. `GeminiService` 做配置校验、模型选择、请求体构造
6. `GeminiService` 使用 `HttpClient` 调 OneAPI/Gemini 兼容接口
7. 收到响应后按业务场景解析文本或图片
8. 成功时转换为业务 DTO 返回
9. 失败时抛 `BusinessException`
10. Controller 或全局异常处理器把错误序列化为统一响应

可以概括为：

`前端页面 -> 前端 API -> Controller -> GeminiService -> HttpClient -> OneAPI/Gemini -> 响应解析 -> DTO -> 前端展示`

---

## 三、配置层是怎么决定 Gemini 走哪条通道的

核心配置位于：

- `src/main/resources/application.yml`
- `src/main/resources/application-example.yml`

项目里 Gemini 分成两组配置：

### 1. 通用 Gemini 配置

前缀：

- `gemini.*`

主要字段：

- `gemini.api.key`
- `gemini.api.base-url`
- `gemini.text-model`
- `gemini.vision-model`
- `gemini.vision-models`
- `gemini.vision-payload-formats`
- `gemini.temperature`
- `gemini.max-tokens`
- `gemini.max-image-bytes`
- `gemini.image-model`
- `gemini.image-models`

默认用途：

- 人像分析
- 文本探针
- 视觉探针
- 部分图片模型候选回退

### 2. 场景图专用配置

前缀：

- `scene-image.*`

主要字段：

- `scene-image.provider`
- `scene-image.protocol`
- `scene-image.api.key`
- `scene-image.api.base-url`
- `scene-image.model`
- `scene-image.models`
- `scene-image.response-format`
- `scene-image.size`
- `scene-image.count`
- `scene-image.chat-max-tokens`

默认用途：

- 易经场景图生成
- 场景图的二次绘图

### 3. 当前默认走法

当前项目的默认现实路径是：

- 通用 Gemini：走 OneAPI 兼容 `/v1/chat/completions`
- 场景图：优先走 `scene-image` 独立通道
- 场景图协议默认是 `chat-completions`
- 场景图模型默认是 `gemini-3-pro-image-preview`

也就是说：

- 人像分析和探针依赖 `gemini.*`
- 易经场景图依赖 `scene-image.*`

这两套配置可以相同，也可以拆开。

---

## 四、控制器入口层

### 1. GeminiController

文件：

- `src/main/java/com/example/demo/controller/GeminiController.java`

对外暴露三个接口：

#### `POST /api/gemini/face-analyze`

作用：

- 接收图片 base64、图片类型、用户提示词
- 调用 `geminiService.analyzeFace(request)`
- 返回 `Result<GeminiFaceAnalysisResponse>`

#### `POST /api/gemini/probe/text`

作用：

- 给文本模型发最小探针请求
- 检查当前通道能否正常返回文本

#### `POST /api/gemini/probe/vision`

作用：

- 给视觉模型发最小探针请求
- 检查当前通道能否识别图片输入

### 2. StandaloneYijingController

文件：

- `src/main/java/com/example/demo/controller/StandaloneYijingController.java`

Gemini 相关入口是：

#### `POST /api/standalone/yijing/scene-image`

作用：

- 接收易经问题、卦象、动爻、解读摘要等上下文
- 调用 `geminiService.generateYijingSceneImage(request)`
- 返回 `YijingSceneImageResponse`

---

## 五、GeminiService 的公共基础能力

文件：

- `src/main/java/com/example/demo/service/GeminiService.java`

这是整个 Gemini 链路的核心。

### 1. 注入的基础依赖

服务内部依赖两项基础组件：

- `HttpClient`
- `ObjectMapper`

用途分别是：

- `HttpClient` 负责发 HTTP 请求到 OneAPI/Gemini
- `ObjectMapper` 负责把请求体序列化为 JSON，并把响应体反序列化为 `Map`

### 2. 统一的配置字段注入

`GeminiService` 使用 `@Value` 从配置里注入全部模型、协议、token 和参数，之后每个业务分支都只从这里取配置，不直接读环境变量。

### 3. 公共 URI 构造逻辑

主要方法：

- `buildRequestUri()`
- `buildSceneImageRequestUri()`
- `buildSecondStageSceneImageRequestUri(protocol)`

职责：

- 通用 Gemini 调用默认指向 `/chat/completions`
- 场景图根据 `scene-image.protocol` 决定是：
  - `/chat/completions`
  - `/images/generations`

### 4. 公共鉴权头构造逻辑

主要方法：

- `buildAuthorizationHeaders()`
- `buildSceneImageAuthorizationHeaders()`
- `buildSecondStageAuthorizationHeaders()`

职责：

- 把 API Key 挂到 `Authorization: Bearer xxx`
- 场景图允许与通用 Gemini 使用不同 token

### 5. 公共异常模型

Gemini 链路中主要通过 `BusinessException` 向外抛错。

好处是：

- 可以带业务可读错误消息
- 可以带 HTTP 状态码
- 可以带失败详情对象 `GeminiFailureDetails`

这让前端既能展示简洁报错，也能在日志里保留完整定位信息。

---

## 六、人像分析链路

### 1. 前端入口

页面文件：

- `src-frontend/pages/GeminiFacePage.jsx`

调用入口：

- `geminiApi.analyzeFace(imageBase64, mimeType, prompt)`

定义位置：

- `src-frontend/api/index.js`

前端工作流程：

1. 用户上传图片
2. 前端校验文件格式和大小
3. 转成 base64
4. 调 `/api/gemini/face-analyze`
5. 用 `unwrapApiData(response)` 取后端 `data`
6. 将结果渲染到页面

### 2. 后端入口

接口：

- `POST /api/gemini/face-analyze`

DTO：

- `GeminiFaceAnalysisRequest`

包含字段：

- `imageBase64`
- `mimeType`
- `prompt`

### 3. `analyzeFace()` 的内部流程

方法：

- `GeminiService.analyzeFace(GeminiFaceAnalysisRequest request)`

执行顺序如下：

#### 第一步：校验通用 OneAPI 配置

方法：

- `validateOneApiConfiguration()`

会检查：

- `apiKey` 是否存在
- key 是否以 `sk-` 开头
- `visionModel` 是否存在
- `apiBaseUrl` 是否存在

如果缺失，直接抛业务异常，不发请求。

#### 第二步：校验图片输入

会处理：

- MIME 类型标准化
- base64 清洗
- 图片解码体积估算

只允许：

- `image/jpeg`
- `image/png`
- `image/webp`

还会校验：

- 图片不能空
- 图片不能超过 `gemini.max-image-bytes`

#### 第三步：构造增强提示词

方法：

- `buildEnhancedPrompt(request.getPrompt())`

这里做了两层事：

1. 如果用户没传 prompt，则使用默认的人像文化分析 prompt
2. 即使用户传了 prompt，也会被包进一个安全边界很强的结构化大 prompt 中

这个大 prompt 的目标是：

- 禁止身份识别
- 禁止对敏感属性做事实判断
- 只允许做“传统文化娱乐性说明”
- 强制要求输出 JSON

#### 第四步：执行视觉请求

方法：

- `executeVisionRequest(...)`

执行时会：

1. 解析视觉模型候选列表 `resolveVisionModelsToTry()`
2. 解析视觉 payload 格式候选列表 `resolveVisionPayloadFormatsToTry()`
3. 双层循环尝试：
   - 外层切换模型
   - 内层切换图片 payload 格式

### 4. 视觉请求体怎么构造

方法：

- `buildVisionRequestBody(...)`

当前项目采用的是 OpenAI 兼容的多模态消息格式。

核心结构是：

- `messages`
- `content` 是数组
- 一个文本 part
- 一个图片 part

图片 part 会根据 payload 格式有两种写法：

#### 格式 A：`openai-image-url`

```json
{
  "type": "image_url",
  "image_url": {
    "url": "data:image/png;base64,..."
  }
}
```

#### 格式 B：`openai-image-url-string`

```json
{
  "type": "image_url",
  "image_url": "data:image/png;base64,..."
}
```

这样做的原因是：

- 不同 OneAPI 网关对 OpenAI 兼容格式支持不完全一致
- 有的要对象结构
- 有的要纯字符串

所以项目内置了格式切换和失败重试。

### 5. 上游返回后怎么解析

请求成功后，`analyzeFace()` 不直接把原始 JSON 返回给前端，而是分两步处理：

#### 第一步：提取文本

方法：

- `parseResponse(responseBody, actualModel)`

会做这些事情：

- 解析 `choices[0].message`
- 从 `content` 中提取文本
- 尝试把文本解析成 JSON
- 如果 Gemini 没有严格返回 JSON，则尝试从文本里抽取 JSON 片段
- 如果最终仍然不是 JSON，则进入文本兜底模式

#### 第二步：Map 转业务 DTO

方法：

- `GeminiFaceResponseMapper.fromMap(...)`

作用：

- 把松散的 `Map<String, Object>` 转成 `GeminiFaceAnalysisResponse`
- 规范字段类型
- 构造 `detailSections`
- 生成更适合前端展示的结构

最终前端拿到的不是“生肉 JSON”，而是规范化后的业务 DTO。

### 6. 人像分析的失败重试策略

在 `executeVisionRequest(...)` 中，遇到以下情况时会尝试下一个候选：

- 当前 payload 格式不兼容
- 当前模型不兼容
- 某些上游返回说明存在兼容性问题

是否切换由这两个方法控制：

- `shouldTryNextVisionModel(...)`
- `mapUpstreamFailureStatus(...)`

---

## 七、文本探针链路

### 1. 接口用途

接口：

- `POST /api/gemini/probe/text`

方法：

- `GeminiService.probeText(String prompt)`

这个接口不是业务功能，而是用于诊断：

- key 是否可用
- base URL 是否正确
- 当前 text model 是否可用
- OneAPI 是否能正常透传文本

### 2. 请求构造

方法：

- `buildTextProbeRequestBody(prompt)`

特点：

- 使用 `textModel`
- 只发送一条简单 user message
- `max_tokens` 会限制在较小值

### 3. 响应返回

执行方法：

- `executeProbe(model, requestBody, "text")`

返回 DTO：

- `GeminiProbeResponse`

包含：

- `model`
- `uri`
- `content`
- `contentLength`

这个接口非常适合快速判断“通道通不通”。

---

## 八、视觉探针链路

### 1. 接口用途

接口：

- `POST /api/gemini/probe/vision`

方法：

- `GeminiService.probeVision(GeminiFaceAnalysisRequest request)`

这个接口用于诊断：

- 图片是否能顺利送进 OneAPI
- 当前视觉模型是否可用
- 上游是否能识别多模态消息

### 2. 和正式人像分析的区别

它和 `analyzeFace()` 很像，但更轻：

- prompt 更短
- token 更少
- 返回的是 `GeminiProbeResponse`
- 只关心“能不能读图”，不关心文化报告结构

---

## 九、易经场景图链路

这是当前 Gemini 链路里最复杂的一条。

### 1. 前端入口

页面：

- `src-frontend/pages/YijingPage.jsx`

触发函数：

- `handleGenerateSceneImage()`

前端会把这些信息发给后端：

- 用户问题
- 起卦方式
- 解读摘要
- 解读提示
- 动爻数组
- 本卦快照
- 变卦快照

调用 API：

- `yijingApi.generateSceneImage(data)`

### 2. 后端入口

控制器：

- `StandaloneYijingController.generateSceneImage(...)`

服务方法：

- `GeminiService.generateYijingSceneImage(request)`

### 3. 第一层：配置校验

方法：

- `validateSceneImageGenerationConfiguration()`

这里会根据 provider 分两条路：

#### 路径 A：OneAPI/YMAPI 兼容通道

会校验：

- `sceneImageApiKey` 或回退后的 key 是否存在
- key 是否有效
- `sceneImageApiBaseUrl` 是否存在
- 是否至少有一个可用模型

#### 路径 B：Google 官方图片提供方

通过 `isGoogleSceneImageProvider()` 判断。

当 provider 是以下值之一时会走 Google 官方分支：

- `google-openai-compatible`
- `google-openai`
- `google-gemini`
- `google`

此时会进入：

- `validateGoogleOfficialSceneImageConfiguration()`

主要要求：

- `SCENE_IMAGE_API_KEY` 必须是真正的 Google Gemini key
- 不能还是 `sk-` 开头的 OneAPI token

### 4. 第二层：构造易经场景图 prompt

方法：

- `buildYijingSceneImagePrompt(request)`

这个 prompt 会融合：

- 问题
- 场景类别
- 本卦名
- 卦意
- 卦象意境
- 关键词
- 解读摘要
- 解读提示
- 动爻
- 变卦
- 画面要求
- 风格建议

它的目标不是简单让模型“画图”，而是：

- 优先直接返回图片
- 如果当前通道不能直接返回图，则严格输出一个 JSON 场景方案

也正因为这个设计，场景图链路存在：

- `direct_image`
- `prompt_only`

这两种中间态。

### 5. 第三层：执行第一阶段场景图请求

方法：

- `executeSceneImageGenerationRequest(prompt)`

它会先取模型列表：

- `resolveSceneImageModelsToTry()`

然后按顺序尝试模型。

### 6. 场景图请求体怎么构造

总入口：

- `buildSceneImageGenerationRequestBody(modelName, prompt)`

根据协议不同分两种：

#### 协议 A：`images-generations`

方法：

- `buildSceneImageImagesRequestBody(...)`

会发送：

- `model`
- `prompt`
- `response_format`
- `n`
- `size`

#### 协议 B：`chat-completions`

方法：

- `buildSceneImageChatRequestBody(...)`

会发送：

- `model`
- `messages`
- `generationConfig.responseModalities = ["TEXT", "IMAGE"]`
- `modalities = ["text", "image"]`
- `temperature`
- `max_tokens`

这里是本次修复中非常关键的部分，因为图片模型如果走 OneAPI/OpenAI 兼容层，很多时候需要同时满足这两类模态声明，网关才会把图透传回来。

### 7. 场景图响应怎么解析

总入口：

- `parseSceneImageResponseResult(responseBody, actualModel, requestUri, protocol)`

如果协议是 `images-generations`：

- 进入 `parseImageGenerationResponse(...)`

如果协议是 `chat-completions`：

- 进入 `parseSceneImageChatResponse(...)`

### 8. `chat/completions` 图片解析规则

`parseSceneImageChatResponse(...)` 会同时尝试提取：

- 图片 payload
- 场景方案 payload

#### 图片 payload 提取规则

会尝试从以下位置找图：

- `message.b64_json`
- `message.image_url`
- `message.images[]`
- `message.content[]`
- `content.parts[]`
- `inlineData.data`
- `inline_data.data`
- `url`
- `image_url.url`

也就是说，只要上游返回的是常见兼容写法，服务端大概率都能识别到。

#### 场景方案 payload 提取规则

如果没拿到图片，会尝试把文本解析成结构化 JSON。

目标字段包括：

- `visual_summary`
- `revised_prompt`
- `negative_prompt`
- `display_text`

如果 Gemini 返回的是 JSON 字符串，服务端会把它解析成 `SceneImagePlanPayload`。

### 9. 什么情况下算真正成功

场景图只有在拿到下面任意一种时才算成功：

- `imageBase64`
- `imageUrl`

否则不算成功。

### 10. `prompt_only` 的处理逻辑

如果第一阶段返回：

- 有场景方案
- 没有图片

那么会标记为：

- `generationMode = prompt_only`

随后服务端自动进入第二阶段真实绘图：

- `executeSecondStageImageGeneration(...)`

#### 第二阶段做什么

它会：

1. 根据第一阶段的 `revisedPrompt + negativePrompt + originalPrompt` 组装真正的绘图 prompt
2. 解析第二阶段可尝试的协议列表
3. 解析第二阶段可尝试的模型列表
4. 再次调用图片接口
5. 如果拿到图，则返回真实图片结果

如果第二阶段依然拿不到图：

- 最终直接抛错
- 不再把“纯 prompt 方案”当成功结果返回给前端

### 11. 为什么之前会出现“后端成功但前端没图”

这不是 GeminiService 本身请求失败，而是字段风格不一致：

- 后端 DTO 使用 `snake_case`
- 前端页面用 `camelCase`

比如后端发出的是：

- `image_url`
- `display_text`
- `revised_prompt`

前端却读取：

- `imageUrl`
- `displayText`
- `revisedPrompt`

现在前端已经通过：

- `normalizeSceneImageResult(payload)`
- `resolveSceneImageSource(sceneImage)`

把这层兼容补上了。

---

## 十、模型选择和回退逻辑

GeminiService 里有几套模型解析逻辑：

### 1. 视觉模型候选

方法：

- `resolveVisionModelsToTry()`

用于：

- 人像分析
- 视觉探针

### 2. 图片模型候选

方法：

- `resolveImageModelsToTry()`

用于：

- 通用图片模型候选拼装

### 3. 场景图第一阶段模型候选

方法：

- `resolveSceneImageModelsToTry()`

用于：

- 易经场景图第一阶段请求

### 4. 场景图第二阶段模型候选

方法：

- `resolveSecondStageImageModelsToTry()`

用于：

- prompt_only 后的二次绘图

### 5. 场景图第二阶段协议候选

方法：

- `resolveSecondStageProtocolsToTry()`

会尝试：

- 当前配置的协议
- `images-generations`
- `chat-completions`

这样做的目的是：

- 第一阶段可能只适合拿 prompt
- 第二阶段可能更适合直接走纯图片接口

---

## 十一、错误处理与状态码映射

### 1. 通用错误映射

方法：

- `mapUpstreamFailureStatus(...)`

规则大致是：

- `401` 或 token 无效 -> `401 Unauthorized`
- `429` -> `429 Too Many Requests`
- `5xx` -> `502 Bad Gateway`
- 其他 -> `400 Bad Request`

### 2. 错误文案构造

主要方法：

- `buildFailureMessage(...)`
- `buildSceneImageFailureMessage(...)`
- `buildSecondStageSceneImageFailureMessage(...)`

这些方法会根据上游 body 内容识别更具体的错误，例如：

- invalid token
- invalid url
- model not found
- no candidates returned
- 限流
- 上游 5xx

这样做的结果是：

- 前端收到的错误更可读
- 用户不用只看到“服务器内部错误”

### 3. 失败详情对象

方法：

- `buildFailureDetails(...)`

返回结构：

- `attemptedModels`
- `lastModel`
- `lastStatus`
- `lastPayloadFormat`
- `uri`

这对排查“到底试了哪些模型、最后挂在哪一步”很有帮助。

---

## 十二、日志链路怎么看

Gemini 链路的日志已经做得比较细，排查时建议按下面顺序看。

### 1. 先看是否发出请求

关键日志样式：

- `Calling Gemini ...`
- `Calling scene image generation ...`
- `Calling second-stage image generation ...`

要确认：

- model
- uri
- protocol
- provider

### 2. 再看 HTTP 结果

如果非 200，通常会看到：

- `Gemini ... failed`
- `Scene image generation failed`
- `Second-stage image generation failed`

这时优先确认：

- 状态码
- 上游 body

### 3. 如果是 200 但结果不可用

重点看：

- `Scene image generation returned 200 but no usable content`
- `Scene image first-stage returned plan instead of image`
- `Second-stage image generation returned no usable image`

这说明：

- 请求成功到达上游了
- 但响应内容没有被识别为可用结果

### 4. 场景图成功日志

关键字段：

- `generationMode`
- `imageSupported`
- `hasImageUrl`
- `hasImageBase64`

判断方法：

- `hasImageUrl=true` 或 `hasImageBase64=true`
  - 后端已成功
- 两者都为 `false`
  - 继续查解析与上游响应

---

## 十三、前端如何消费 Gemini 结果

### 1. Gemini 人像页

页面：

- `src-frontend/pages/GeminiFacePage.jsx`

处理方式：

- 上传图片
- 转 base64
- 调 `geminiApi.analyzeFace()`
- 用 `unwrapApiData(response)` 拿到业务数据
- 直接渲染 `GeminiFaceAnalysisResponse`

### 2. 易经页面

页面：

- `src-frontend/pages/YijingPage.jsx`

处理方式：

- 调 `yijingApi.generateSceneImage()`
- `unwrapApiData(response)`
- `normalizeSceneImageResult(payload)`
- `resolveSceneImageSource(sceneImage)`
- `<img src={sceneImageSrc} />`

这里特别要记住：

- 前端显示是否成功，不只看请求是否 200
- 还要看 `sceneImageSrc` 是否真的被解析出来

---

## 十四、典型故障与定位方法

### 1. 文本探针失败

优先检查：

- `ONE_API_KEY`
- `ONE_API_BASE_URL`
- `gemini.text-model`

### 2. 视觉探针失败

优先检查：

- 图片 MIME 类型
- 图片 base64 是否干净
- 视觉 payload 格式是否兼容
- `vision-model` 是否真的支持多模态

### 3. 人像分析返回乱码或非 JSON

优先检查：

- 模型是否严格遵守 prompt
- OneAPI 是否改写了响应

不过当前已有：

- JSON 提取逻辑
- 文本 fallback 逻辑

所以通常不会直接彻底炸掉。

### 4. 场景图 200 但没图

优先检查：

- `generationMode`
- `hasImageUrl`
- `hasImageBase64`
- `finishReason`

如果看到：

- `finishReason=max_tokens`

优先提高：

- `SCENE_IMAGE_CHAT_MAX_TOKENS`

### 5. 场景图返回 prompt_only

说明：

- 第一阶段只拿到了场景方案

接着看：

- 第二阶段是否有请求日志
- 第二阶段是否成功

### 6. 后端日志显示有图，但页面没图

优先检查：

- 前端是否拿到了 `data.image_url`
- 是否经过 `normalizeSceneImageResult()`
- 是否被错误覆盖成空状态

---

## 十五、当前项目里最重要的几个结论

### 1. Gemini 并不是直接调官方 SDK

当前项目主要是：

- 用 `HttpClient`
- 直接调 OpenAI 兼容接口
- 通过 OneAPI/YMAPI 间接接 Gemini

### 2. 真正复杂的是兼容层

难点不在“发请求”，而在：

- 多模型候选
- 多 payload 兼容
- 多协议兼容
- 图片响应格式不统一
- 网关可能吞图或改写字段

### 3. 场景图是最复杂的一条链路

因为它同时涉及：

- 图片模型
- chat/completions 兼容
- JSON 场景方案 fallback
- 二次绘图
- 图片字段归一化

### 4. 人像分析链路更稳定

它虽然也是视觉调用，但输出目标更清晰：

- 固定是 JSON
- 最终走 DTO 映射
- 不涉及二次图片生成

---

## 十六、建议的维护方式

如果后续继续扩展 Gemini 能力，建议遵循当前模式：

1. 新业务先定义清楚“成功输出是什么”
2. 尽量把上游返回解析成业务 DTO
3. 所有失败都走 `BusinessException`
4. 记录尝试过的模型、协议和 payload 格式
5. 对前端暴露统一、稳定的字段命名

对于图片能力，尤其建议：

- 后端内部允许兼容多种响应格式
- 对前端只输出一个稳定的最终结构

---

## 十七、关键文件索引

后端：

- `src/main/java/com/example/demo/controller/GeminiController.java`
- `src/main/java/com/example/demo/controller/StandaloneYijingController.java`
- `src/main/java/com/example/demo/service/GeminiService.java`
- `src/main/java/com/example/demo/dto/request/gemini/GeminiFaceAnalysisRequest.java`
- `src/main/java/com/example/demo/dto/request/yijing/YijingSceneImageRequest.java`
- `src/main/java/com/example/demo/dto/response/gemini/GeminiFaceAnalysisResponse.java`
- `src/main/java/com/example/demo/dto/response/gemini/GeminiFaceResponseMapper.java`
- `src/main/java/com/example/demo/dto/response/gemini/GeminiProbeResponse.java`
- `src/main/java/com/example/demo/dto/response/yijing/YijingSceneImageResponse.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-example.yml`

前端：

- `src-frontend/api/index.js`
- `src-frontend/pages/GeminiFacePage.jsx`
- `src-frontend/pages/YijingPage.jsx`

补充文档：

- `docs/YIJING_SCENE_IMAGE_FLOW.md`

---

## 十八、总结

当前项目中的 Gemini 调用不是“一条简单 API 请求”，而是一套围绕 OneAPI/Gemini 兼容层建立起来的业务编排系统。

它的核心特点是：

- 统一入口在 `GeminiService`
- 通用能力和场景图能力分离配置
- 通用文本、视觉、人像分析复用一套骨架
- 场景图有自己更复杂的协议与回退机制
- 最终目标始终是把不稳定的上游响应，转换成稳定可消费的业务结果

如果以后要继续接新的 Gemini 能力，最推荐的做法是直接沿着这套链路扩展，而不是绕开 `GeminiService` 另起一套调用方式。
