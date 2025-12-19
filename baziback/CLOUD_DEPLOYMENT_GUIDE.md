# 易经占卜 MCP 服务 - 云服务器部署指南

## 📋 概述

本指南将帮助您将易经占卜 MCP 服务部署到云服务器，供团队成员远程使用。

### 部署架构

```
┌─────────────────┐
│ 团队成员电脑     │
│ Claude Desktop  │
└────────┬────────┘
         │ MCP 协议
         ▼
┌─────────────────┐
│ 云服务器         │
│ ├─ MCP 服务     │ (可选)
│ └─ 后端服务     │ (必须)
└─────────────────┘
```

**推荐部署方式**：
- ✅ 云服务器部署后端服务（Spring Boot）
- ✅ 团队成员本地运行 MCP 服务
- ✅ MCP 服务连接到云服务器后端

---

## 🚀 快速部署

### 步骤 1: 打包服务

在本地 Windows 电脑上运行：

```powershell
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback\deploy
.\package-for-server.ps1
```

这将生成：
- `yijing-mcp-server-YYYYMMDD-HHmmss.tar.gz` - MCP 服务包
- 需要手动准备：`bazi-0.0.1-SNAPSHOT.jar` - 后端服务

### 步骤 2: 上传到服务器

```bash
# 上传 MCP 服务包
scp deploy/yijing-mcp-server-*.tar.gz user@your-server:/tmp/

# 上传后端 JAR
scp target/bazi-0.0.1-SNAPSHOT.jar user@your-server:/tmp/

# 上传部署脚本
scp deploy/deploy-to-server.sh user@your-server:/tmp/
```

### 步骤 3: 在服务器上部署

SSH 登录到服务器：

```bash
ssh user@your-server
cd /tmp
chmod +x deploy-to-server.sh
sudo bash deploy-to-server.sh
```

### 步骤 4: 验证部署

```bash
# 检查后端服务状态
sudo systemctl status yijing-backend

# 测试 API
curl http://localhost:8088/api/yijing/hexagrams
```

### 步骤 5: 配置防火墙

```bash
# Ubuntu/Debian
sudo ufw allow 8088/tcp
sudo ufw reload

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=8088/tcp
sudo firewall-cmd --reload
```

---

## 📦 详细部署步骤

### 一、准备工作

#### 1.1 服务器要求

- **操作系统**: Ubuntu 20.04+ / CentOS 8+ / Debian 11+
- **CPU**: 2核+
- **内存**: 2GB+
- **磁盘**: 10GB+
- **网络**: 公网 IP 或域名

#### 1.2 安装依赖

**Ubuntu/Debian**:
```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Java 17
sudo apt install openjdk-17-jdk -y

# 安装 Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# 验证安装
java -version
node --version
npm --version
```

**CentOS/RHEL**:
```bash
# 安装 Java 17
sudo yum install java-17-openjdk-devel -y

# 安装 Node.js 18
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install nodejs -y
```

---

### 二、打包本地服务

#### 2.1 编译后端

```powershell
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
mvn clean package -DskipTests
```

生成文件：`target/bazi-0.0.1-SNAPSHOT.jar`

#### 2.2 打包 MCP 服务

```powershell
cd deploy
.\package-for-server.ps1
```

生成文件：`deploy/yijing-mcp-server-YYYYMMDD-HHmmss.tar.gz`

---

### 三、上传到服务器

#### 3.1 使用 SCP 上传

```bash
# 设置变量（替换为您的服务器信息）
SERVER_USER="your-username"
SERVER_IP="your-server-ip"

# 上传后端 JAR
scp target/bazi-0.0.1-SNAPSHOT.jar $SERVER_USER@$SERVER_IP:/tmp/

# 上传 MCP 服务包
scp deploy/yijing-mcp-server-*.tar.gz $SERVER_USER@$SERVER_IP:/tmp/

# 上传部署脚本
scp deploy/deploy-to-server.sh $SERVER_USER@$SERVER_IP:/tmp/
```

#### 3.2 使用 SFTP 上传

```bash
sftp user@your-server
put target/bazi-0.0.1-SNAPSHOT.jar /tmp/
put deploy/yijing-mcp-server-*.tar.gz /tmp/
put deploy/deploy-to-server.sh /tmp/
quit
```

