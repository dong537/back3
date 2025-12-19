# 易经占卜 MCP 服务 - 部署检查清单

## ✅ 部署前检查

### 环境准备

- [ ] **Java 17+** 已安装
  ```powershell
  java -version
  # 应显示 17 或更高版本
  ```

- [ ] **Node.js 18+** 已安装
  ```powershell
  node --version
  # 应显示 v18.0.0 或更高版本
  ```

- [ ] **Maven 3.6+** 已安装
  ```powershell
  mvn --version
  ```

- [ ] **Claude Desktop** 已安装
  - 下载地址: https://claude.ai/desktop

---

## 📦 MCP 服务打包检查

### 文件完整性

确认以下文件存在：

- [ ] `mcp-server/package.json` - NPM 配置
- [ ] `mcp-server/tsconfig.json` - TypeScript 配置
- [ ] `mcp-server/src/index.ts` - 服务入口
- [ ] `mcp-server/README.md` - 服务文档
- [ ] `mcp-server/LICENSE` - 许可证
- [ ] `mcp-server/.gitignore` - Git 忽略配置
- [ ] `mcp-server/.npmignore` - NPM 忽略配置

### 构建测试

```powershell
cd mcp-server

# 安装依赖
npm install

# 构建
npm run build

# 检查构建产物
dir dist
# 应该看到 index.js, index.d.ts 等文件
```

---

## 🚀 本地部署检查

### 步骤 1: 后端服务

- [ ] 编译成功
  ```powershell
  mvn clean package -DskipTests
  ```

- [ ] JAR 文件存在
  ```powershell
  dir target\bazi-0.0.1-SNAPSHOT.jar
  ```

- [ ] 服务启动成功
  ```powershell
  java -jar target\bazi-0.0.1-SNAPSHOT.jar
  # 等待看到: Tomcat started on port 8088
  ```

- [ ] API 可访问
  ```powershell
  curl http://localhost:8088/api/yijing/hexagrams
  # 应返回 JSON 数据
  ```

### 步骤 2: MCP 服务

- [ ] 构建成功
  ```powershell
  cd mcp-server
  npm run build
  ```

- [ ] 开发模式测试
  ```powershell
  npm run dev
  # 应显示: 易经占卜 MCP 服务已启动
  ```

### 步骤 3: Claude Desktop 配置

