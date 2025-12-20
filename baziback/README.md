# 玄学占卜系统 - API 文档

## 📖 项目简介

基于 Spring Boot 的综合玄学占卜系统，提供 **易经占卜** 和 **塔罗牌** 两大核心功能，支持 MCP 协议接入。

### 核心功能

| 模块 | 功能 | 状态 |
|------|------|------|
| **易经占卜** | 64卦数据、5种起卦方法、动爻变卦、AI解读 | ✅ 可用 |
| **塔罗牌** | 78张牌、12种牌阵、正逆位解读 | ✅ 可用 |
| **MCP 服务** | 支持 Claude/Cursor 等 AI 工具调用 | ✅ 可用 |
| **DeepSeek AI** | 智能解读与分析 | ✅ 可用 |
| **八字/紫微/星座** | 命理分析 | ⚠️ 开发中 |

---

## 🚀 快速开始

### 环境要求

- **Java**: 17 或更高版本
- **Maven**: 3.6+
- **MySQL**: 5.7+ (可选，用于用户系统)

### 1. 克隆项目

```bash
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
```

### 2. 配置数据库（可选）

如果需要使用用户系统，请配置 MySQL 数据库：

```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bazi?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### 3. 编译项目

```bash
mvn clean package -DskipTests
```

### 4. 启动项目

```bash
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

或使用 Maven：

```bash
mvn spring-boot:run
```

### 5. 验证启动

访问：`http://localhost:8088`

看到以下日志表示启动成功：
```
Tomcat started on port 8088 (http) with context path '/'
Started Demo1Application in X.XXX seconds
```

---

## 📡 API 接口文档

### 基础信息

- **Base URL**: `http://localhost:8088`
- **Content-Type**: `application/json`
- **端口**: 8088

---

## 🎯 易经占卜 API

### 1. 生成卦象

**接口**: `POST /api/yijing/hexagram/generate`

**请求参数**:

```json
{
  "question": "今天运势如何？",
  "method": "time"
}
```

**method 参数说明**:
- `time` - 时间起卦（根据当前时间）
- `random` - 随机起卦
- `number` - 数字起卦（需提供 seed）
- `coin` - 金钱卦（模拟投掷硬币）
- `plum_blossom` - 梅花易数

**响应示例**:

```json
{
  "success": true,
  "message": "生成成功",
  "data": {
    "hexagram": {
      "id": 1,
      "name": "乾",
      "chinese": "乾为天",
      "binary": "111111",
      "judgment": "元亨利贞",
      "image": "天行健，君子以自强不息",
      "meaning": "刚健中正，自强不息"
    },
    "changingLines": [6],
    "changedHexagram": {
      "id": 44,
      "name": "姤",
      "chinese": "天风姤"
    },
    "method": "time",
    "question": "今天运势如何？",
    "timestamp": "2024-12-18T22:00:00"
  }
}
```

### 2. 解读卦象

**接口**: `POST /api/yijing/hexagram/interpret`

**请求参数**:

```json
{
  "hexagramId": 1,
  "changingLines": [6],
  "question": "今天运势如何？"
}
```

**响应示例**:

```json
{
  "success": true,
  "message": "解读成功",
  "data": {
    "interpretation": "卦象解读内容...",
    "advice": "建议内容..."
  }
}
```

### 3. 获取所有卦象

**接口**: `GET /api/yijing/hexagrams`

**响应示例**:

```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "hexagrams": [
      {
        "id": 1,
        "name": "乾",
        "chinese": "乾为天",
        "binary": "111111"
      }
      // ... 共64卦
    ],
    "total": 64
  }
}
```

### 4. 获取指定卦象

**接口**: `GET /api/yijing/hexagram/{id}`

**路径参数**: `id` - 卦象ID (1-64)

### 5. 快速占卜（推荐）

**接口**: `POST /api/standalone/yijing/quick-divination`

一键完成起卦和解读，最便捷的占卜方式。

