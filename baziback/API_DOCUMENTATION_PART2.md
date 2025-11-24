# 参天AI - API接口文档（第2部分）

## 目录

- [5. 多语言支持（NEW）](#5-多语言支持new)
- [6. 支付系统](#6-支付系统)
- [7. 分析管理](#7-分析管理)
- [8. 用户八字信息](#8-用户八字信息)

---

## 5. 多语言支持（NEW）

### 5.1 获取支持的语言列表

**接口：** `GET /api/i18n/languages`  
**认证：** 不需要

**功能说明：**
返回系统支持的所有语言及其名称

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "zh-CN": "简体中文",
    "zh-TW": "繁體中文",
    "en": "English",
    "ja": "日本語",
    "ko": "한국어",
    "es": "Español",
    "fr": "Français",
    "de": "Deutsch",
    "ru": "Русский",
    "pt": "Português"
  }
}
```

**测试命令：**

```bash
curl -X GET http://localhost:8088/api/i18n/languages
```

---

### 5.2 翻译分析结果

**接口：** `POST /api/i18n/translate`  
**认证：** 需要Token  
**限流：** 60秒内最多10次

**功能说明：**
将八字分析结果翻译成目标语言，保持专业术语准确性

**请求参数：**

```json
{
  "content": "八字分析结果内容...",
  "targetLanguage": "en"
}
```

**参数说明：**

- `content`: 原始内容（中文）
- `targetLanguage`: 目标语言代码（en, ja, ko等）

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "Translated content in English..."
}
```

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/i18n/translate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content":"您的八字显示事业运势较好",
    "targetLanguage":"en"
  }'
```

---

### 5.3 生成多语言报告

**接口：** `POST /api/i18n/report`  
**认证：** 需要Token  
**限流：** 60秒内最多5次

**功能说明：**
直接生成指定语言的八字分析报告

**请求参数：**

```json
{
  "baziData": "庚午 辛巳 甲寅 己巳",
  "reportType": "comprehensive",
  "language": "ja"
}
```

**参数说明：**

- `baziData`: 八字数据
- `reportType`: 报告类型（comprehensive/career/love/health/wealth）
- `language`: 语言代码

**报告类型说明：**

- `comprehensive`: 综合分析
- `career`: 事业运势
- `love`: 感情运势
- `health`: 健康运势
- `wealth`: 财运分析

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/i18n/report \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "baziData":"庚午 辛巳 甲寅 己巳",
    "reportType":"comprehensive",
    "language":"ja"
  }'
```

---

## 6. 支付系统

### 6.1 创建订单并获取支付表单

**接口：** `POST /api/payment/create`  
**认证：** 需要Token

**请求参数：**

```json
{
  "orderType": "membership",
  "packageId": 1,
  "amount": 99.00,
  "paymentMethod": "alipay"
}
```

**参数说明：**

- `orderType`: 订单类型（membership/service/analysis）
- `packageId`: 套餐ID
- `amount`: 金额
- `paymentMethod`: 支付方式（alipay/wechat）

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderNo": "ORD20241124001",
    "paymentForm": "<form>...</form>",
    "qrCode": "https://..."
  }
}
```

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/payment/create \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderType":"membership",
    "packageId":1,
    "amount":99.00,
    "paymentMethod":"alipay"
  }'
```

---

### 6.2 查询订单详情

**接口：** `GET /api/payment/order/{orderNo}`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderNo": "ORD20241124001",
    "orderType": "membership",
    "amount": 99.00,
    "status": "paid",
    "createTime": "2024-11-24T10:30:00",
    "payTime": "2024-11-24T10:35:00"
  }
}
```

**测试命令：**

```bash
curl -X GET http://localhost:8088/api/payment/order/ORD20241124001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 6.3 查询用户订单列表

**接口：** `GET /api/payment/orders`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "orderNo": "ORD20241124001",
      "orderType": "membership",
      "amount": 99.00,
      "status": "paid",
      "createTime": "2024-11-24T10:30:00"
    }
  ]
}
```

---

### 6.4 查询用户会员信息

**接口：** `GET /api/payment/membership/info`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "membershipLevel": "premium",
    "expireTime": "2025-11-24T10:30:00",
    "remainingDays": 365,
    "benefits": ["无限次分析", "AI深度解读", "专属客服"]
  }
}
```

---

### 6.5 查询所有会员套餐

