# 云服务器部署后配置指南

部署完成后，按照以下步骤配置和使用服务。

## ✅ 第一步：验证服务器部署

### 1. 检查后端服务状态

```bash
# SSH 登录到服务器
ssh user@your-server

# 检查服务状态
sudo systemctl status yijing-backend

# 应该看到 "active (running)"
```

### 2. 测试 API 接口

```bash
# 在服务器上测试
curl http://localhost:8088/api/yijing/hexagrams

# 应该返回 JSON 格式的卦象列表
```

### 3. 获取服务器 IP 地址

```bash
# 查看公网 IP
curl ifconfig.me

# 或查看内网 IP
hostname -I
```

**记录这个 IP 地址**，后续团队成员需要使用。

---

## 🔧 第二步：配置防火墙和安全

### 1. 开放 8088 端口

**Ubuntu/Debian (UFW)**:
```bash
sudo ufw allow 8088/tcp
sudo ufw reload
sudo ufw status
```

**CentOS/RHEL (firewalld)**:
```bash
sudo firewall-cmd --permanent --add-port=8088/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

### 2. 测试外部访问

在**本地电脑**上测试（替换为您的服务器 IP）：

```powershell
# Windows PowerShell
Invoke-RestMethod -Uri "http://your-server-ip:8088/api/yijing/hexagrams"

# 或使用 curl
curl http://your-server-ip:8088/api/yijing/hexagrams
```

如果能正常返回数据，说明配置成功！

### 3. 配置 HTTPS（可选但推荐）

如果有域名，建议配置 HTTPS：

```bash
# 安装 Nginx
sudo apt install nginx -y

# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取 SSL 证书
sudo certbot --nginx -d your-domain.com

# Nginx 会自动配置 HTTPS
```

---

## 👥 第三步：团队成员配置

### 方案 A：团队成员本地运行 MCP 服务（推荐）

每个团队成员需要：

#### 1. 获取项目代码

```powershell
# 从您的仓库克隆或从共享文件夹获取
git clone <your-repo-url>
cd baziback\mcp-server
```

#### 2. 构建 MCP 服务

```powershell
# 安装依赖
npm install

# 构建
npm run build

# 验证构建
dir dist
```

#### 3. 配置 Claude Desktop

编辑 `%APPDATA%\Claude\claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "node",
      "args": [
        "C:\\path\\to\\baziback\\mcp-server\\dist\\index.js"
      ],
      "env": {
        "YIJING_BACKEND_URL": "http://your-server-ip:8088"
      }
    }
  }
}
```

**重要**：
- 将 `C:\\path\\to\\baziback` 替换为实际路径
- 将 `your-server-ip` 替换为服务器 IP 地址
- 注意 Windows 路径使用双反斜杠 `\\`

#### 4. 重启 Claude Desktop

关闭并重新打开 Claude Desktop。

#### 5. 测试功能

在 Claude Desktop 中输入：
```
帮我占卜一下今天的运势
```

如果 Claude 能够调用易经占卜工具并返回结果，配置成功！

---

### 方案 B：发布 NPM 包供团队使用（可选）

如果您想让团队成员安装更简单：

#### 1. 发布到 NPM

```powershell
cd mcp-server

# 登录 NPM（首次）
npm login

# 发布
npm publish --access public
```

#### 2. 团队成员安装

```powershell
# 全局安装
npm install -g @lldd/yijing-divination-mcp-server
```

#### 3. 简化的配置

```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "yijing-mcp",
      "env": {
        "YIJING_BACKEND_URL": "http://your-server-ip:8088"
      }
    }
  }
}
```

---

## 📝 第四步：创建团队使用文档

为团队成员创建一个简单的使用说明：

### 创建 `TEAM_SETUP.md`

```markdown
# 易经占卜 MCP 服务使用指南

## 快速配置（5分钟）

### 1. 获取代码
从 [共享位置] 获取 `baziback` 项目文件夹

### 2. 构建 MCP 服务
打开 PowerShell，执行：
\`\`\`powershell
cd path\to\baziback\mcp-server
npm install
npm run build
\`\`\`

### 3. 配置 Claude Desktop
1. 打开文件：`%APPDATA%\Claude\claude_desktop_config.json`
2. 添加以下配置：
\`\`\`json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "node",
      "args": ["你的路径\\baziback\\mcp-server\\dist\\index.js"],
      "env": {
        "YIJING_BACKEND_URL": "http://服务器IP:8088"
      }
    }
  }
}
\`\`\`
3. 保存并重启 Claude Desktop

### 4. 测试
在 Claude Desktop 中输入：
\`\`\`
帮我占卜一下今天的运势
\`\`\`

## 服务器信息
- **后端地址**: http://服务器IP:8088
- **管理员**: [您的联系方式]
```