**请求参数**:
```json
{
  "question": "今年事业运势如何？",
  "method": "time"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "占卜成功",
  "data": {
    "question": "今年事业运势如何？",
    "hexagram_name": "乾",
    "judgment": "元亨利贞",
    "image": "天行健，君子以自强不息",
    "meaning": "刚健中正，自强不息",
    "keywords": ["刚健", "自强", "领导"],
    "applications": ["事业", "领导", "决策"],
    "interpretation_hint": "此卦象征天道刚健..."
  }
}
```

### 6. 获取起卦方法列表

**接口**: `GET /api/standalone/yijing/methods`

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "methods": {
      "time": "时间起卦 - 根据当前时间生成卦象",
      "random": "随机起卦 - 完全随机生成卦象",
      "number": "数字起卦 - 根据提供的数字种子生成卦象",
      "coin": "金钱卦 - 模拟投掷三枚硬币六次",
      "plum_blossom": "梅花易数 - 结合时间和外应生成卦象"
    }
  }
}
```

### 7. 获取指定卦象详情

**接口**: `GET /api/yijing/hexagram/{id}`

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 1,
    "name": "Qian",
    "chinese": "乾",
    "binary": "111111",
    "upper": "乾",
    "lower": "乾",
    "judgment": "元亨利贞",
    "image": "天行健，君子以自强不息",
    "meaning": "刚健中正，自强不息",
    "keywords": ["刚健", "自强", "领导"],
    "element": "金",
    "season": "秋",
    "direction": "西北",
    "applications": ["事业", "领导", "决策"],
    "lines": [
      {
        "position": 1,
        "text": "初九：潜龙勿用",
        "meaning": "时机未到，需要等待"
      }
      // ... 共6爻
    ]
  }
}
```

---

## 🧪 测试示例

### PowerShell 测试

```powershell
# 1. 生成卦象
$body = @{
    question = "今天运势如何？"
    method = "time"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8088/api/yijing/hexagram/generate" -Method Post -Body $body -ContentType "application/json"
$response | ConvertTo-Json -Depth 10

# 2. 获取所有卦象
Invoke-RestMethod -Uri "http://localhost:8088/api/yijing/hexagrams" -Method Get

# 3. 获取指定卦象
Invoke-RestMethod -Uri "http://localhost:8088/api/yijing/hexagram/1" -Method Get
```

### cURL 测试

```bash
# 1. 生成卦象
curl -X POST http://localhost:8088/api/yijing/hexagram/generate \
  -H "Content-Type: application/json" \
  -d '{"question":"今天运势如何？","method":"time"}'

# 2. 获取所有卦象
curl http://localhost:8088/api/yijing/hexagrams

# 3. 获取指定卦象
curl http://localhost:8088/api/yijing/hexagram/1
```

---

## 🔧 配置说明

### application.yml 配置

```yaml
# 服务器端口
server:
  port: 8088

# JWT 配置
jwt:
  secret: your-secret-key-at-least-256-bits-long
  expiration: 86400000  # 24小时

# DeepSeek AI 配置
deepseek:
  api:
    key: your-deepseek-api-key
    endpoint: https://api.deepseek.com/v1/chat/completions
  model: deepseek-chat
  temperature: 0.5

# 数据库配置（可选）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bazi
    username: root
    password: 123456
```

### 环境变量配置

可以通过环境变量覆盖配置：

```bash
# Windows
set SERVER_PORT=8088
set JWT_SECRET=your-secret-key
set DEEPSEEK_API_KEY=your-api-key

# Linux/Mac
export SERVER_PORT=8088
export JWT_SECRET=your-secret-key
export DEEPSEEK_API_KEY=your-api-key
```

---

## 📚 六十四卦速查表

### 八宫卦序

| 卦序 | 卦名 | 卦象 | 卦序 | 卦名 | 卦象 |
|------|------|------|------|------|------|
| 1 | 乾 | ☰☰ | 33 | 遁 | ☰☶ |
| 2 | 坤 | ☷☷ | 34 | 大壮 | ☳☰ |
| 3 | 屯 | ☵☳ | 35 | 晋 | ☲☷ |
| 4 | 蒙 | ☶☵ | 36 | 明夷 | ☷☲ |
| 5 | 需 | ☵☰ | 37 | 家人 | ☴☲ |
| 6 | 讼 | ☰☵ | 38 | 睽 | ☲☱ |
| 7 | 师 | ☷☵ | 39 | 蹇 | ☵☶ |
| 8 | 比 | ☵☷ | 40 | 解 | ☳☵ |

