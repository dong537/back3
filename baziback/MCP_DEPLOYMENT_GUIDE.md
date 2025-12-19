# 易经占卜 MCP 服务部署指南

本指南将帮助您将易经占卜项目打包并部署为 MCP 服务。

## 📦 项目结构

```
baziback/
├── mcp-server/              # MCP 服务目录
│   ├── src/
│   │   └── index.ts        # MCP 服务入口
│   ├── dist/               # 编译输出（自动生成）
│   ├── package.json        # NPM 配置
│   ├── tsconfig.json       # TypeScript 配置
│   └── README.md           # MCP 服务文档
├── src/                    # Java 后端源码
├── target/                 # Java 编译输出
└── README.md              # 项目主文档
```

## 🚀 部署步骤

### 步骤 1: 启动后端服务

MCP 服务依赖 Java 后端，首先需要启动后端：

```bash
# 进入项目根目录
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback

# 如果还没有编译，先编译
mvn clean package -DskipTests

# 启动后端服务
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

确认后端启动成功（看到 "Tomcat started on port 8088"）

### 步骤 2: 安装 MCP 服务依赖

```bash
# 进入 MCP 服务目录
cd mcp-server

# 安装依赖
npm install
```

### 步骤 3: 构建 MCP 服务

```bash
# 构建 TypeScript 代码
npm run build
```

构建成功后会在 `dist/` 目录生成可执行文件。

### 步骤 4: 本地测试

```bash
# 开发模式测试
npm run dev
```

### 步骤 5: 配置 Claude Desktop

#### Windows 配置

1. 打开配置文件：`%APPDATA%\Claude\claude_desktop_config.json`

2. 添加 MCP 服务配置：

```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "node",
      "args": [
        "c:\\Users\\Lenovo\\Desktop\\n8n\\back3\\baziback\\mcp-server\\dist\\index.js"
      ],
      "env": {
        "YIJING_BACKEND_URL": "http://localhost:8088"
      }
    }
  }
}
```

#### macOS/Linux 配置

1. 打开配置文件：`~/Library/Application Support/Claude/claude_desktop_config.json`

2. 添加配置：

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

### 步骤 6: 重启 Claude Desktop

关闭并重新打开 Claude Desktop，MCP 服务将自动加载。

## ✅ 验证部署

在 Claude Desktop 中测试：

```
帮我占卜一下今天的运势
```

如果 Claude 能够调用易经占卜工具并返回结果，说明部署成功！

## 📝 可用的 MCP 工具

部署成功后，Claude 可以使用以下工具：

1. **yijing_generate_hexagram** - 生成卦象
2. **yijing_interpret_hexagram** - 解读卦象
3. **yijing_get_hexagram** - 获取卦象详情
4. **yijing_list_hexagrams** - 列出所有卦象
5. **yijing_quick_divination** - 快速占卜（推荐）

## 🌐 发布到 NPM（可选）

如果想让其他人也能使用您的 MCP 服务：

### 1. 登录 NPM

```bash
npm login
```

### 2. 发布包

```bash
cd mcp-server
npm publish --access public
```

### 3. 其他人安装

```bash
npm install -g @lldd/yijing-divination-mcp-server
```

### 4. Claude Desktop 配置（使用全局安装）

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

## 🔧 高级配置

### 使用不同的后端地址

如果后端部署在其他服务器：

```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "yijing-mcp",
      "env": {
        "YIJING_BACKEND_URL": "http://your-server:8088"
      }
    }
  }
}
```

### 修改后端端口

如果需要修改后端端口，编辑 `application.yml`：

```yaml
server:
  port: 9999  # 改为其他端口
```

然后更新 MCP 配置中的 `YIJING_BACKEND_URL`。

## 🐛 故障排除

### 问题 1: Claude Desktop 找不到 MCP 服务

**解决方案**:
1. 检查配置文件路径是否正确
2. 确保 JSON 格式正确（使用 JSON 验证器）
3. 重启 Claude Desktop

### 问题 2: MCP 服务无法连接后端

**解决方案**:
1. 确认后端服务已启动
2. 检查端口是否正确（默认 8088）
3. 检查防火墙设置

### 问题 3: 构建失败

**解决方案**:
```bash
# 清理并重新安装
cd mcp-server
rm -rf node_modules package-lock.json
npm install
npm run build
```

### 问题 4: 权限错误（Linux/Mac）

**解决方案**:
```bash
chmod +x dist/index.js
```

## 📊 性能优化

### 1. 后端服务优化

在 `application.yml` 中调整：

```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

### 2. 启用缓存

后端已配置 Caffeine 缓存，无需额外配置。

## 🔒 安全建议

1. **不要在公网暴露后端服务**，除非配置了适当的安全措施
2. **使用环境变量**存储敏感配置
3. **定期更新依赖**：`npm update`

## 📚 相关资源

- [Model Context Protocol 文档](https://modelcontextprotocol.io/)
- [Claude Desktop 下载](https://claude.ai/desktop)
- [项目主文档](./README.md)
- [MCP 服务文档](./mcp-server/README.md)

## 🎯 下一步

部署成功后，您可以：

1. ✅ 在 Claude Desktop 中使用易经占卜
2. ✅ 自定义 MCP 工具功能
3. ✅ 发布到 NPM 供他人使用
4. ✅ 集成到其他支持 MCP 的应用

---

**部署完成！** 现在您可以在 Claude Desktop 中使用易经占卜功能了！ 🎉
