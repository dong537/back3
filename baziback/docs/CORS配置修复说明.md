# CORS配置修复说明

## 🔍 问题描述

登录注册功能出现 403 Forbidden 错误，日志显示：
```
DEBUG o.s.w.c.r.DefaultCorsProcessor - Reject: 'http://localhost:3001' origin is not allowed 
DEBUG o.s.w.s.a.HttpWebHandlerAdapter - [7e9f7486-8] Completed 403 FORBIDDEN
```

**原因**：后端CORS配置未包含前端运行地址 `http://localhost:3001`

---

## ✅ 修复内容

### 1. 更新CorsConfig.java默认值

**文件**：`src/main/java/com/example/demo/config/CorsConfig.java`

**修改前**：
```java
@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://lldd.click}")
```

**修改后**：
```java
@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001,http://localhost:5173,https://lldd.click}")
```

### 2. 添加application.yml配置

**文件**：`src/main/resources/application.yml`

**新增配置**：
```yaml
# 应用配置
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001,http://localhost:5173,https://lldd.click}
```

---

## 🔄 修复后的效果

现在后端允许以下来源的跨域请求：
- ✅ `http://localhost:3000` - Vite默认端口
- ✅ `http://localhost:3001` - 当前前端运行端口
- ✅ `http://localhost:5173` - Vite HMR端口
- ✅ `https://lldd.click` - 生产环境域名

---

## 🚀 应用修复

### 方法1：重启后端服务（推荐）

如果后端服务正在运行，需要重启以应用新的配置：

```bash
# 停止后端服务（Ctrl+C）
# 重新启动
cd back3/baziback
mvn spring-boot:run
# 或
java -jar target/demo-1.0.0.jar
```

### 方法2：使用环境变量（可选）

如果需要临时添加其他来源，可以通过环境变量配置：

```bash
# Windows PowerShell
$env:CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:3001,http://localhost:5173,https://lldd.click"

# Linux/Mac
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:3001,http://localhost:5173,https://lldd.click"
```

---

## 📝 配置说明

### CORS配置项说明

- **allowed-origins**：允许的跨域来源，多个用逗号分隔
- **allowed-methods**：允许的HTTP方法（已配置为 `*`，允许所有方法）
- **allowed-headers**：允许的请求头（已配置为 `*`，允许所有请求头）
- **allow-credentials**：是否允许携带凭证（已配置为 `true`）
- **max-age**：预检请求缓存时间（已配置为 3600 秒）

### 通配符支持

配置中使用了 `addAllowedOriginPattern`，支持通配符模式：
- `http://localhost:*` - 允许所有本地端口
- `https://*.example.com` - 允许所有子域名

---

## ⚠️ 注意事项

1. **生产环境安全**：
   - 生产环境应该明确指定允许的域名
   - 不要使用通配符 `*` 允许所有来源
   - 建议通过环境变量配置，不要硬编码

2. **开发环境**：
   - 开发环境可以使用 `http://localhost:*` 通配符
   - 方便切换不同的前端端口

3. **配置优先级**：
   - 环境变量 `CORS_ALLOWED_ORIGINS` > `application.yml` > 代码默认值

---

## 🔍 验证修复

修复后，可以通过以下方式验证：

1. **查看日志**：
   - 登录时不应该再出现 `Reject: 'http://localhost:3001' origin is not allowed`
   - 应该看到 `Allowed origin: http://localhost:3001`

2. **测试登录**：
   - 前端应该能够正常发送登录请求
   - 不再出现 403 Forbidden 错误

3. **检查响应头**：
   - 在浏览器开发者工具的Network标签页
   - 查看响应头中是否有 `Access-Control-Allow-Origin: http://localhost:3001`

---

## 📚 相关文件

- `src/main/java/com/example/demo/config/CorsConfig.java` - CORS配置类
- `src/main/resources/application.yml` - 应用配置文件
- `src/main/resources/application-example.yml` - 配置示例文件

---

**修复时间**：2026-01-17  
**修复状态**：✅ 已完成
