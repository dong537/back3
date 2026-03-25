# 后端i18n国际化优化指南

## 📋 已完成的工作

### 1. 翻译文件配置 ✅
- ✅ 创建 `src/main/resources/messages.properties` (英文)
- ✅ 创建 `src/main/resources/messages_zh_CN.properties` (中文)
- ✅ 创建 `src/main/resources/messages_en_US.properties` (英文)

### 2. Spring配置 ✅
- ✅ `I18nConfig.java` - MessageSource和LocaleResolver配置
- ✅ `I18nUtil.java` - i18n工具类

### 3. pom.xml ✅
- ✅ 添加 spring-boot-starter-web 依赖

## 🔧 待优化的控制器和服务

### 需要修改的文件列表

#### 1. BaZiController.java
硬编码错误消息位置:
- 第50行: `"八字不能为空"`
- 第56行: `"分析失败: " + e.getMessage()`
- 第68行: `"八字不能为空"`
- 第73行: `"分析失败: " + e.getMessage()`
- 第84行: `"分析失败: " + e.getMessage()`
- 第95行: `"八字和出生年份不能为空"`

**修改示例:**
```java
@PostMapping("/analyze")
public ResponseEntity<?> analyze(@RequestBody Map<String, Object> request) {
    try {
        String baZi = asString(request.get("baZi"));
        if (baZi == null || baZi.isBlank()) {
            return badRequest(i18nUtil.getErrorMessage("bazi.invalidInput"));
        }
        // ... rest of code
    } catch (Exception e) {
        return badRequest(i18nUtil.getErrorMessage("bazi.failed", e.getMessage()));
    }
}
```

#### 2. 其他需要修改的服务

**YijingService/Controller**
- 检查失败消息
- 输入验证消息

**TarotService/Controller**
- 牌阵处理消息
- 错误消息

**ZodiacService/Controller**
- 星座分析消息
- 日期验证消息

**用户认证相关**
- 登录失败消息
- 令牌验证消息

**MCP客户端消息** (McpStarClient, McpZiweiClient等)
- API连接错误
- 工具列表获取失败
- 执行失败消息

## ✨ 使用方式

### 在Spring Bean中使用:
```java
@Service
public class BaZiService {
    
    @Autowired
    private I18nUtil i18nUtil;
    
    public void analyze(String baZi) {
        if (baZi == null) {
            throw new IllegalArgumentException(i18nUtil.getErrorMessage("bazi.invalidInput"));
        }
        // ...
    }
}
```

### 在控制器中使用:
```java
@RestController
public class BaZiController {
    
    @Autowired
    private I18nUtil i18nUtil;
    
    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody Map<String, Object> request) {
        try {
            // ... code
        } catch (Exception e) {
            String message = i18nUtil.getErrorMessage("bazi.failed", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
    }
}
```

### 获取语言切换:
客户端可以通过以下方式切换语言:
- URL参数: `?lang=en_US` 或 `?lang=zh_CN`
- HTTP Header: `Accept-Language: en-US` 或 `Accept-Language: zh-CN`

## 🎯 优化检查清单

### 需要修改的消息键

| 位置 | 原始消息 | i18n键 |
|------|---------|--------|
| 验证 | 八字不能为空 | error.bazi.invalidInput |
| 分析 | 分析失败: {msg} | error.bazi.failed |
| 日期 | 无效的出生日期 | error.bazi.invalidDate |
| 变换 | 日期转换失败: {msg} | error.bazi.conversionFailed |
| 报告 | 报告生成失败: {msg} | error.bazi.reportFailed |

### 总体修改计划

1. **第一步**: 修改主要控制器 (BaZiController, YijingController等)
2. **第二步**: 修改业务服务 (BaZiService, 星座服务等)
3. **第三步**: 修改API客户端 (McpStarClient, McpZiweiClient等)
4. **第四步**: 修改工具类中的硬编码消息
5. **第五步**: 测试多语言切换功能

## 📝 测试语言切换

### cURL测试:
```bash
# 中文
curl -H "Accept-Language: zh-CN" http://localhost:8080/api/bazi/analyze

# 英文
curl -H "Accept-Language: en-US" http://localhost:8080/api/bazi/analyze

# 使用参数切换
curl http://localhost:8080/api/bazi/analyze?lang=en_US
```

## ⚠️ 注意事项

1. **编码**: 确保所有properties文件都保存为UTF-8编码
2. **性能**: i18n消息已缓存3600秒，无需担心性能问题
3. **默认语言**: 默认语言设置为中文(zh_CN)
4. **参数化**: 所有带参数的消息使用{0}, {1}格式
5. **向后兼容**: 如果消息获取失败，系统会返回消息键本身，不会崩溃

## 🔗 参考资源

- [Spring i18n 官方文档](https://spring.io/guides/gs/centralized-configuration/)
- [ResourceBundleMessageSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/support/ResourceBundleMessageSource.html)
- [LocaleContextHolder](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/i18n/LocaleContextHolder.html)
