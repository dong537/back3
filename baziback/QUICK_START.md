# 易经占卜 MCP 服务 - 快速开始

## 🚀 5分钟快速部署

### 第一步：启动后端服务

```powershell
# 进入项目目录
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback

# 编译项目（首次运行）
mvn clean package -DskipTests

# 启动后端
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

**等待看到**：`Tomcat started on port 8088`

---

### 第二步：构建 MCP 服务

**打开新的 PowerShell 窗口**：

```powershell
# 进入 MCP 服务目录
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback\mcp-server

# 安装依赖（首次运行）
npm install

# 构建
npm run build
```

---

### 第三步：配置 Claude Desktop

1. **打开配置文件**：
   ```powershell
   notepad $env:APPDATA\Claude\claude_desktop_config.json
   ```

2. **添加以下配置**：
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

3. **保存并关闭**

---

### 第四步：重启 Claude Desktop

关闭并重新打开 Claude Desktop。

---

### 第五步：测试

在 Claude Desktop 中输入：

```
帮我占卜一下今天的运势
```

如果 Claude 调用了易经占卜工具并返回结果，**部署成功！** 🎉

---

## 📦 发布为 NPM 包（可选）

如果想让团队成员更方便地使用：

### 1. 发布到 NPM

```powershell
cd mcp-server

# 登录 NPM（首次）
npm login

# 发布
npm publish --access public
```

### 2. 团队成员安装

```powershell
# 全局安装
npm install -g @lldd/yijing-divination-mcp-server
```

### 3. 简化的 Claude Desktop 配置

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

---

## 🔧 日常使用

### 启动服务

每次使用前需要启动后端：

```powershell
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

**提示**：可以创建一个批处理脚本自动启动。

### 停止服务

在运行后端的窗口按 `Ctrl+C`。

---

## 🐛 遇到问题？

### 检查清单

- [ ] 后端是否运行？访问 http://localhost:8088
- [ ] MCP 服务是否构建？检查 `mcp-server/dist/` 目录
- [ ] Claude Desktop 配置路径是否正确？
- [ ] 是否重启了 Claude Desktop？

### 查看日志

**后端日志**：在运行 `java -jar` 的窗口查看

**MCP 服务日志**：
```powershell
# 测试 MCP 服务
cd mcp-server
npm run dev
```

---

## 📚 更多文档

- [完整部署指南](MCP_DEPLOYMENT_GUIDE.md)
- [团队使用指南](TEAM_USAGE_GUIDE.md)
- [MCP 服务文档](mcp-server/README.md)
- [项目主文档](README.md)

---

**快速开始完成！** 现在可以在 Claude Desktop 中使用易经占卜功能了！ 🎊