*完整64卦数据请参考代码中的 `HexagramRepository.java`*

---

## 🎲 起卦方法说明

### 1. 时间起卦 (time)

根据当前时间自动计算卦象，适合日常占卜。

**原理**: 使用年月日时的数字进行计算

### 2. 随机起卦 (random)

完全随机生成卦象，适合快速占卜。

### 3. 数字起卦 (number)

使用指定的数字作为种子生成卦象。

**请求示例**:
```json
{
  "method": "number",
  "question": "事业发展如何？",
  "seed": "123456"
}
```

### 4. 金钱卦 (coin)

模拟传统的投掷硬币起卦方法。

### 5. 梅花易数 (plum_blossom)

使用梅花易数的起卦方法。

---

## 🃏 塔罗牌 API

### 塔罗牌数据概览

- **78张完整塔罗牌** - 22张大阿卡纳 + 56张小阿卡纳
- **12种牌阵** - 从单牌到凯尔特十字
- **正逆位解读** - 每张牌包含正位和逆位含义

### 塔罗牌阵类型

| 牌阵类型 | 英文名 | 牌数 | 适用场景 |
|----------|--------|------|----------|
| 单牌 | SINGLE | 1 | 快速指引 |
| 时间之流 | PAST_PRESENT_FUTURE | 3 | 时间线分析 |
| 爱情三角 | LOVE_TRIAD | 3 | 感情问题 |
| 凯尔特十字 | CELTIC_CROSS | 10 | 深度分析 |
| 马蹄铁 | HORSESHOE | 7 | 综合分析 |
| 关系十字 | RELATIONSHIP_CROSS | 5 | 人际关系 |
| 事业之路 | CAREER_PATH | 6 | 职业发展 |
| 决策分析 | DECISION_MAKING | 5 | 选择判断 |
| 灵性指引 | SPIRITUAL_GUIDANCE | 4 | 心灵成长 |
| 年度运势 | YEAR_AHEAD | 12 | 全年预测 |
| 脉轮校准 | CHAKRA_ALIGNMENT | 7 | 能量分析 |
| 阴影工作 | SHADOW_WORK | 6 | 内在探索 |

### 大阿卡纳牌 (22张)

| ID | 牌名 | 关键词 | 正位含义 |
|----|------|--------|----------|
| 1 | 愚者 | 开始;自由;冒险 | 新的开始、信任与尝试 |
| 2 | 魔术师 | 行动;创造;掌控 | 资源齐备、行动力强 |
| 3 | 女祭司 | 直觉;沉静;秘密 | 直觉与洞察 |
| 4 | 女皇 | 滋养;丰盛;关怀 | 丰盛、滋养与关系经营 |
| 5 | 皇帝 | 秩序;权威;稳定 | 结构、规则与负责 |
| 6 | 教皇 | 信念;导师;传统 | 学习、传统与价值观 |
| 7 | 恋人 | 选择;关系;一致 | 选择、关系与价值一致 |
| 8 | 战车 | 意志;推进;胜利 | 推进与胜利 |
| 9 | 力量 | 勇气;耐心;自控 | 温柔而坚定的自控 |
| 10 | 隐者 | 内省;智慧;独处 | 内省寻路 |
| 11 | 命运之轮 | 变化;转机;周期 | 周期变化与转机 |
| 12 | 正义 | 原则;平衡;决断 | 公平、因果与清晰决断 |
| 13 | 倒吊人 | 转念;等待;暂停 | 换视角、暂停与重新评估 |
| 14 | 死神 | 转化;告别;重生 | 结束与新生 |
| 15 | 节制 | 调和;节奏;整合 | 调和、整合与节奏管理 |
| 16 | 恶魔 | 欲望;束缚;依赖 | 欲望与束缚的提醒 |
| 17 | 高塔 | 破局;重建;冲击 | 突变与破局 |
| 18 | 星星 | 希望;疗愈;信心 | 希望、疗愈与信心重建 |
| 19 | 月亮 | 迷雾;直觉;不安 | 不确定与潜意识 |
| 20 | 太阳 | 喜悦;清晰;成功 | 清晰、喜悦与正向成果 |
| 21 | 审判 | 觉醒;复盘;呼唤 | 复盘觉醒与进入新阶段 |
| 22 | 世界 | 完成;圆满;整合 | 完成、圆满与整合 |

