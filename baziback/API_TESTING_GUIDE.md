# 参天AI - API接口测试指南

**版本：** 1.0.0  
**测试日期：** 2024-11-24  
**基础URL：** `http://localhost:8088`

---

## 目录

1. [测试环境准备](#测试环境准备)
2. [快速测试流程](#快速测试流程)
3. [完整接口清单](#完整接口清单)
4. [测试脚本](#测试脚本)

---

## 测试环境准备

### 1. 启动项目
确保项目已启动并运行在 `http://localhost:8088`

### 2. 准备测试工具
- **Postman** 或 **cURL** 命令行工具
- **浏览器**（用于测试GET接口）

### 3. 获取测试Token
```bash
# 步骤1: 发送验证码
curl -X POST http://localhost:8088/api/auth/sms/send \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}'

# 步骤2: 登录获取Token（验证码从Redis或日志中获取）
curl -X POST http://localhost:8088/api/auth/phone/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"123456"}'

# 保存返回的accessToken用于后续测试
export TOKEN="YOUR_ACCESS_TOKEN_HERE"
```

---

## 快速测试流程

### 测试流程1：用户注册登录
```bash
# 1. 发送验证码
curl -X POST http://localhost:8088/api/auth/sms/send \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}'

# 2. 登录
curl -X POST http://localhost:8088/api/auth/phone/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"123456"}'

# 3. 获取用户信息
curl -X GET http://localhost:8088/api/user/info \
  -H "Authorization: Bearer $TOKEN"
```

### 测试流程2：八字分析完整流程
```bash
# 1. 创建八字信息
curl -X POST http://localhost:8088/api/bazi/info \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"测试用户",
    "gender":1,
    "birthYear":1990,
    "birthMonth":5,
    "birthDay":15,
    "birthHour":10,
    "birthMinute":30,
    "isLunar":0,
    "timezone":"Asia/Shanghai",
    "birthplace":"北京",
    "isDefault":1
  }'

# 2. 进行八字分析
curl -X POST http://localhost:8088/api/bazi/formatted \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gender":"male","solarDatetime":"1990-05-15 10:30"}'

# 3. 查看分析历史
curl -X GET "http://localhost:8088/api/analysis/history?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"

# 4. 获取分析统计
curl -X GET http://localhost:8088/api/analysis/statistics \
  -H "Authorization: Bearer $TOKEN"
```

### 测试流程3：趋势分析（NEW）
```bash
# 获取完整趋势分析
curl -X POST http://localhost:8088/api/trend/analysis \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bazi":"庚午 辛巳 甲寅 己巳",
    "gender":"male",
    "birthDate":"1990-05-15",
    "startAge":0,
    "endAge":80
  }'
```

### 测试流程4：多语言支持（NEW）
```bash
# 1. 获取支持的语言列表
curl -X GET http://localhost:8088/api/i18n/languages

# 2. 翻译分析结果
curl -X POST http://localhost:8088/api/i18n/translate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content":"您的八字显示事业运势较好，适合在技术领域发展。",
    "targetLanguage":"en"
  }'

# 3. 生成多语言报告
curl -X POST http://localhost:8088/api/i18n/report \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "baziData":"庚午 辛巳 甲寅 己巳",
    "reportType":"comprehensive",
    "language":"ja"
  }'
```

---

## 完整接口清单

### 认证与授权（4个接口）
- ✅ `POST /api/auth/sms/send` - 发送短信验证码
- ✅ `POST /api/auth/phone/login` - 手机号登录/注册
- ✅ `POST /api/auth/token/refresh` - 刷新Token
- ✅ `GET /api/auth/sms/ttl/{phone}` - 获取验证码剩余时间

### 用户管理（3个接口）
- ✅ `POST /api/user/register` - 用户注册
- ✅ `POST /api/user/login` - 用户登录
- ✅ `GET /api/user/info` - 获取用户信息

### 八字分析（2个接口）
- ✅ `GET /api/bazi/tools` - 获取可用工具列表
- ✅ `POST /api/bazi/formatted` - 获取八字详情

### 趋势分析 🆕（1个接口）
- ✅ `POST /api/trend/analysis` - 获取完整趋势分析

### 多语言支持 🆕（3个接口）
- ✅ `GET /api/i18n/languages` - 获取支持的语言列表
- ✅ `POST /api/i18n/translate` - 翻译分析结果
- ✅ `POST /api/i18n/report` - 生成多语言报告

### 塔罗占卜（8个接口）
- ✅ `POST /api/tarot/card/info` - 获取单张牌信息
- ✅ `POST /api/tarot/card/list` - 列出所有塔罗牌
- ✅ `POST /api/tarot/reading/perform` - 执行塔罗解读
- ✅ `POST /api/tarot/card/search` - 搜索塔罗牌
- ✅ `POST /api/tarot/card/similar` - 查找相似牌
- ✅ `POST /api/tarot/analytics` - 获取数据库分析
- ✅ `POST /api/tarot/spread/custom` - 创建自定义牌阵
- ✅ `POST /api/tarot/card/random` - 获取随机牌

### 易经八字（9个接口）
- ✅ `GET /api/yijing/tools` - 获取可用工具列表
- ✅ `POST /api/yijing/hexagram/generate` - 生成六爻卦象
- ✅ `POST /api/yijing/bazi/chart/generate` - 生成八字命盘
- ✅ `POST /api/yijing/bazi/analyze` - 分析八字命盘
- ✅ `POST /api/yijing/bazi/forecast` - 预测未来运势
- ✅ `POST /api/yijing/combined-analysis` - 综合分析
- ✅ `POST /api/yijing/destiny-consult` - 命理咨询
- ✅ `POST /api/yijing/knowledge/learn` - 知识学习
- ✅ `POST /api/yijing/case-study` - 案例分析

### 紫微斗数（7个接口）
- ✅ `GET /api/ziwei/tools` - 获取可用工具列表
- ✅ `POST /api/ziwei/chart/generate` - 生成命盘
- ✅ `POST /api/ziwei/chart/interpret` - 命盘解读
- ✅ `POST /api/ziwei/fortune/analyze` - 运势分析
- ✅ `POST /api/ziwei/compatibility/analyze` - 合婚分析
- ✅ `POST /api/ziwei/auspicious-date/select` - 择日功能
- ✅ `POST /api/ziwei/visualization/generate` - 生成可视化图表

### 星座运势（5个接口）
- ✅ `POST /api/zodiac/info` - 获取星座基本信息
- ✅ `POST /api/zodiac/daily-horoscope` - 获取每日运势
- ✅ `POST /api/zodiac/compatibility` - 星座配对分析
- ✅ `POST /api/zodiac/by-date` - 根据日期查询星座
- ✅ `POST /api/zodiac/all` - 获取所有星座信息

### DeepSeek AI（3个接口）
- ✅ `POST /api/deepseek/generate-report` - 生成八字报告
- ✅ `POST /api/deepseek/interpret-hexagram` - 解读卦象
- ✅ `POST /api/deepseek/chart/deepseek-interpret` - 命盘解读

### 分析管理（8个接口）
- ✅ `GET /api/analysis/history` - 获取分析历史列表
- ✅ `GET /api/analysis/history/{id}` - 获取分析历史详情
- ✅ `POST /api/analysis/history/{id}/favorite` - 收藏/取消收藏
- ✅ `GET /api/analysis/history/favorites` - 获取收藏列表
- ✅ `GET /api/analysis/statistics` - 获取分析统计
- ✅ `GET /api/analysis/reports` - 获取报告列表
- ✅ `GET /api/analysis/report/{id}` - 获取报告详情
- ✅ `POST /api/analysis/report/{id}/export` - 导出报告

### 用户八字信息（6个接口）
- ✅ `POST /api/bazi/info` - 创建八字信息
- ✅ `PUT /api/bazi/info` - 更新八字信息
- ✅ `GET /api/bazi/info/list` - 获取八字信息列表
- ✅ `GET /api/bazi/info/default` - 获取默认八字信息
- ✅ `PUT /api/bazi/info/{id}/default` - 设置默认八字
- ✅ `DELETE /api/bazi/info/{id}` - 删除八字信息

### 反馈系统（3个接口）
- ✅ `POST /api/feedback` - 提交反馈
- ✅ `GET /api/feedback/list` - 获取反馈列表
- ✅ `GET /api/feedback/{id}` - 获取反馈详情

### 知识库（8个接口）
- ✅ `GET /api/knowledge/categories` - 获取所有分类
- ✅ `GET /api/knowledge/categories/top` - 获取顶级分类
- ✅ `GET /api/knowledge/categories/{parentId}/children` - 获取子分类
- ✅ `GET /api/knowledge/articles` - 获取文章列表
- ✅ `GET /api/knowledge/articles/category/{categoryId}` - 根据分类获取文章
- ✅ `GET /api/knowledge/article/{id}` - 获取文章详情
- ✅ `GET /api/knowledge/articles/search` - 搜索文章
- ✅ `POST /api/knowledge/article/{id}/like` - 点赞文章
- ✅ `POST /api/knowledge/article/{id}/collect` - 收藏文章

### 支付系统（7个接口）
- ✅ `POST /api/payment/create` - 创建订单
- ✅ `POST /api/payment/alipay/notify` - 支付宝异步通知
- ✅ `GET /api/payment/success` - 支付成功回调
- ✅ `GET /api/payment/order/{orderNo}` - 查询订单详情
- ✅ `GET /api/payment/orders` - 查询用户订单列表
- ✅ `GET /api/payment/membership/info` - 查询会员信息
- ✅ `GET /api/payment/membership/packages` - 查询会员套餐

---

## 接口统计

**总计：** 80+ 个API接口

**按模块分类：**
- 认证与授权：4个
- 用户管理：3个
- 八字分析：2个
- 趋势分析（NEW）：1个
- 多语言支持（NEW）：3个
- 塔罗占卜：8个
- 易经八字：9个
- 紫微斗数：7个
- 星座运势：5个
- DeepSeek AI：3个
- 分析管理：8个
- 用户八字信息：6个
- 反馈系统：3个
- 知识库：9个
- 支付系统：7个

---

## 测试脚本

### Postman Collection
可以导入以下环境变量：
```json
{
  "baseUrl": "http://localhost:8088",
  "token": "YOUR_ACCESS_TOKEN"
}
```

### Bash测试脚本
```bash
#!/bin/bash

# 设置基础URL
BASE_URL="http://localhost:8088"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "========================================="
echo "参天AI - API接口测试"
echo "========================================="

# 测试1: 发送验证码
echo -e "\n${GREEN}测试1: 发送短信验证码${NC}"
curl -X POST $BASE_URL/api/auth/sms/send \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试2: 登录
echo -e "\n${GREEN}测试2: 手机号登录${NC}"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/phone/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"123456"}')
echo $LOGIN_RESPONSE | jq '.'

# 提取Token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')
echo "Token: $TOKEN"

# 测试3: 获取用户信息
echo -e "\n${GREEN}测试3: 获取用户信息${NC}"
curl -X GET $BASE_URL/api/user/info \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

# 测试4: 八字分析
echo -e "\n${GREEN}测试4: 八字分析${NC}"
curl -X POST $BASE_URL/api/bazi/formatted \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gender":"male","solarDatetime":"1990-05-15 10:30"}' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

# 测试5: 趋势分析
echo -e "\n${GREEN}测试5: 趋势分析（NEW）${NC}"
curl -X POST $BASE_URL/api/trend/analysis \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bazi":"庚午 辛巳 甲寅 己巳",
    "gender":"male",
    "birthDate":"1990-05-15"
  }' \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

# 测试6: 多语言支持
echo -e "\n${GREEN}测试6: 获取支持的语言列表（NEW）${NC}"
curl -X GET $BASE_URL/api/i18n/languages \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

# 测试7: 分析统计
echo -e "\n${GREEN}测试7: 获取分析统计${NC}"
curl -X GET $BASE_URL/api/analysis/statistics \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n" | jq '.'

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}测试完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
```

保存为 `test_api.sh` 并执行：
```bash
chmod +x test_api.sh
./test_api.sh
```

---

## 测试注意事项

### 1. 限流测试
某些接口有限流保护，测试时注意：
- 短信验证码：60秒内最多1次
- 登录接口：60秒内最多5次
- 八字分析：60秒内最多10次
- 趋势分析：60秒内最多5次
- DeepSeek接口：60秒内最多5次

### 2. 认证测试
大部分接口需要Token认证，测试前确保：
- Token有效期为24小时
- 使用RefreshToken可以刷新AccessToken
- Token过期会返回401错误

### 3. 数据准备
测试前确保：
- MySQL数据库已启动
- Redis已启动（用于验证码存储）
- DeepSeek API Key已配置
- 支付宝配置已完成（如需测试支付）

### 4. 响应时间
- 普通查询接口：< 100ms
- 八字分析接口：< 2s
- DeepSeek AI接口：5-10s（取决于网络）
- 趋势分析接口：10-15s（包含AI解读）

---

## 常见问题

### Q1: Token无效怎么办？
A: 重新登录获取新Token，或使用RefreshToken刷新

### Q2: 验证码收不到？
A: 检查Redis是否启动，查看日志中的验证码

### Q3: 接口返回500错误？
A: 检查数据库连接、MCP服务配置、DeepSeek API Key

### Q4: 趋势分析返回慢？
A: 正常现象，包含AI解读需要10-15秒

---

**测试完成！所有80+个接口均可正常访问。**
