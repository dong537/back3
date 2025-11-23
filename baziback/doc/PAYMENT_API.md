# 支付宝支付系统 API 文档

## 概述

本系统实现了支付宝支付功能，支持两种支付模式：
1. **普通支付模式**：用户可以支付任意金额购买服务
2. **会员模式**：用户可以购买月度、季度或年度会员套餐

## 数据库初始化

在使用支付功能前，需要先执行数据库初始化脚本：

```bash
mysql -u root -p bazi < database/init_payment_tables.sql
```

## 配置说明

### 支付宝配置（application.yml）

```yaml
alipay:
  app-id: 你的支付宝应用ID
  private-key: 你的应用私钥
  alipay-public-key: 支付宝公钥
  sign-type: RSA2
  charset: UTF-8
  format: json
  sandbox: false  # 是否使用沙箱环境
  return-url: http://your-domain.com/api/payment/success  # 支付成功后的前端回调地址
  notify-url: http://your-domain.com/api/payment/alipay/notify  # 支付宝异步通知地址
```

**注意**：
- `notify-url` 必须是公网可访问的地址，本地开发时可以使用内网穿透工具（如ngrok）
- 生产环境建议使用环境变量配置敏感信息

## API 接口

### 1. 查询会员套餐列表

**接口地址**：`GET /api/payment/membership/packages`

**请求头**：无需认证

**响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "packageName": "月度会员",
      "packageType": 1,
      "durationDays": 30,
      "originalPrice": 99.00,
      "salePrice": 88.00,
      "discount": "8.9折",
      "description": "享受一个月的会员特权",
      "features": [
        "无限次算命",
        "专属客服",
        "优先体验新功能"
      ]
    },
    {
      "id": 2,
      "packageName": "季度会员",
      "packageType": 2,
      "durationDays": 90,
      "originalPrice": 297.00,
      "salePrice": 238.00,
      "discount": "8.0折",
      "description": "享受三个月的会员特权，更优惠",
      "features": [
        "无限次算命",
        "专属客服",
        "优先体验新功能",
        "季度专属报告"
      ]
    }
  ]
}
```

### 2. 创建订单（普通支付）

**接口地址**：`POST /api/payment/create`

**请求头**：
```
Authorization: Bearer {token}
```

**请求体**：
```json
{
  "orderType": 1,
  "productName": "八字详批服务",
  "productDesc": "专业八字命理分析",
  "amount": 99.00
}
```

**参数说明**：
- `orderType`: 订单类型，1-普通支付，2-会员购买
- `productName`: 商品名称
- `productDesc`: 商品描述（可选）
- `amount`: 订单金额（普通支付时必填）

**响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderNo": "ORD1700123456789ABCD1234",
    "payForm": "<form>...</form>",
    "expireMinutes": 30
  }
}
```

**使用说明**：
- 前端收到响应后，将 `payForm` 插入到页面中并自动提交，会跳转到支付宝支付页面
- 订单有效期为30分钟

### 3. 创建订单（会员购买）

**接口地址**：`POST /api/payment/create`

**请求头**：
```
Authorization: Bearer {token}
```

**请求体**：
```json
{
  "orderType": 2,
  "productName": "月度会员",
  "packageId": 1
}
```

**参数说明**：
- `orderType`: 订单类型，2表示会员购买
- `productName`: 商品名称
- `packageId`: 会员套餐ID（会员购买时必填）

**响应示例**：同普通支付

### 4. 查询订单详情

**接口地址**：`GET /api/payment/order/{orderNo}`

**请求头**：
```
Authorization: Bearer {token}
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderNo": "ORD1700123456789ABCD1234",
    "orderType": 1,
    "productName": "八字详批服务",
    "productDesc": "专业八字命理分析",
    "amount": 99.00,
    "status": 1,
    "statusDesc": "已支付",
    "tradeNo": "2024112322001234567890123456",
    "payTime": "2024-11-23 14:30:00",
    "expireTime": "2024-11-23 15:00:00",
    "createTime": "2024-11-23 14:25:00"
  }
}
```

**订单状态说明**：
- 0: 待支付
- 1: 已支付
- 2: 已取消
- 3: 已退款

### 5. 查询用户订单列表

**接口地址**：`GET /api/payment/orders`