- [ ] 配置文件路径正确
  - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
  - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`

- [ ] JSON 格式正确
  ```json
  {
    "mcpServers": {
      "yijing-divination": {
        "command": "node",
        "args": ["绝对路径/dist/index.js"],
        "env": {
          "YIJING_BACKEND_URL": "http://localhost:8088"
        }
      }
    }
  }
  ```

- [ ] 已重启 Claude Desktop

### 步骤 4: 功能验证

在 Claude Desktop 中测试：

- [ ] 快速占卜
  ```
  帮我占卜一下今天的运势
  ```

- [ ] 生成卦象
  ```
  用时间起卦方法帮我占卜事业发展
  ```

- [ ] 获取卦象信息
  ```
  告诉我第1卦的详细信息
  ```

- [ ] 列出所有卦象
  ```
  列出所有64卦
  ```

---

## 📤 NPM 发布检查

### 发布前准备

- [ ] 更新 `package.json` 中的信息
  - [ ] `name`: 包名（如 `@your-org/yijing-divination-mcp-server`）
  - [ ] `version`: 版本号
  - [ ] `repository`: 仓库地址
  - [ ] `author`: 作者信息

- [ ] 创建 `.npmrc`（如果使用私有仓库）
  ```
  registry=https://registry.npmjs.org/
  ```

- [ ] 登录 NPM
  ```powershell
  npm login
  ```

### 发布流程

- [ ] 构建项目
  ```powershell
  npm run build
  ```

- [ ] 检查打包内容
  ```powershell
  npm pack --dry-run
  # 查看将要发布的文件列表
  ```

- [ ] 发布到 NPM
  ```powershell
  npm publish --access public
  ```

- [ ] 验证发布
  ```powershell
  npm view @your-org/yijing-divination-mcp-server
  ```

---

## 👥 团队部署检查

### 方案 A: NPM 包分发

团队成员执行：

- [ ] 安装 NPM 包
  ```powershell
  npm install -g @your-org/yijing-divination-mcp-server
  ```

- [ ] 验证安装
  ```powershell
  npm list -g @your-org/yijing-divination-mcp-server
  ```

- [ ] 配置 Claude Desktop
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

### 方案 B: 共享后端服务

- [ ] 后端部署到服务器
  - [ ] 服务器 IP/域名: _______________
  - [ ] 端口: 8088
  - [ ] 防火墙已配置

- [ ] 团队成员配置
  ```json
  {
    "mcpServers": {
      "yijing-divination": {
        "command": "yijing-mcp",
        "env": {
          "YIJING_BACKEND_URL": "http://服务器地址:8088"
        }
      }
    }
  }
  ```

- [ ] 网络连通性测试
  ```powershell
  curl http://服务器地址:8088/api/yijing/hexagrams
  ```

---

## 🔒 安全检查

- [ ] **API Key 配置**
  - [ ] DeepSeek API Key 已配置（用于 AI 解读）
  - [ ] 不要将 API Key 提交到 Git

- [ ] **环境变量**
  - [ ] 敏感信息使用环境变量
  - [ ] `.env` 文件已添加到 `.gitignore`

- [ ] **网络安全**
  - [ ] 后端服务不暴露到公网（除非必要）
  - [ ] 使用 HTTPS（生产环境）
  - [ ] 配置 CORS（如需要）

---

## 📊 性能检查

- [ ] **后端性能**
  - [ ] 缓存已启用（Caffeine）
  - [ ] 线程池配置合理
  - [ ] 数据库连接池配置（如使用）

- [ ] **MCP 服务性能**
  - [ ] 响应时间 < 2秒
  - [ ] 错误处理完善
  - [ ] 日志记录适当

---

## 📝 文档检查

确认以下文档完整：

- [ ] `README.md` - 项目主文档
- [ ] `MCP_DEPLOYMENT_GUIDE.md` - 部署指南
- [ ] `TEAM_USAGE_GUIDE.md` - 团队使用指南
- [ ] `QUICK_START.md` - 快速开始
- [ ] `mcp-server/README.md` - MCP 服务文档
- [ ] `DEPLOYMENT_CHECKLIST.md` - 本检查清单

---

## 🐛 故障排查清单

如果遇到问题，按顺序检查：

### 后端问题

- [ ] 端口 8088 是否被占用？
  ```powershell
  netstat -ano | findstr :8088
  ```

- [ ] Java 版本是否正确？
  ```powershell
  java -version
  ```

- [ ] 数据库连接是否正常？（如使用）

- [ ] 日志中是否有错误？

### MCP 服务问题

- [ ] Node.js 版本是否正确？
  ```powershell
  node --version
  ```

- [ ] 依赖是否安装完整？
  ```powershell
  npm install
  ```

- [ ] 构建是否成功？
  ```powershell
  npm run build
  ```

- [ ] 后端 URL 是否正确？

### Claude Desktop 问题

- [ ] 配置文件路径是否正确？

- [ ] JSON 格式是否有效？
  - 使用 JSON 验证器检查

- [ ] 是否重启了 Claude Desktop？

- [ ] 查看 Claude Desktop 日志
  - Windows: `%APPDATA%\Claude\logs\`
  - macOS: `~/Library/Logs/Claude/`

---

## ✅ 最终验证

全部检查通过后，进行最终验证：

- [ ] 后端服务稳定运行
- [ ] MCP 服务正常响应
- [ ] Claude Desktop 可以调用所有工具
- [ ] 团队成员可以正常使用
- [ ] 文档完整且准确
- [ ] 错误处理完善
- [ ] 日志记录清晰

---

## 📞 支持信息

**项目维护者**: LLDD  
**版本**: 1.0.0  
**最后更新**: 2024-12-18

**获取帮助**:
1. 查看文档目录
2. 检查日志文件
3. 联系项目维护者

---

**部署检查完成！** 🎉
