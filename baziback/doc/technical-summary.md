# 技术总结：AgentPit SSO 单点登录 & Token 消耗上报

日期：2026-04-05

## 一、OAuth2 授权登录

### 凭证信息

| 参数 | 值 |
|------|-----|
| Client ID | `cmnkiszzv003b60t9kfs52kn9` |
| Client Secret | `cmnkiszzv003c60t9oalntnw2` |
| Callback URL | `https://destiny.agentpit.io/api/auth/agentpit/callback` |
| Authorization URL | `https://www.agentpit.io/api/oauth/authorize` |
| Token URL | `https://www.agentpit.io/api/oauth/token` |
| UserInfo URL | `https://www.agentpit.io/api/oauth/userinfo` |
| Scope | `profile` |

### OAuth2 流程

```
用户 → 前端 → /api/auth/agentpit（弹窗模式）或 /api/auth/agentpit/sso（SSO 模式）
     → 302 重定向到 AgentPit 授权页
     → 用户授权
     → AgentPit 回调 /api/auth/agentpit/callback?code=xxx&state=xxx
     → 后端用 code 换 access_token
     → 用 access_token 获取用户信息
     → 查找或创建本地用户
     → 生成 JWT
     → 返回给前端
```

### 两种模式

1. **弹窗模式** (`GET /api/auth/agentpit`)
   - 打开弹窗窗口进行授权
   - 通过 `postMessage` 或 `localStorage` 将结果传回主窗口
   - 用户手动点击"agentpit 授权登陆"按钮触发

2. **SSO 自动模式** (`GET /api/auth/agentpit/sso?returnUrl=/`)
   - 全页面重定向，使用 `prompt=none` 静默授权
   - 通过 `state` 参数（`sso:` 前缀）区分模式
   - 成功后通过 URL hash 传递 token，重定向到前端 `/auth/sso/callback`

## 二、SSO 自动单点登录

### 核心文件

| 文件 | 功能 |
|------|------|
| `AgentpitOAuthController.java` | 后端 OAuth 端点（弹窗 + SSO 两种模式） |
| `AgentpitOAuthService.java` | OAuth 业务逻辑（code 换 token、获取用户信息、JWT 生成） |
| `SsoCallbackPage.jsx` | 前端 SSO 回调页，从 URL hash 提取 token 完成登录 |
| `ssoHelper.js` | 防无限循环工具（`shouldAutoSso`、`markSsoAttempted`、`clearSsoAttempted`） |
| `AuthContext.jsx` | 认证上下文，集成自动 SSO 触发逻辑 |

### 防循环机制

使用 `sessionStorage` 的 `sso_attempted` 标记：

1. **首次访问**：无本地 token → `shouldAutoSso()` 返回 `true` → 标记 `sso_attempted=1` → 重定向到 SSO
2. **SSO 成功**：回调页提取 token → `clearSsoAttempted()` 清除标记 → 正常使用
3. **SSO 失败**：回到登录页 → `sso_attempted=1` 仍在 → 不再自动重试
4. **跳过条件**：SSO 回调页、登录页、URL 含 `sso_error` 参数时不触发

### SSO 时序图

```
AuthContext 初始化
  │
  ├─ 有本地 token → 验证 token → 成功 → 正常使用
  │                            → 失败 → logout
  │
  └─ 无本地 token
       │
       ├─ shouldAutoSso() = false → 显示登录页
       │
       └─ shouldAutoSso() = true
            → markSsoAttempted()
            → 重定向到 /api/auth/agentpit/sso
            → AgentPit 静默授权（prompt=none）
            → 回调 /api/auth/agentpit/callback?code=xxx&state=sso:/
            → 后端处理 → JS 重定向到 /auth/sso/callback#token=xxx&user=xxx
            → SsoCallbackPage 提取 token → auth.login() → 跳转 returnUrl
```

## 三、Token 消耗上报

### API 接口

#### 1. 上报 Token 消耗
```
POST /api/v1/tokens/report
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "agentId": "gemini-face",        // 必填
  "tokensUsed": 1500,              // 必填，>0
  "startedAt": "2026-04-05T10:30:00", // 必填，ISO 8601
  "endedAt": "2026-04-05T10:30:05",   // 必填，ISO 8601，必须晚于 startedAt
  "applicationId": "baziback",     // 可选
  "inputTokens": 500,             // 可选
  "outputTokens": 1000,           // 可选
  "modelName": "gemini-2.0-flash", // 可选
  "requestId": "req-123",         // 可选
  "metadata": {"key": "value"}    // 可选，JSON 对象
}
```

#### 2. 查询消耗汇总
```
GET /api/v1/tokens/summary
Authorization: Bearer <JWT>

Response: { "totalTokens": 15000, "totalRecords": 10 }
```

#### 3. 查询消耗记录
```
GET /api/v1/tokens/records?page=1&size=20
Authorization: Bearer <JWT>

Response: { "records": [...], "page": 1, "size": 20 }
```

### 核心文件

| 文件 | 功能 |
|------|------|
| `TokenUsageController.java` | REST API 端点 |
| `TokenUsageService.java` | 业务逻辑（校验、入库） |
| `TokenUsageMapper.java` | MyBatis 数据访问层 |
| `TokenUsage.java` | 实体类 |
| `TokenReportRequest.java` | 请求 DTO |
| `TokenTracker.java` | 自动从 API 响应提取 token 用量的组件 |
| `add_token_usage_table.sql` | 建表 SQL |

### 自动 Token 追踪

`TokenTracker` 组件已集成到以下服务中，每次 AI API 调用后自动记录 token 消耗：

- `GeminiService` — 人脸分析、场景图片生成、探测
- `DeepSeekService` — AI 解读
- `BaZiDeepSeekService` — 八字 AI 分析
- `ReasoningService` — 推理服务

### 数据库表结构

```sql
CREATE TABLE tb_token_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_id VARCHAR(255) NOT NULL,
    application_id VARCHAR(255),
    user_id BIGINT NOT NULL,
    tokens_used INT NOT NULL,
    input_tokens INT,
    output_tokens INT,
    started_at DATETIME NOT NULL,
    ended_at DATETIME NOT NULL,
    model_name VARCHAR(100),
    request_id VARCHAR(255),
    metadata JSON,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_agent_id (agent_id),
    INDEX idx_user_id (user_id),
    INDEX idx_application_id (application_id),
    INDEX idx_create_time (create_time)
);
```

## 四、关键配置位置

### 后端配置

- `application.yml` — Spring Boot 配置（OAuth、DB、API keys 等），环境变量优先
- `SecurityConfig.java` — 安全配置（公开/保护路径）
  - 公开：`/api/auth/agentpit/**`
  - 保护：`/api/v1/tokens/**`（需 JWT）

### Docker 配置

- `/opt/baziback-docker/app/.env` — 环境变量（**生产凭证在此处，优先级高于 application.yml**）
- `/opt/baziback-docker/app/docker-compose.yml` — 容器编排
  - backend `environment` 部分必须声明所有需要传入的环境变量

### 环境变量传递链

```
.env 文件 → docker-compose.yml environment 声明 → 容器环境变量 → Spring Boot ${} 占位符
```

**注意**：`.env` 中定义的变量，必须在 `docker-compose.yml` 的 `environment` 中声明才会传入容器。