---

## 🔍 第五步：监控和维护

### 1. 查看服务日志

```bash
# 实时查看日志
sudo journalctl -u yijing-backend -f

# 查看最近的错误
sudo journalctl -u yijing-backend -p err -n 50
```

### 2. 设置日志轮转

```bash
# 编辑 journald 配置
sudo nano /etc/systemd/journald.conf

# 设置日志大小限制
SystemMaxUse=500M
SystemKeepFree=1G

# 重启 journald
sudo systemctl restart systemd-journald
```

### 3. 监控服务状态

创建简单的监控脚本 `/opt/check-service.sh`:

```bash
#!/bin/bash
if ! systemctl is-active --quiet yijing-backend; then
    echo "Service is down! Restarting..."
    sudo systemctl restart yijing-backend
    # 可以添加邮件通知
fi
```

添加到 crontab（每5分钟检查一次）：
```bash
crontab -e
# 添加：
*/5 * * * * /opt/check-service.sh
```

### 4. 性能优化

如果遇到性能问题，调整 JVM 参数：

```bash
sudo systemctl edit yijing-backend

# 添加：
[Service]
Environment="JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC"

# 重启服务
sudo systemctl restart yijing-backend
```

---

## 📊 第六步：配置可选功能

### 1. 配置 DeepSeek API Key（AI 解读功能）

```bash
# 编辑服务配置
sudo systemctl edit yijing-backend

# 添加环境变量
[Service]
Environment="DEEPSEEK_API_KEY=sk-your-api-key-here"

# 重启服务
sudo systemctl restart yijing-backend
```

### 2. 配置数据库（用户系统，可选）

```bash
# 安装 MySQL
sudo apt install mysql-server -y

# 创建数据库
sudo mysql -e "CREATE DATABASE bazi;"
sudo mysql -e "CREATE USER 'bazi'@'localhost' IDENTIFIED BY 'your-password';"
sudo mysql -e "GRANT ALL PRIVILEGES ON bazi.* TO 'bazi'@'localhost';"

# 配置后端连接
sudo systemctl edit yijing-backend

# 添加数据库配置
[Service]
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bazi"
Environment="SPRING_DATASOURCE_USERNAME=bazi"
Environment="SPRING_DATASOURCE_PASSWORD=your-password"

# 重启服务
sudo systemctl restart yijing-backend
```

---

## 🚨 常见问题排查

### 问题 1: 团队成员无法连接服务器

**检查清单**:
- [ ] 服务器防火墙是否开放 8088 端口
- [ ] 云服务商安全组是否允许 8088 端口
- [ ] 服务是否正常运行：`sudo systemctl status yijing-backend`
- [ ] 本地网络是否可达：`ping your-server-ip`

**解决方案**:
```bash
# 检查端口监听
sudo netstat -tlnp | grep 8088

# 检查防火墙
sudo ufw status

# 测试本地访问
curl http://localhost:8088/api/yijing/hexagrams
```

### 问题 2: 服务频繁重启

**查看日志**:
```bash
sudo journalctl -u yijing-backend -n 100
```

**常见原因**:
- 内存不足
- 端口冲突
- 配置错误

### 问题 3: API 响应慢

**优化建议**:
1. 增加 JVM 内存
2. 启用缓存（已默认启用）
3. 检查网络延迟
4. 考虑使用 CDN

---

## 📋 团队成员配置检查清单

发送给每个团队成员：

- [ ] 已获取项目代码
- [ ] 已安装 Node.js 18+
- [ ] 已构建 MCP 服务（`npm install && npm run build`）
- [ ] 已配置 Claude Desktop
- [ ] 已重启 Claude Desktop
- [ ] 测试功能正常

---

## 📞 获取支持

### 服务器管理
- **查看状态**: `sudo systemctl status yijing-backend`
- **查看日志**: `sudo journalctl -u yijing-backend -f`
- **重启服务**: `sudo systemctl restart yijing-backend`

### 团队成员支持
- 提供服务器 IP 地址
- 提供配置示例
- 协助排查连接问题

---

## ✅ 部署完成确认

确认以下所有项目都已完成：

- [ ] 后端服务运行正常
- [ ] API 可以外部访问
- [ ] 防火墙已配置
- [ ] 团队成员配置文档已准备
- [ ] 至少一个团队成员测试成功
- [ ] 监控和日志已配置
- [ ] 备份策略已制定

---

**恭喜！** 您的易经占卜 MCP 服务已成功部署到云服务器，团队成员现在可以使用了！ 🎉

**下一步**: 将服务器 IP 和配置说明分享给团队成员。
