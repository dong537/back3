# 易经占卜 MCP 服务 - 团队使用指南

## 📋 目录

1. [概述](#概述)
2. [团队成员安装指南](#团队成员安装指南)
3. [使用方式](#使用方式)
4. [发布到 NPM](#发布到-npm)
5. [内网部署方案](#内网部署方案)
6. [常见问题](#常见问题)

---

## 概述

本项目提供了一个易经占卜 MCP 服务，可以让团队成员在 Claude Desktop 中使用易经占卜功能。

### 架构说明

```
┌─────────────────┐
│ Claude Desktop  │
│   (MCP 客户端)   │
└────────┬────────┘
         │
         │ MCP 协议
         ▼
┌─────────────────┐
│  MCP 服务       │
│  (Node.js)      │
└────────┬────────┘
         │
         │ HTTP API
         ▼
┌─────────────────┐
│  后端服务       │
│  (Spring Boot)  │
└─────────────────┘
```

---

## 团队成员安装指南

### 方案一：使用 NPM 包（推荐）

适用于已发布到 NPM 的情况。

#### 步骤 1: 安装 MCP 服务

```bash
npm install -g @lldd/yijing-divination-mcp-server
```

#### 步骤 2: 启动后端服务

**选项 A - 使用团队共享服务器**

如果团队有共享服务器，只需配置后端地址即可（跳到步骤 3）。

**选项 B - 本地启动后端**

```bash
# 1. 克隆项目（或从团队共享位置获取）
git clone <your-repo-url>
cd baziback

# 2. 编译项目
mvn clean package -DskipTests

# 3. 启动服务
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

#### 步骤 3: 配置 Claude Desktop

**Windows 用户**

1. 打开配置文件：
   ```
   %APPDATA%\Claude\claude_desktop_config.json
   ```

2. 添加配置：
   ```json
   {
     "mcpServers": {
       "yijing-divination": {
         "command": "yijing-mcp",
         "env": {
           "YIJING_BACKEND_URL": "http://localhost:8088"
         }
       }
     }
   }
   ```

   如果使用团队共享服务器，将 `localhost:8088` 改为服务器地址，例如：
   ```json
   "YIJING_BACKEND_URL": "http://192.168.1.100:8088"
   ```

**macOS/Linux 用户**

1. 打开配置文件：
   ```
   ~/Library/Application Support/Claude/claude_desktop_config.json
   ```

2. 添加相同的配置（路径格式相同）

#### 步骤 4: 重启 Claude Desktop

关闭并重新打开 Claude Desktop，MCP 服务将自动加载。

#### 步骤 5: 验证安装

在 Claude Desktop 中输入：
```
帮我占卜一下今天的运势
```

如果 Claude 能够调用易经占卜工具并返回结果，说明安装成功！

---

### 方案二：使用本地构建版本

适用于未发布到 NPM 或需要本地开发的情况。

#### 步骤 1: 获取项目代码

```bash
# 从团队仓库克隆
git clone <your-repo-url>
cd baziback
```

#### 步骤 2: 构建 MCP 服务

```bash
cd mcp-server
npm install
npm run build
```

#### 步骤 3: 启动后端服务

```bash
cd ..
mvn clean package -DskipTests
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

#### 步骤 4: 配置 Claude Desktop

使用绝对路径配置：

**Windows**
```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "node",
      "args": [
        "C:\\path\\to\\baziback\\mcp-server\\dist\\index.js"
      ],
      "env": {
        "YIJING_BACKEND_URL": "http://localhost:8088"
      }
    }
  }
}
```

**macOS/Linux**
```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "node",
      "args": [
        "/path/to/baziback/mcp-server/dist/index.js"
      ],
      "env": {
        "YIJING_BACKEND_URL": "http://localhost:8088"
      }
    }
  }
}
```

---

## 使用方式

### 基础使用

在 Claude Desktop 中，可以直接用自然语言请求占卜：

```
# 快速占卜
帮我占卜一下今天的运势

# 指定起卦方法
用金钱卦帮我占卜事业发展

# 查询卦象信息
告诉我第1卦（乾卦）的详细信息

# 列出所有卦象
列出所有64卦
```

### 高级使用

如果需要更精确的控制，可以要求 Claude 使用特定的工具：

```
使用 yijing_generate_hexagram 工具，用时间起卦方法帮我占卜感情问题

使用 yijing_interpret_hexagram 工具解读第1卦，动爻为第6爻
```

---

## 发布到 NPM

如果您是项目管理员，想要发布 MCP 服务供团队使用：

### 步骤 1: 准备发布

```bash
cd mcp-server

# 确保已登录 NPM
npm login

# 检查包名是否可用
npm search @lldd/yijing-divination-mcp-server
```

### 步骤 2: 更新版本信息

编辑 `package.json`：

```json
{
  "name": "@your-org/yijing-divination-mcp-server",
  "version": "1.0.0",
  "repository": {
    "type": "git",
    "url": "https://github.com/your-org/your-repo.git"
  }
}
```

### 步骤 3: 发布

```bash
# 构建项目
npm run build

# 发布到 NPM
npm publish --access public
```

### 步骤 4: 通知团队

发布成功后，团队成员可以直接安装：

```bash
npm install -g @your-org/yijing-divination-mcp-server
```

---

## 内网部署方案

### 方案一：使用私有 NPM 仓库

适合有私有 NPM 仓库（如 Verdaccio）的团队。

#### 1. 配置 NPM 仓库

```bash
npm config set registry http://your-npm-registry:4873
```

#### 2. 发布到私有仓库

```bash
cd mcp-server
npm publish
```

#### 3. 团队成员安装

```bash
npm config set registry http://your-npm-registry:4873
npm install -g @lldd/yijing-divination-mcp-server
```

### 方案二：共享文件夹部署

适合小型团队或内网环境。

#### 1. 构建并打包

```bash
cd mcp-server
npm install
npm run build

# 打包整个 mcp-server 目录
cd ..
tar -czf yijing-mcp-server.tar.gz mcp-server/
```

#### 2. 分发给团队

将 `yijing-mcp-server.tar.gz` 放到共享文件夹。

#### 3. 团队成员使用

```bash
# 解压到本地
tar -xzf yijing-mcp-server.tar.gz
cd mcp-server
npm install --production

# 配置 Claude Desktop 使用本地路径
```

### 方案三：集中式后端服务

推荐用于团队协作。

#### 1. 部署后端服务到服务器

```bash
# 在服务器上
cd baziback
mvn clean package -DskipTests

# 使用 systemd 或 supervisor 管理服务
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

#### 2. 配置防火墙

确保端口 8088 可被团队成员访问。

#### 3. 团队成员配置

所有成员的 Claude Desktop 配置指向同一个后端：

```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "yijing-mcp",
      "env": {
        "YIJING_BACKEND_URL": "http://team-server:8088"
      }
    }
  }
}
```

**优势**：
- ✅ 统一维护，只需部署一个后端
- ✅ 节省资源，不需要每个人启动后端
- ✅ 数据共享（如果需要）

---

## 常见问题

### Q1: 如何更新 MCP 服务？

**使用 NPM 包**：
```bash
npm update -g @lldd/yijing-divination-mcp-server
```

**使用本地构建**：
```bash
git pull
cd mcp-server
npm run rebuild
```

然后重启 Claude Desktop。

### Q2: 多个团队成员可以共用一个后端服务吗？

可以！这是推荐的部署方式。只需：
1. 在服务器上部署一个后端服务
2. 所有成员的 `YIJING_BACKEND_URL` 指向该服务器
3. 确保网络可达和防火墙配置正确

### Q3: 如何查看 MCP 服务日志？

**Windows**：
- 打开 Claude Desktop 的开发者工具（如果可用）
- 或查看 `%APPDATA%\Claude\logs\`

**macOS/Linux**：
- 查看 `~/Library/Logs/Claude/`

### Q4: 后端服务需要数据库吗？

易经占卜功能**不需要**数据库，64卦数据已内置在代码中。

数据库仅用于用户系统（登录/注册），如果不需要可以注释掉相关配置。

### Q5: 如何配置 DeepSeek API Key？

编辑后端的 `application.yml`：

```yaml
deepseek:
  api:
    key: sk-your-api-key-here
    endpoint: https://api.deepseek.com/v1/chat/completions
```

或使用环境变量：
```bash
export DEEPSEEK_API_KEY=sk-your-api-key-here
```

### Q6: 可以在没有网络的环境使用吗？

可以，但有限制：
- ✅ 起卦功能完全可用（不需要网络）
- ✅ 获取卦象信息完全可用
- ❌ AI 解读功能需要 DeepSeek API（需要网络）

### Q7: 如何卸载？

**卸载 NPM 包**：
```bash
npm uninstall -g @lldd/yijing-divination-mcp-server
```

**删除 Claude Desktop 配置**：
从 `claude_desktop_config.json` 中删除 `yijing-divination` 配置项。

---

## 📞 技术支持

### 遇到问题时的检查清单

- [ ] 后端服务是否正常运行？（访问 http://localhost:8088）
- [ ] MCP 服务是否正确安装？（`npm list -g`）
- [ ] Claude Desktop 配置是否正确？（JSON 格式）
- [ ] 环境变量 `YIJING_BACKEND_URL` 是否正确？
- [ ] 防火墙是否允许访问？
- [ ] Node.js 版本是否 >= 18？（`node --version`）
- [ ] Java 版本是否 >= 17？（`java --version`）

### 获取帮助

1. 查看项目文档：`README.md`、`MCP_DEPLOYMENT_GUIDE.md`
2. 查看后端日志：检查 Spring Boot 启动日志
3. 查看 MCP 服务日志：Claude Desktop 日志目录
4. 联系项目维护者

---

## 📝 版本历史

### v1.0.0 (2024-12-18)
- ✅ 初始版本发布
- ✅ 支持 5 种起卦方法
- ✅ 完整的 64 卦数据
- ✅ AI 智能解读

---

## 📄 许可证

MIT License - 仅供团队内部使用

---

**最后更新**: 2024-12-18  
**维护者**: LLDD  
**项目版本**: 1.0.0
