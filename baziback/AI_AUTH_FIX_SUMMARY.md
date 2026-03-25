# AI 对话认证错误修复总结

## 问题描述

用户在请求 `/api/deepseek/reasoning-stream` 时收到 **401 UNAUTHORIZED** 错误。

## 根本原因

### 1. Token 存储不一致
- **前端**：AuthContext 使用 `sessionStorage` 存储 token
- **前端代码**：多个页面使用 `localStorage.getItem('token')` 获取 token
- **结果**：token 无法被正确传递到后端

### 2. 后端认证配置不完整
- `ReasoningController` 没有验证 Authorization header
- `SecurityConfig` 中 `/api/deepseek/reasoning-stream` 没有明确标记为需要认证

## 修复内容

### 前端修复

#### 1. AIPage.jsx
```javascript
// 修改前
const token = localStorage.getItem('token')

// 修改后
const token = sessionStorage.getItem('token')
```

#### 2. TarotPage.jsx
- `checkTodayDraw()` 方法
- `handleDailyDraw()` 方法

#### 3. Home.jsx
- `loadFortuneDetail()` 方法

#### 4. ThinkingChain.jsx
- 流式请求中的 token 获取

### 后端修复

#### 1. SecurityConfig.java
```java
// 添加认证要求
.pathMatchers("/api/deepseek/reasoning-stream").authenticated()
// 其他 DeepSeek 接口允许公开访问
.pathMatchers("/api/deepseek/**").permitAll()
```

#### 2. ReasoningController.java
```java
// 添加认证检查
@PostMapping(value = "/reasoning-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamReasoning(
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody Map<String, Object> request) {
    
    // 验证认证信息
    Long userId = authUtil.requireUserId(token);
    log.info("用户 {} 请求推理流式响应", userId);
    // ...
}
```

## 修改的文件

### 前端
1. `src-frontend/pages/AIPage.jsx`
2. `src-frontend/pages/TarotPage.jsx`
3. `src-frontend/pages/Home.jsx`
4. `src-frontend/components/ThinkingChain.jsx`

### 后端
1. `src/main/java/com/example/demo/config/SecurityConfig.java`
2. `src/main/java/com/example/demo/controller/ReasoningController.java`

## 测试步骤

1. **重新编译后端**
   ```bash
   mvn clean package
   ```

2. **重启后端服务**

3. **清除浏览器缓存**
   - 清除 localStorage 和 sessionStorage
   - 刷新页面

4. **测试 AI 对话**
   - 登录账号
   - 进入 AI 对话页面
   - 发送消息
   - 验证是否成功接收流式响应

## 关键点

- ✅ 前端统一使用 `sessionStorage` 存储 token
- ✅ 后端明确标记需要认证的端点
- ✅ 认证检查在控制器层面进行
- ✅ 错误日志记录用户 ID 便于调试

## 注意事项

- 确保 `AuthUtil.requireUserId()` 方法能正确处理 token 验证
- 如果仍然收到 401，检查 JWT token 的有效期配置
- 检查浏览器开发者工具中的 Network 标签，确认 Authorization header 被正确发送