---

### 四、服务器部署

#### 4.1 自动部署（推荐）

```bash
# SSH 登录
ssh user@your-server

# 进入临时目录
cd /tmp

# 赋予执行权限
chmod +x deploy-to-server.sh

# 运行部署脚本
sudo bash deploy-to-server.sh
```

#### 4.2 手动部署

如果自动部署脚本失败，可以手动部署：

```bash
# 创建目录
sudo mkdir -p /opt/yijing-backend
sudo mkdir -p /opt/yijing-mcp-server
sudo chown -R $USER:$USER /opt/yijing-backend
sudo chown -R $USER:$USER /opt/yijing-mcp-server

# 部署后端
cp /tmp/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/

# 部署 MCP 服务
cd /opt/yijing-mcp-server
tar -xzf /tmp/yijing-mcp-server-*.tar.gz
npm install --production

# 创建后端服务
sudo tee /etc/systemd/system/yijing-backend.service > /dev/null <<EOF
[Unit]
Description=Yijing Backend Service
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=/opt/yijing-backend
ExecStart=/usr/bin/java -jar /opt/yijing-backend/bazi-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable yijing-backend
sudo systemctl start yijing-backend
```

---

### 五、配置与验证

#### 5.1 检查服务状态

```bash
# 查看后端服务状态
sudo systemctl status yijing-backend

# 查看日志
sudo journalctl -u yijing-backend -f
```

#### 5.2 测试 API

```bash
# 测试健康检查
curl http://localhost:8088/api/yijing/hexagrams

# 测试生成卦象
curl -X POST http://localhost:8088/api/yijing/hexagram/generate \
  -H "Content-Type: application/json" \
  -d '{"question":"测试","method":"time"}'
```

#### 5.3 配置防火墙

```bash
# Ubuntu/Debian (UFW)
sudo ufw allow 8088/tcp
sudo ufw status

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=8088/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

#### 5.4 配置 Nginx 反向代理（可选）

```bash
# 安装 Nginx
sudo apt install nginx -y

# 创建配置
sudo tee /etc/nginx/sites-available/yijing > /dev/null <<EOF
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8088;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# 启用配置
sudo ln -s /etc/nginx/sites-available/yijing /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## 👥 团队成员配置

### 方案一：本地 MCP + 云端后端（推荐）

团队成员在本地安装 MCP 服务，连接到云服务器后端。

#### 1. 安装 MCP 服务

```powershell
# 全局安装（如已发布到 NPM）
npm install -g @lldd/yijing-divination-mcp-server

# 或使用本地构建版本
```

#### 2. 配置 Claude Desktop

编辑 `%APPDATA%\Claude\claude_desktop_config.json`:

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

**如果配置了域名**：
```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "yijing-mcp",
      "env": {
        "YIJING_BACKEND_URL": "http://your-domain.com"
      }
    }
  }
}
```

#### 3. 重启 Claude Desktop

---

### 方案二：完全云端部署

在云服务器上同时运行 MCP 服务和后端服务。

**注意**：MCP 服务通常运行在客户端，此方案仅适用于特殊场景。

---

## 🔧 高级配置

### 配置 DeepSeek API Key

编辑 `/opt/yijing-backend/application.yml`:

```yaml
deepseek:
  api:
    key: sk-your-api-key-here
    endpoint: https://api.deepseek.com/v1/chat/completions
```

或使用环境变量：

```bash
# 编辑服务文件
sudo systemctl edit yijing-backend

# 添加环境变量
[Service]
Environment="DEEPSEEK_API_KEY=sk-your-api-key-here"

# 重启服务
sudo systemctl restart yijing-backend
```

### 配置 HTTPS（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

### 配置数据库（可选）

如果需要用户系统功能：

```bash
# 安装 MySQL
sudo apt install mysql-server -y

# 创建数据库
sudo mysql -e "CREATE DATABASE bazi;"
sudo mysql -e "CREATE USER 'bazi'@'localhost' IDENTIFIED BY 'your-password';"
sudo mysql -e "GRANT ALL PRIVILEGES ON bazi.* TO 'bazi'@'localhost';"

# 配置后端连接
# 编辑 application.yml 或使用环境变量
```

