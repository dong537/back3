# 高优先级风险整改清单

更新时间：2026-04-04

本文聚焦当前 `baziback` 项目最值得优先收口的风险，按影响面与紧急度分为 `P0` 和 `P1` 两级，目标是先解决“会出事故、会拖慢迭代、会影响成交信任”的问题。

## P0

### 1. 生产配置与密钥暴露风险

- 影响文件：
  [application.yml](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/resources/application.yml)
  [application-example.yml](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/resources/application-example.yml)
  [Demo1Application.java](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/java/com/example/demo/Demo1Application.java)
- 风险说明：
  默认 JWT secret、AI provider key、固定外部 base URL、调试级日志和数据库连接打印会把正式环境暴露面拉高。
- 优先动作：
  1. 所有密钥改为环境变量注入，样例文件只保留占位符。
  2. 生产环境关闭数据库 URL 打印和 `DEBUG` 级别日志。
  3. 为 `prod` profile 增加强校验，缺失关键配置时拒绝启动。
- 验收标准：
  仓库中不再出现真实密钥；生产日志不输出敏感配置；`prod` profile 在缺参时启动失败并给出明确提示。

### 2. Gemini 场景图链路复杂度过高

- 影响文件：
  [GeminiService.java](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/java/com/example/demo/service/GeminiService.java)
  [GeminiSceneImageSupport.java](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/java/com/example/demo/service/GeminiSceneImageSupport.java)
- 风险说明：
  场景图模型筛选、协议回退、request body 拼装长期堆在单服务类里，会让后续排障和新增模型支持越来越脆。
- 优先动作：
  1. 继续按职责拆分 `request builder`、`response parser`、`fallback policy`。
  2. 让场景图逻辑拥有独立单测，而不是只挂在 `GeminiService` 集成式测试里。
  3. 为协议切换和模型过滤补充回归样例。
- 验收标准：
  场景图相关逻辑可以独立测试；核心 fallback 路径均有测试覆盖；`GeminiService` 继续缩小。

## P1

### 3. 后端 WebFlux 与 MVC 混搭

- 影响文件：
  [pom.xml](C:/Users/Lenovo/Desktop/n8n/back3/baziback/pom.xml)
  [application.yml](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/resources/application.yml)
- 风险说明：
  `spring-boot-starter-webflux` 与 `spring-boot-starter-web` 同时存在，容易在自动配置、线程模型、异常处理上埋隐患。
- 优先动作：
  1. 明确主栈到底是 `WebFlux` 还是 `Spring MVC`。
  2. 去掉非主栈依赖，并跑通接口回归与部署验证。
- 验收标准：
  依赖栈单一；接口行为、过滤器链和异常处理策略一致。

### 4. 前端超大页面阻碍持续迭代

- 影响文件：
  [Home.jsx](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src-frontend/pages/Home.jsx)
  [TarotPage.jsx](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src-frontend/pages/TarotPage.jsx)
  [YijingPage.jsx](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src-frontend/pages/YijingPage.jsx)
- 风险说明：
  页面同时承载文案、状态、副作用和展示组件，回归修改很容易出现牵一发动全身。
- 优先动作：
  1. 页面级常量、纯展示组件、数据装配逻辑分层。
  2. 优先把首页和塔罗页拆成 `page shell + hooks + presentational components`。
  3. 对首页运势、塔罗抽牌流程补最小化渲染回归测试。
- 验收标准：
  单文件长度持续下降；页面逻辑和 UI 可独立测试；新需求改动范围收敛。

### 5. 社区模块潜在 N+1 查询

- 影响文件：
  [CommunityService.java](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/java/com/example/demo/service/CommunityService.java)
- 风险说明：
  列表和评论装配阶段存在循环补用户、补回复信息的模式，数据量上来后会明显拉高接口延迟。
- 优先动作：
  1. 为帖子列表、评论列表补 SQL 次数统计。
  2. 把用户与回复信息改成批量查询或 join/聚合装配。
- 验收标准：
  单次列表请求的 SQL 次数可预测；分页场景下接口时延稳定。

### 6. 仓库产物与交付目录混杂

- 影响文件：
  [dist/index.html](C:/Users/Lenovo/Desktop/n8n/back3/baziback/dist/index.html)
  [docker-compose.yml](C:/Users/Lenovo/Desktop/n8n/back3/baziback/docker-compose.yml)
  [mcp-src/index.ts](C:/Users/Lenovo/Desktop/n8n/back3/baziback/mcp-src/index.ts)
- 风险说明：
  仓库里混入构建产物、打包归档、临时目录和多套 MCP 实现，协作时很容易造成提交噪音和发布混乱。
- 优先动作：
  1. 清理已跟踪的构建产物，和 `.gitignore` 保持一致。
  2. 明确 `mcp-src` 与 `mcp-server` 的唯一主实现。
  3. 把 `deploy` 输出物迁到仓库外或独立发布目录。
- 验收标准：
  日常提交不再夹带产物文件；发布链路唯一且可复现。

## 本轮已启动的整改

- 场景图逻辑已从 [GeminiService.java](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/java/com/example/demo/service/GeminiService.java) 抽出到 [GeminiSceneImageSupport.java](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src/main/java/com/example/demo/service/GeminiSceneImageSupport.java)。
- 首页已开始拆分为页面配置、运势工具函数和展示组件三层：
  [homeConfig.js](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src-frontend/pages/homeConfig.js)
  [homeFortuneUtils.js](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src-frontend/pages/homeFortuneUtils.js)
  [HomeCards.jsx](C:/Users/Lenovo/Desktop/n8n/back3/baziback/src-frontend/components/home/HomeCards.jsx)