**请求头**：
```
Authorization: Bearer {token}
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "orderNo": "ORD1700123456789ABCD1234",
      "orderType": 1,
      "productName": "八字详批服务",
      "amount": 99.00,
      "status": 1,
      "statusDesc": "已支付",
      "createTime": "2024-11-23 14:25:00"
    }
  ]
}
```

### 6. 查询用户会员信息

**接口地址**：`GET /api/payment/membership/info`

**请求头**：
```
Authorization: Bearer {token}
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "isMember": true,
    "membershipType": 1,
    "membershipTypeDesc": "月度会员",
    "startTime": "2024-11-23 14:30:00",
    "endTime": "2024-12-23 14:30:00",
    "remainingDays": 30
  }
}
```

### 7. 支付宝异步通知回调

**接口地址**：`POST /api/payment/alipay/notify`

**说明**：此接口由支付宝服务器调用，无需前端调用

**响应**：
- 成功：返回字符串 "success"
- 失败：返回字符串 "failure"

### 8. 支付成功前端回调

**接口地址**：`GET /api/payment/success?out_trade_no={orderNo}`

**说明**：用户支付成功后，支付宝会跳转到此地址

**响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "orderNo": "ORD1700123456789ABCD1234",
    "message": "支付成功"
  }
}
```

## 支付流程

### 普通支付流程

1. 前端调用 `/api/payment/create` 创建订单（orderType=1）
2. 后端返回支付表单HTML
3. 前端将表单插入页面并自动提交
4. 跳转到支付宝支付页面
5. 用户完成支付
6. 支付宝异步通知后端 `/api/payment/alipay/notify`
7. 后端更新订单状态
8. 支付宝跳转到 `/api/payment/success`
9. 前端展示支付成功页面

### 会员购买流程

1. 前端调用 `/api/payment/membership/packages` 获取套餐列表
2. 用户选择套餐
3. 前端调用 `/api/payment/create` 创建订单（orderType=2）
4. 后续流程同普通支付
5. 支付成功后，后端自动创建会员记录
6. 用户可通过 `/api/payment/membership/info` 查询会员信息

## 会员特权

会员购买成功后，可以享受以下特权：

### 月度会员（30天）
- 无限次算命
- 专属客服
- 优先体验新功能

### 季度会员（90天）
- 无限次算命
- 专属客服
- 优先体验新功能
- 季度专属报告

### 年度会员（365天）
- 无限次算命
- 专属客服
- 优先体验新功能
- 年度专属报告
- 生日专属礼物

## 测试说明

### 沙箱环境测试

1. 在 `application.yml` 中设置 `alipay.sandbox: true`
2. 使用支付宝沙箱账号进行测试
3. 沙箱买家账号：在支付宝开放平台获取

### 本地开发测试

由于支付宝异步通知需要公网地址，本地开发时需要：

1. 使用内网穿透工具（如ngrok）：
   ```bash
   ngrok http 8088
   ```

2. 将生成的公网地址配置到 `notify-url`：
   ```yaml
   alipay:
     notify-url: https://your-ngrok-url.ngrok.io/api/payment/alipay/notify
   ```

3. 同时在支付宝开放平台配置相同的通知地址

## 安全建议

1. **私钥保护**：应用私钥不要硬编码在代码中，使用环境变量或配置中心
2. **签名验证**：所有支付宝回调都必须验证签名
3. **幂等性**：支付回调可能重复，需要保证订单处理的幂等性
4. **金额校验**：回调时要验证订单金额是否匹配
5. **HTTPS**：生产环境必须使用HTTPS

## 常见问题

### Q1: 支付成功但订单状态未更新？
A: 检查支付宝异步通知地址是否配置正确，是否可以公网访问

### Q2: 签名验证失败？
A: 检查应用私钥和支付宝公钥是否配置正确，注意不要混淆

### Q3: 会员购买后没有生成会员记录？
A: 检查商品名称是否包含"月度"、"季度"或"年度"关键字

### Q4: 订单过期如何处理？
A: 系统会自动将过期未支付的订单标记为已取消（需要定时任务支持）

## 数据库表结构

### tb_order（订单表）
- 存储所有订单信息
- 支持普通支付和会员购买两种类型

### tb_membership（会员表）
- 存储用户会员信息
- 支持会员续费和叠加

### tb_membership_package（会员套餐表）
- 存储会员套餐配置
- 支持动态调整价格和特权

### tb_payment_log（支付日志表）
- 记录所有支付宝回调信息
- 用于问题排查和对账

## 联系方式

如有问题，请联系技术支持。
