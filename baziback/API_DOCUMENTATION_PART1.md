# 参天AI - API接口文档（第1部分）

**项目名称：** 参天AI八字命理分析系统  
**版本：** 1.0.0  
**基础URL：** `http://localhost:8088`  
**文档生成时间：** 2024-11-24

---

## 目录

- [通用说明](#通用说明)
- [1. 认证与授权](#1-认证与授权)
- [2. 用户管理](#2-用户管理)
- [3. 八字分析](#3-八字分析)
- [4. 趋势分析（NEW）](#4-趋势分析new)

---

## 通用说明

### 认证方式

大部分接口需要JWT Token认证，在请求头中添加：

```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

### 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 错误码说明

- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权（Token无效或过期）
- `403`: 禁止访问
- `429`: 请求过于频繁（触发限流）
- `500`: 服务器内部错误

---

## 1. 认证与授权

### 1.1 发送短信验证码

**接口：** `POST /api/auth/sms/send`  
**认证：** 不需要  
**限流：** 60秒内最多1次（IP级别）

**请求参数：**

```json
{
  "phone": "18813158769"
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "phone": "13800138000",
    "expireSeconds": 300
  }
}
```

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/auth/sms/send \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}'
```

---

### 1.2 手机号登录/注册

**接口：** `POST /api/auth/phone/login`  
**认证：** 不需要  
**限流：** 60秒内最多5次（IP级别）

**功能说明：**

- 验证码正确且用户存在 → 登录
- 验证码正确但用户不存在 → 自动注册并登录

**请求参数：**

```json
{
  "phone": "18813158769",
  "code": "941065"
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "用户8000",
      "nickname": "用户8000",
      "phone": "13800138000",
      "isNewUser": false
    }
  }
}
```

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/auth/phone/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"123456"}'
```

---

### 1.3 刷新Token

**接口：** `POST /api/auth/token/refresh`  
**认证：** 需要RefreshToken

**请求头：**

```
Authorization: Bearer YOUR_REFRESH_TOKEN
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

---

### 1.4 获取验证码剩余时间

**接口：** `GET /api/auth/sms/ttl/{phone}`  
**认证：** 不需要

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "phone": "13800138000",
    "ttl": 245,
    "expired": false
  }
}
```

---

## 2. 用户管理

### 2.1 用户注册（用户名密码方式）

**接口：** `POST /api/user/register`  
**认证：** 不需要

**请求参数：**

```json
{
  "username": "testuser",
  "password": "password123",
  "phone": "13800138000"
}
```

---

### 2.2 用户登录（用户名密码方式）

**接口：** `POST /api/user/login`  
**认证：** 不需要

**请求参数：**

```json
{
  "username": "testuser",
  "password": "password123"
}
```

---

### 2.3 获取用户信息

**接口：** `GET /api/user/info`  
**认证：** 需要Token

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "phone": "13800138000",
    "status": 1,
    "createTime": "2024-11-20T10:30:00"
  }
}
```

---

## 3. 八字分析

### 3.1 获取可用工具列表

**接口：** `GET /api/bazi/tools`  
**认证：** 需要Token  
**限流：** 60秒内最多20次

---

### 3.2 获取八字详情（格式化响应）

**接口：** `POST /api/bazi/formatted`  
**认证：** 需要Token  
**限流：** 60秒内最多10次

**功能说明：**

- 自动保存分析历史到数据库
- 记录分析耗时和模型版本
- 返回前端友好的格式化数据

**请求参数：**

```json
{
  "lunarDatetime": "2000-05-15T12:00:00+08:00",
  "gender": 0,
  "eightCharProviderSect": 2
}
```

**参数说明：**

- `gender`: 性别，male（男）或 female（女）
- `solarDatetime`: 阳历日期时间，格式：YYYY-MM-DD HH:mm

**响应示例：**

```json
{
  "gender": "male",
  "solarDatetime": "1990-05-15 10:30",
  "bazi": "庚午 辛巳 甲寅 己巳",
  "wuxing": {
    "metal": 2,
    "wood": 2,
    "water": 0,
    "fire": 3,
    "earth": 1
  },
  "analysis": "八字分析内容..."
}
```

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/bazi/formatted \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"gender":"male","solarDatetime":"1990-05-15 10:30"}'
```

---

## 4. 趋势分析（NEW）

### 4.1 获取完整趋势分析

**接口：** `POST /api/trend/analysis`  
**认证：** 需要Token  
**限流：** 60秒内最多5次

**功能说明：**

- 大运分析（10年一个周期）
- 流年分析（当前年+未来5年）
- 流月分析（当前年12个月）
- 重要节点识别
- 风险预警提示
- AI深度解读

**请求参数：**

```json
{
  "bazi": "庚午 辛巳 甲寅 己巳",
  "gender": "male",
  "birthDate": "1990-05-15",
  "startAge": 0,
  "endAge": 80
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "dayunAnalysis": [
      {
        "startAge": 3,
        "endAge": 12,
        "startYear": 1993,
        "endYear": 2002,
        "dayunGanzhi": "壬午",
        "luckLevel": "较好",
        "luckScore": 75,
        "description": "壬午大运，运势较好。",
        "keyAreas": {
          "事业": 80,
          "财运": 70,
          "感情": 75,
          "健康": 85
        }
      }
    ],
    "liunianAnalysis": [...],
    "liuyueAnalysis": [...],
    "importantNodes": [...],
    "riskWarnings": [...],
    "aiInsight": "AI深度解读...",
    "analysisTime": "2024-11-24T16:30:00"
  }
}
```

**测试命令：**

```bash
curl -X POST http://localhost:8088/api/trend/analysis \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bazi":"庚午 辛巳 甲寅 己巳",
    "gender":"male",
    "birthDate":"1990-05-15"
  }'
```

---

**继续查看第2部分文档...**