**接口：** `GET /api/payment/membership/packages`  
**认证：** 不需要

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "月度会员",
      "price": 29.00,
      "duration": 30,
      "benefits": ["每日10次分析", "基础报告"]
    },
    {
      "id": 2,
      "name": "年度会员",
      "price": 299.00,
      "duration": 365,
      "benefits": ["无限次分析", "高级报告", "专属客服"]
    }
  ]
}
```

---

### 6.6 支付宝异步通知回调

**接口：** `POST /api/payment/alipay/notify`  
**认证：** 不需要（支付宝回调）

**说明：**
此接口由支付宝服务器调用，用于通知支付结果

---

### 6.7 支付成功前端回调

**接口：** `GET /api/payment/success?out_trade_no=xxx`  
**认证：** 不需要

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderNo": "ORD20241124001",
    "message": "支付成功"
  }
}
```

---

## 7. 分析管理

### 7.1 获取分析历史列表

**接口：** `GET /api/analysis/history`  
**认证：** 需要Token

**请求参数：**

- `page`: 页码（默认1）
- `pageSize`: 每页数量（默认10）
- `analysisType`: 分析类型（可选，bazi/tarot/yijing/ziwei/zodiac）

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "analysisType": "bazi",
      "requestData": {...},
      "responseData": {...},
      "analysisDuration": 1500,
      "isFavorite": 0,
      "createTime": "2024-11-24T10:30:00"
    }
  ]
}
```

**测试命令：**

```bash
curl -X GET "http://localhost:8088/api/analysis/history?page=1&pageSize=10&analysisType=bazi" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 7.2 获取分析历史详情

**接口：** `GET /api/analysis/history/{id}`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "analysisType": "bazi",
    "requestData": {...},
    "responseData": {...},
    "analysisDuration": 1500,
    "modelVersion": "mcp-bazi-v1",
    "isFavorite": 0,
    "createTime": "2024-11-24T10:30:00"
  }
}
```

---

### 7.3 收藏/取消收藏分析历史

**接口：** `POST /api/analysis/history/{id}/favorite`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "isFavorite": 1
  }
}
```

---

### 7.4 获取收藏列表

**接口：** `GET /api/analysis/history/favorites`  
**认证：** 需要Token

**请求参数：**

- `page`: 页码（默认1）
- `pageSize`: 每页数量（默认10）

---

### 7.5 获取分析统计

**接口：** `GET /api/analysis/statistics`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalCount": 25,
    "baziCount": 10,
    "tarotCount": 5,
    "yijingCount": 3,
    "ziweiCount": 4,
    "zodiacCount": 3
  }
}
```

**测试命令：**

```bash
curl -X GET http://localhost:8088/api/analysis/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 7.6 获取报告列表

**接口：** `GET /api/analysis/reports`  
**认证：** 需要Token

**请求参数：**

- `page`: 页码（默认1）
- `pageSize`: 每页数量（默认10）
- `reportType`: 报告类型（可选）

---

### 7.7 获取报告详情

**接口：** `GET /api/analysis/report/{id}`  
**认证：** 需要Token

---

### 7.8 导出报告

**接口：** `POST /api/analysis/report/{id}/export`  
**认证：** 需要Token

**请求参数：**

```json
{
  "format": "pdf"
}
```

**参数说明：**

- `format`: 导出格式（pdf/markdown/html）

---

## 8. 用户八字信息

### 8.1 创建八字信息

**接口：** `POST /api/bazi/info`  
**认证：** 需要Token

**请求参数：**

```json
{
  "name": "张三",
  "gender": 1,
  "birthYear": 1990,
  "birthMonth": 5,
  "birthDay": 15,
  "birthHour": 10,
  "birthMinute": 30,
  "isLunar": 0,
  "timezone": "Asia/Shanghai",
  "birthplace": "北京",
  "isDefault": 1
}
```

**参数说明：**

- `gender`: 性别（1=男，0=女）
- `isLunar`: 是否农历（1=是，0=否）
- `isDefault`: 是否默认（1=是，0=否）

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "bazi": "庚午 辛巳 甲寅 己巳"
  }
}
```

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/bazi/info \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"张三",
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
```

---

### 8.2 更新八字信息

**接口：** `PUT /api/bazi/info`  
**认证：** 需要Token

---

### 8.3 获取八字信息列表

**接口：** `GET /api/bazi/info/list`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "bazi": "庚午 辛巳 甲寅 己巳",
      "isDefault": 1,
      "createTime": "2024-11-24T10:30:00"
    }
  ]
}
```

---

### 8.4 获取默认八字信息

**接口：** `GET /api/bazi/info/default`  
**认证：** 需要Token

---

### 8.5 设置默认八字

**接口：** `PUT /api/bazi/info/{id}/default`  
**认证：** 需要Token

---

### 8.6 删除八字信息

**接口：** `DELETE /api/bazi/info/{id}`  
**认证：** 需要Token

---

**继续查看第3部分文档...**