### 小阿卡纳牌 (56张)

分为四个花色，每个花色14张（Ace-10 + 侍从/骑士/王后/国王）：

| 花色 | 英文 | 元素 | 代表领域 |
|------|------|------|----------|
| 权杖 | Wands | 火 | 行动、热情、创造力 |
| 圣杯 | Cups | 水 | 情感、关系、直觉 |
| 宝剑 | Swords | 风 | 思维、沟通、冲突 |
| 钱币 | Pentacles | 土 | 物质、工作、健康 |

---

## 🛠️ 开发指南

### 项目结构

```
baziback/
├── src/main/java/com/example/demo/
│   ├── yijing/                    # 易经模块
│   │   ├── model/                 # 数据模型
│   │   ├── repository/            # 64卦数据仓库
│   │   └── service/               # 业务逻辑
│   ├── tarot/                     # 塔罗牌模块
│   │   ├── model/                 # TarotCard, SpreadType
│   │   ├── repository/            # 78张牌数据仓库
│   │   └── service/               # 牌阵服务
│   ├── controller/                # REST API 控制器
│   ├── service/                   # 服务层
│   ├── config/                    # 配置类
│   └── util/                      # 工具类
├── mcp-server/                    # MCP 服务端（TypeScript）
│   ├── src/index.ts               # MCP 入口
│   └── package.json               # 依赖配置
├── src/main/resources/
│   └── application.yml            # 配置文件
└── pom.xml                        # Maven 配置
```

### 核心类说明

**易经模块**:
- `HexagramRepository` - 存储完整的64卦数据
- `HexagramGeneratorService` - 实现各种起卦算法
- `StandaloneYijingService` - 独立易经服务
- `YijingController` - 易经 REST API 接口

**塔罗牌模块**:
- `TarotDeckRepository` - 存储78张塔罗牌数据
- `SpreadCatalog` - 12种牌阵定义
- `TarotCard` - 塔罗牌数据模型
- `SpreadType` - 牌阵类型枚举

---

## 🔌 MCP 服务接入

### MCP 协议简介

MCP (Model Context Protocol) 是一种让 AI 助手调用外部工具的标准协议。本项目提供完整的 MCP 服务端，支持 Claude Desktop、Cursor、Windsurf 等 AI 工具直接调用占卜功能。

### MCP 工具列表

| 工具名称 | 功能描述 |
|----------|----------|
| `yijing_generate_hexagram` | 生成易经卦象 |
| `yijing_interpret_hexagram` | 解读卦象含义 |
| `yijing_get_hexagram` | 获取指定卦象详情 |
| `yijing_list_hexagrams` | 获取64卦列表 |
| `yijing_quick_divination` | 快速占卜（一键起卦+解读） |

### 本地部署完整指南

#### 步骤一：启动后端服务

```bash
# 1. 进入项目目录
cd baziback

# 2. 编译项目
mvn clean package -DskipTests

# 3. 启动后端
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

验证后端运行：
```bash
curl http://localhost:8088/api/yijing/hexagrams
```

#### 步骤二：构建 MCP 服务

```bash
# 1. 进入 MCP 目录
cd mcp-src
# 或使用根目录的 MCP 配置
cd ..

# 2. 安装依赖
npm install

# 3. 编译 TypeScript
npm run build
```

#### 步骤三：配置 AI 工具

**Claude Desktop 配置**

找到配置文件：
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`

