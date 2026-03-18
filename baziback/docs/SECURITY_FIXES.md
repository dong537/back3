# 安全修复指南

## 🔴 立即修复项

### 1. 敏感信息配置修复

#### 步骤 1: 创建环境变量模板

创建 `.env.example` 文件：

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/bazi?useSSL=false&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your_password_here

# JWT 配置
JWT_SECRET=your-secret-key-at-least-256-bits-long-for-hs256-algorithm-security
JWT_EXPIRATION=86400000

# DeepSeek API
DEEPSEEK_API_KEY=your-deepseek-api-key

# MCP API Keys
MCP_BAZI_KEY=your-mcp-bazi-key
MCP_ZIWEI_KEY=your-mcp-ziwei-key
MCP_STAR_KEY=your-mcp-star-key

# 服务器配置
SERVER_PORT=8088
```

#### 步骤 2: 更新 application.yml

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}

deepseek:
  api:
    key: ${DEEPSEEK_API_KEY}
```

#### 步骤 3: 更新 .gitignore

```gitignore
# 环境变量文件
.env
.env.local
.env.production
application-local.yml
application-prod.yml
```

---

### 2. CORS 配置修复

**文件**: `src/main/java/com/example/demo/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 从配置读取允许的源
        Arrays.stream(allowedOrigins.split(","))
            .forEach(config::addAllowedOriginPattern);
        
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
```

---

### 3. 密码加密器修复

**文件**: `src/main/java/com/example/demo/service/UserService.java`

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;  // 注入而不是创建
    
    // 移除：private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
}
```

---

### 4. 前端日志修复

**文件**: `src-frontend/utils/logger.js` (新建)

```javascript
const isDev = import.meta.env.DEV;

export const logger = {
  log: (...args) => {
    if (isDev) {
      console.log(...args);
    }
  },
  error: (...args) => {
    console.error(...args);
    // 生产环境可以发送到错误监控服务
  },
  warn: (...args) => {
    if (isDev) {
      console.warn(...args);
    }
  },
  info: (...args) => {
    if (isDev) {
      console.info(...args);
    }
  }
};
```

然后替换所有 `console.log` 为 `logger.log`。

---

### 5. 错误信息修复

**文件**: `src/main/java/com/example/demo/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Value("${app.debug:false}")
    private boolean debugMode;
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("服务器错误", ex);
        
        Map<String, String> error = new HashMap<>();
        error.put("code", "SERVER_ERROR");
        
        if (debugMode) {
            error.put("message", "服务器内部错误：" + ex.getMessage());
        } else {
            error.put("message", "服务器内部错误，请稍后重试");
        }
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

---

## 🟡 高优先级修复

### 6. 添加输入验证

**文件**: `src/main/java/com/example/demo/dto/request/user/RegisterRequest.java`

```java
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

---

### 7. 添加速率限制

**文件**: `src/main/java/com/example/demo/config/RateLimitConfig.java`

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter loginRateLimiter() {
        return RateLimiter.create(5.0); // 每秒5次
    }
}
```

**文件**: `src/main/java/com/example/demo/controller/UserController.java`

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final RateLimiter loginRateLimiter;
    
    @PostMapping("/login")
    public Map<String, Object> login(@Validated @RequestBody LoginRequest request) {
        if (!loginRateLimiter.tryAcquire()) {
            return Map.of("success", false, "message", "请求过于频繁，请稍后重试");
        }
        // ... 原有逻辑
    }
}
```

---

## 📋 修复检查清单

- [ ] 创建 `.env.example` 文件
- [ ] 更新 `application.yml` 移除敏感信息
- [ ] 更新 `.gitignore` 添加环境变量文件
- [ ] 修复 CORS 配置
- [ ] 修复密码加密器注入
- [ ] 创建日志工具类
- [ ] 替换所有 `console.log` 为 `logger.log`
- [ ] 修复错误信息泄露
- [ ] 添加输入验证注解
- [ ] 添加速率限制
- [ ] 更新日志级别配置
- [ ] 测试所有修复

---

**最后更新**: 2025-01-08
