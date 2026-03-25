# 易经场景图生成逻辑说明

## 文档目的

这份文档用于说明当前“易经场景图”功能的完整实现逻辑、关键字段约定、失败处理策略，以及后续排查时应该优先看的日志点。

适用范围：

- 前端页面发起“生成场景图”
- 后端调用 OneAPI 兼容的 Gemini 图片模型
- 返回图片、回退重试、失败报错
- 前后端字段映射与展示

---

## 整体流程

### 1. 前端发起请求

前端入口位于：

- `src-frontend/pages/YijingPage.jsx`

点击“生成场景图”后，会在 `handleGenerateSceneImage()` 中组装以下信息并请求：

- 用户问题 `question`
- 起卦方式 `method`
- AI 解读或后端推导出的 `interpretation`
- 后端返回的 `interpretation_hint`
- 动爻信息 `changing_lines`
- 本卦快照 `original`
- 变卦快照 `changed`

请求接口：

- `POST /api/standalone/yijing/scene-image`

对应前端 API：

- `src-frontend/api/index.js`
- `yijingApi.generateSceneImage(data)`

---

### 2. 控制器接收请求

后端控制器位于：

- `src/main/java/com/example/demo/controller/StandaloneYijingController.java`

入口方法：

- `generateSceneImage(YijingSceneImageRequest request)`

控制器职责：

- 接收并校验 `YijingSceneImageRequest`
- 调用 `geminiService.generateYijingSceneImage(request)`
- 成功时返回统一结构：

```json
{
  "success": true,
  "message": "场景图生成成功",
  "data": {
    "...": "..."
  }
}
```

- 失败时返回统一错误结构，不再返回“伪成功”的场景方案 JSON

---

### 3. GeminiService 执行图片生成

核心实现位于：

- `src/main/java/com/example/demo/service/GeminiService.java`

主入口：

- `generateYijingSceneImage(YijingSceneImageRequest request)`

当前逻辑分成三步：

#### 3.1 生成场景图提示词

后端先把这些信息整合为用于图片生成的提示词：

- 用户问题
- 场景类别
- 本卦、变卦
- 卦辞、象辞
- 动爻信息
- 解读摘要

目标是让图片模型直接返回适合结果页展示的东方玄学场景图。

#### 3.2 调用 OneAPI 兼容图片模型

当前场景图默认通过独立配置发起请求，使用：

- 独立的 `scene-image` 配置项
- 独立的 URI 构建逻辑
- 独立的 Authorization Header 构建逻辑

当前走的是 OpenAI 兼容的：

- `chat/completions`

请求体中会包含：

- `model`
- `messages`
- `modalities: ["text", "image"]`
- `generationConfig.responseModalities = ["TEXT", "IMAGE"]`
- 较高的 `max_tokens`

这里之所以保留两套“返回图片”声明，是为了兼容 OneAPI/YMAPI 对 Gemini 图片能力的不同转发实现。

#### 3.3 解析响应并决定是否成功

后端会优先解析图片内容，支持以下几类格式：

- `image_url.url`
- `url`
- `b64_json`
- `image_base64`
- `inlineData.data`
- `inline_data.data`
- `content.parts` 中的图片片段

只要拿到以下任意一个，就视为“真成功”：

- `imageBase64`
- `imageUrl`

否则不算成功。

---

## 当前成功判定逻辑

只有满足下面任意条件，接口才会返回成功：

- 拿到 `imageBase64`
- 拿到 `imageUrl`

如果模型只返回文本、场景方案、说明文字，但没有图片内容：

- 后端不会把它当成功结果
- 会继续执行补救逻辑
- 补救后仍无图，则直接抛业务异常

最终用户看到的是明确错误，而不是“看起来成功但没图”的 JSON 结果。

---

## prompt_only 的处理逻辑

有些通道会出现这种情况：

- HTTP 状态码是 `200`
- 模型返回了可继续绘图的 `prompt`
- 但没有返回真正图片

这种情况当前会被标记为：

- `generationMode = prompt_only`

处理方式：

1. 第一阶段先识别出这是“只有方案没有图”
2. 后端自动发起第二阶段真实绘图
3. 如果二次绘图成功，返回图片
4. 如果二次绘图仍无图片，直接报错

也就是说，`prompt_only` 现在只是内部中间状态，不会作为成功结果直接返回给前端展示。

---

## 为什么之前会“后端成功但前端不显示”

这是本次修复里的一个关键问题。

后端响应 DTO 使用的是 `snake_case` 字段名，例如：

- `image_url`
- `image_base64`
- `revised_prompt`
- `display_text`
- `image_supported`

而前端页面一开始读取的是 `camelCase`：

- `imageUrl`
- `imageBase64`
- `revisedPrompt`
- `displayText`
- `imageSupported`

结果就是：

- 后端日志显示 `hasImageUrl=true`
- 前端却取不到 `sceneImageResult.imageUrl`
- 页面误判为“没有图片”

现在前端已增加统一归一化逻辑，兼容两种字段风格。

关键位置：

- `src-frontend/pages/YijingPage.jsx`
- `resolveSceneImageSource(sceneImage)`
- `normalizeSceneImageResult(payload)`

---