---

## 📊 监控与维护

### 查看日志

```bash
# 实时查看后端日志
sudo journalctl -u yijing-backend -f

# 查看最近 100 行
sudo journalctl -u yijing-backend -n 100

# 查看特定时间范围
sudo journalctl -u yijing-backend --since "1 hour ago"
```

### 服务管理

```bash
# 启动服务
sudo systemctl start yijing-backend

# 停止服务
sudo systemctl stop yijing-backend

# 重启服务
sudo systemctl restart yijing-backend

# 查看状态
sudo systemctl status yijing-backend

# 开机自启
sudo systemctl enable yijing-backend
```

### 更新部署

```bash
# 1. 上传新版本
scp target/bazi-0.0.1-SNAPSHOT.jar user@server:/tmp/

# 2. SSH 登录
ssh user@server

# 3. 备份旧版本
sudo cp /opt/yijing-backend/bazi-0.0.1-SNAPSHOT.jar \
        /opt/yijing-backend/bazi-0.0.1-SNAPSHOT.jar.bak

# 4. 替换新版本
sudo cp /tmp/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/

# 5. 重启服务
sudo systemctl restart yijing-backend

# 6. 验证
curl http://localhost:8088/api/yijing/hexagrams
```

---

## 🐛 故障排除

### 问题 1: 服务无法启动

**检查日志**:
```bash
sudo journalctl -u yijing-backend -n 50
```

**常见原因**:
- 端口 8088 被占用
- Java 版本不正确
- JAR 文件损坏

**解决方案**:
```bash
# 检查端口占用
sudo netstat -tlnp | grep 8088

# 检查 Java 版本
java -version

# 重新上传 JAR 文件
```

### 问题 2: 无法访问 API

**检查防火墙**:
```bash
sudo ufw status
sudo firewall-cmd --list-ports
```

**检查服务监听**:
```bash
sudo netstat -tlnp | grep 8088
```

**测试本地访问**:
```bash
curl http://localhost:8088/api/yijing/hexagrams
```

### 问题 3: 团队成员无法连接

**检查网络连通性**:
```bash
# 在团队成员电脑上测试
curl http://server-ip:8088/api/yijing/hexagrams
```

**检查服务器防火墙**:
```bash
sudo ufw allow from team-member-ip to any port 8088
```

### 问题 4: 性能问题

**调整 JVM 参数**:
```bash
# 编辑服务文件
sudo systemctl edit yijing-backend

# 添加 JVM 参数
[Service]
Environment="JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC"
```

**启用缓存**（已默认启用）

---

## 🔒 安全建议

1. **使用 HTTPS**
   - 配置 SSL 证书
   - 强制 HTTPS 访问

2. **限制访问**
   - 配置防火墙规则
   - 使用 VPN 或内网访问

3. **定期更新**
   - 更新系统补丁
   - 更新依赖包

4. **备份数据**
   - 定期备份配置文件
   - 备份数据库（如使用）

5. **监控日志**
   - 设置日志轮转
   - 监控异常访问

---

## 📝 部署检查清单

- [ ] 服务器环境准备完成
- [ ] Java 17+ 已安装
- [ ] Node.js 18+ 已安装
- [ ] 后端 JAR 已上传
- [ ] MCP 服务包已上传
- [ ] 部署脚本已执行
- [ ] 后端服务运行正常
- [ ] API 测试通过
- [ ] 防火墙已配置
- [ ] 团队成员可访问
- [ ] HTTPS 已配置（可选）
- [ ] 监控已设置（可选）

---

## 📞 获取帮助

**查看文档**:
- `README.md` - 项目主文档
- `TEAM_USAGE_GUIDE.md` - 团队使用指南
- `DEPLOYMENT_CHECKLIST.md` - 部署检查清单

**常见问题**:
- 检查服务日志
- 验证网络连通性
- 确认配置正确

---

**部署完成！** 现在团队成员可以通过云服务器使用易经占卜功能了！ 🎉

**版本**: 1.0.0  
**最后更新**: 2024-12-18