添加以下内容：
```json
{
  "mcpServers": {
    "yijing-tarot": {
      "command": "node",
      "args": ["C:/Users/Lenovo/Desktop/n8n/back3/baziback/mcp-dist/index.js"],
      "env": {
        "YIJING_BACKEND_URL": "http://localhost:8088"
      }
    }
  }
}
```

**Cursor 配置**

在 Cursor 设置 → MCP Servers 中添加：
```json
{
  "yijing-tarot": {
    "command": "node",
    "args": ["C:/Users/Lenovo/Desktop/n8n/back3/baziback/mcp-dist/index.js"],
    "env": {
      "YIJING_BACKEND_URL": "http://localhost:8088"
    }
  }
}
```

**Windsurf 配置**

编辑 `~/.codeium/windsurf/mcp_config.json`：
```json
{
  "mcpServers": {
    "yijing-tarot": {
      "command": "node",
      "args": ["C:/Users/Lenovo/Desktop/n8n/back3/baziback/mcp-dist/index.js"],
      "env": {
        "YIJING_BACKEND_URL": "http://localhost:8088"
      }
    }
  }
}
```

#### 步骤四：验证 MCP 服务

重启你的 AI 工具（Claude/Cursor/Windsurf），然后尝试：
- "帮我用时间起卦法占卜今天运势"
- "查看易经第一卦乾卦的详细信息"
- "列出所有64卦"

---

### 云服务器部署

如果后端部署在云服务器：

```bash
# 1. 克隆代码
git clone https://github.com/dong537/back3.git
cd back3/baziback

# 2. 编译并启动后端
mvn clean package -DskipTests
nohup java -jar target/bazi-0.0.1-SNAPSHOT.jar &

# 3. 安装 MCP
npm install && npm run build

# 4. 使用 PM2 管理（可选）
npm install -g pm2
pm2 start mcp-dist/index.js --name yijing-mcp
```

然后在本地 AI 工具中配置，将 `YIJING_BACKEND_URL` 改为你的服务器地址：
```json
{
  "env": {
    "YIJING_BACKEND_URL": "http://你的服务器IP:8088"
  }
}
```

---

## ❓ 常见问题

### Q1: 项目启动失败，提示端口被占用

**A**: 修改 `application.yml` 中的端口号：

```yaml
server:
  port: 8089  # 改为其他端口
```

### Q2: 数据库连接失败

**A**: 如果不需要用户系统，可以注释掉数据库相关配置。或者确保 MySQL 已启动并创建了 `bazi` 数据库。

### Q3: JWT 配置错误

**A**: 确保 `application.yml` 中配置了 `jwt.secret`，密钥长度至少 256 位。

### Q4: 如何使用 DeepSeek AI 解读

**A**: 需要配置有效的 DeepSeek API Key：

```yaml
deepseek:
  api:
    key: sk-your-api-key-here
```

---

## 📝 更新日志

### v1.0.0 (2024-12-19)

**易经占卜**:
- ✅ 完整的64卦数据（含卦辞、象辞、爻辞）
- ✅ 5种起卦方法（时间/随机/数字/金钱/梅花易数）
- ✅ 自动变卦计算
- ✅ DeepSeek AI 智能解读

**塔罗牌**:
- ✅ 78张完整塔罗牌数据
- ✅ 22张大阿卡纳 + 56张小阿卡纳
- ✅ 12种牌阵支持
- ✅ 正逆位含义解读

**MCP 服务**:
- ✅ 支持 Claude Desktop / Cursor / Windsurf
- ✅ TypeScript 实现
- ✅ STDIO 传输协议

---

## 📄 许可证

本项目仅供学习和研究使用。

---

## 🤝 联系方式

**GitHub**: [项目地址]  
**问题反馈**: 请提交 Issue

---

## 🔗 相关链接

- [MCP 协议官方文档](https://modelcontextprotocol.io/)
- [DeepSeek API 文档](https://platform.deepseek.com/docs)
- [魔搭 MCP 广场](https://www.modelscope.cn/mcp)

---

**最后更新**: 2024-12-19  
**版本**: 1.0.0  
**状态**: ✅ 生产就绪