## 前端当前展示逻辑

前端页面位于：

- `src-frontend/pages/YijingPage.jsx`

场景图返回后，前端会先做字段归一化，再进入展示。

### 图片地址解析规则

当前优先级如下：

1. `imageUrl`
2. `image_url`
3. `imageBase64`
4. `image_base64`

如果 `imageBase64` 已经是 `data:` 开头的数据地址，则直接使用。

如果只是纯 base64，则自动补成：

```text
data:image/png;base64,...
```

### 展示规则

如果 `sceneImageSrc` 有值：

- 页面直接渲染 `<img src={sceneImageSrc} />`

如果没有值：

- 页面展示兜底说明文案

但在当前修复逻辑下，真正“没有图”的情况一般不会再走到成功页，而会被后端直接当错误返回。

---

## 当前日志设计

为了便于定位问题，后端增加了更细的日志。

### 1. 请求发起日志

会打印：

- provider
- protocol
- model
- uri

示例关注点：

```text
Calling scene image generation | provider=..., protocol=..., model=..., uri=...
```

### 2. 解析结果日志

会打印：

- `generationMode`
- 是否支持图片
- 是否拿到 `imageUrl`
- 是否拿到 `imageBase64`
- `visualSummaryLength`
- `displayTextLength`

示例关注点：

```text
Scene image result ready | provider=..., model=..., generationMode=direct_image, imageSupported=true, hasImageUrl=true, hasImageBase64=false
```

### 3. 无图失败日志

如果最后没图，会打印：

- provider
- model
- sceneCategory
- generationMode
- requestUri
- revisedPromptLength
- visualSummaryLength
- displayText

### 4. 上游原始响应摘要

对于关键失败分支，会额外记录：

- `finishReason`
- 文本长度
- 是否识别到图片 payload
- 是否识别到场景方案 payload
- `rawBody`

这能帮助判断到底是：

- token 不足
- 网关吞图
- 模型仅返回文本
- 模型不支持当前方法

---

## 已确认过的典型问题

### 1. `max_tokens` 太低导致 200 但无图

已验证现象：

- `max_tokens` 过低时，上游可能返回 `200`
- 但内容为空或被截断
- `finish_reason` 可能是 `MAX_TOKENS`

当前处理：

- 场景图通道提高默认 token 预算
- 对 `max_tokens` 不足增加更明确的报错提示

### 2. 某些候选模型在当前网关下不存在

已出现过：

- 某模型直接 `404`

当前处理：

- 场景图候选模型收紧
- 默认优先使用已验证可返回图片的模型

### 3. 上游能生成图，但前端没显示

已确认根因：

- 后端返回 `snake_case`
- 前端只读取 `camelCase`

当前处理：

- 前端统一做字段归一化

---

## 排查顺序建议

以后如果“场景图又出问题”，建议按下面顺序查：

### 第一步：先看后端结果摘要日志

重点看这几个字段：

- `generationMode`
- `imageSupported`
- `hasImageUrl`
- `hasImageBase64`

判断方法：

- `hasImageUrl=true` 或 `hasImageBase64=true`
  - 后端已经成功，优先查前端展示
- 两者都为 `false`
  - 继续查 GeminiService 失败日志

### 第二步：看是否是 `prompt_only`

如果出现：

- `generationMode=prompt_only`

说明第一轮只拿到了方案，没有拿到图片。此时要确认：

- 二次绘图是否发起
- 二次绘图是否成功
- 最终有没有被正确抛错

### 第三步：看是否是 token 问题

如果日志中出现：

- `finishReason=MAX_TOKENS`

优先检查：

- `scene-image.chat-max-tokens`

### 第四步：看是否是模型或网关问题

如果出现：

- `404`
- `model not found`
- 返回文本但始终不回图片

优先检查：

- 当前 OneAPI/YMAPI 是否支持该模型
- 网关是否正确转发 Gemini 图片返回结构
- 是否真的把 `IMAGE` modality 透传下去了

### 第五步：看前端字段映射

如果后端已经确认成功，但页面无图，优先确认：

- 前端是不是拿到了 `data.image_url`
- 是否经过 `normalizeSceneImageResult()`
- `resolveSceneImageSource()` 最终返回值是否为空

---

## 关键文件索引

后端：

- `src/main/java/com/example/demo/controller/StandaloneYijingController.java`
- `src/main/java/com/example/demo/service/GeminiService.java`
- `src/main/java/com/example/demo/dto/request/yijing/YijingSceneImageRequest.java`
- `src/main/java/com/example/demo/dto/response/yijing/YijingSceneImageResponse.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-example.yml`

前端：

- `src-frontend/pages/YijingPage.jsx`
- `src-frontend/api/index.js`

---

## 当前结论

截至本次修复，易经场景图功能的逻辑是：

- 前端负责收集卦象上下文并发起请求
- 后端负责组织提示词、调用图片模型、解析 OneAPI/Gemini 响应
- 只有真正拿到图片才返回成功
- 没有图片则直接报错，不再返回伪成功 JSON
- 前端已兼容 `snake_case` 和 `camelCase` 响应字段

如果未来再次出现“有日志但没图”，请优先对照本文件中的“排查顺序建议”逐项检查。
