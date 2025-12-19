# 部署命令 - 服务器 8.130.42.57

## 📤 第一步：上传文件到服务器

在 Windows PowerShell 中执行以下命令：

### 1. 上传 MCP 服务源码

```powershell
scp c:\Users\Lenovo\Desktop\n8n\back3\baziback\mcp-server-source.zip root@8.130.42.57:/tmp/
```

### 2. 编译并上传后端 JAR

```powershell
# 编译后端（如果还没有编译）
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
mvn clean package -DskipTests

# 上传 JAR 文件
scp target\bazi-0.0.1-SNAPSHOT.jar root@8.130.42.57:/tmp/
```

---

## 🖥️ 第二步：SSH 登录服务器

```powershell
ssh root@8.130.42.57
```

---

## 🚀 第三步：在服务器上执行部署

登录服务器后，复制粘贴以下命令：

### 分步部署命令（逐步复制执行，避免断开连接）

> ⚠️ **重要**: 请逐步复制执行以下命令，不要一次性粘贴全部！

---

#### 步骤 1: 检查文件

```bash
ls -la /tmp/*.jar /tmp/*.zip 2>/dev/null || echo "文件不在 /tmp/"
ls -la /var/tmp/*.jar /var/tmp/*.zip 2>/dev/null || echo "文件不在 /var/tmp/"
```

如果文件在 `/var/tmp/`，执行：
```bash
cp /var/tmp/bazi-0.0.1-SNAPSHOT.jar /tmp/
cp /var/tmp/mcp-server-source.zip /tmp/
```

---

#### 步骤 2: 部署 MCP 服务

先查看 zip 文件结构：
```bash
unzip -l /tmp/mcp-server-source.zip | head -20
```

解压文件（根据结构选择一个）：
```bash
# 方式A: 如果 zip 内没有 mcp-server 目录前缀
rm -rf /opt/mcp-server && mkdir -p /opt/mcp-server && unzip -o /tmp/mcp-server-source.zip -d /opt/mcp-server

# 方式B: 如果 zip 内有 mcp-server 目录前缀
rm -rf /opt/mcp-server && unzip -o /tmp/mcp-server-source.zip -d /opt
```

确认文件存在：
```bash
ls -la /opt/mcp-server/
cat /opt/mcp-server/package.json
```

安装依赖并构建：
```bash
cd /opt/mcp-server && npm install && npm run build
```

验证构建成功：
```bash
ls -la /opt/mcp-server/dist/index.js
```

---

#### 步骤 3: 部署后端 JAR

```bash
mkdir -p /opt/yijing-backend && cp /tmp/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/
```

---

#### 步骤 4: 创建 systemd 服务

```bash
cat > /etc/systemd/system/yijing-backend.service <<'EOF'
[Unit]
Description=Yijing Backend Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/yijing-backend
ExecStart=/usr/bin/java -jar /opt/yijing-backend/bazi-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
```

---

#### 步骤 5: 启动服务

```bash
systemctl daemon-reload && systemctl enable yijing-backend && systemctl start yijing-backend
```

---

#### 步骤 6: 验证部署

```bash
sleep 5 && systemctl status yijing-backend
```

```bash
curl http://localhost:8088/api/yijing/hexagrams
```

---

### 常用管理命令

```bash
# 查看状态
systemctl status yijing-backend

# 查看日志
journalctl -u yijing-backend -f

# 重启服务
systemctl restart yijing-backend
```

---

## ✅ 第四步：验证部署

### 在服务器上测试

```bash
# 检查服务状态
systemctl status yijing-backend

# 测试 API
curl http://localhost:8088/api/yijing/hexagrams

# 查看日志
journalctl -u yijing-backend -f
```

### 在本地 Windows 测试

```powershell
# 测试 API 访问
Invoke-RestMethod -Uri "http://8.130.42.57:8088/api/yijing/hexagrams"

# 或使用 curl
curl http://8.130.42.57:8088/api/yijing/hexagrams
```

如果返回 JSON 数据，说明部署成功！

---

## 👥 第五步：团队成员配置

### 配置信息分享给团队

**服务器地址**: `http://8.130.42.57:8088`

### 团队成员配置步骤

1. **获取项目代码**（从您这里获取或 Git 克隆）

2. **构建本地 MCP 服务**：
```powershell
cd path\to\baziback\mcp-server
npm install
npm run build
```

3. **配置 Claude Desktop**：

编辑 `%APPDATA%\Claude\claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "node",
      "args": [
        "C:\\完整路径\\baziback\\mcp-server\\dist\\index.js"
      ],
      "env": {
        "YIJING_BACKEND_URL": "http://8.130.42.57:8088"
      }
    }
  }
}
```

4. **重启 Claude Desktop**

5. **测试**：
```
帮我占卜一下今天的运势
```

---

## 🔧 常用管理命令

### 服务管理

```bash
# SSH 登录
ssh root@8.130.42.57

# 查看服务状态
systemctl status yijing-backend

# 启动服务
systemctl start yijing-backend

# 停止服务
systemctl stop yijing-backend

# 重启服务
systemctl restart yijing-backend

# 查看实时日志
journalctl -u yijing-backend -f

# 查看最近 100 行日志
journalctl -u yijing-backend -n 100
```

### 更新部署

```powershell
# 在 Windows 上传新版本
scp target\bazi-0.0.1-SNAPSHOT.jar root@8.130.42.57:/tmp/new-bazi.jar

# SSH 登录服务器
ssh root@8.130.42.57

# 备份并替换
cp /opt/yijing-backend/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/bazi-0.0.1-SNAPSHOT.jar.bak
cp /tmp/new-bazi.jar /opt/yijing-backend/bazi-0.0.1-SNAPSHOT.jar

# 重启服务
systemctl restart yijing-backend
```

---

## 📋 部署检查清单

- [ ] mcp-server-source.zip 已上传
- [ ] bazi-0.0.1-SNAPSHOT.jar 已上传
- [ ] 部署脚本已执行
- [ ] 服务运行正常（`systemctl status yijing-backend`）
- [ ] API 可访问（`curl http://localhost:8088/api/yijing/hexagrams`）
- [ ] 防火墙已配置
- [ ] 本地可以访问（`curl http://8.130.42.57:8088/api/yijing/hexagrams`）
- [ ] 团队成员配置文档已准备

---

## 🎯 快速命令汇总

### Windows 本地执行

```powershell
# 上传文件
scp c:\Users\Lenovo\Desktop\n8n\back3\baziback\mcp-server-source.zip root@8.130.42.57:/tmp/
scp c:\Users\Lenovo\Desktop\n8n\back3\baziback\target\bazi-0.0.1-SNAPSHOT.jar root@8.130.42.57:/tmp/

# SSH 登录
ssh root@8.130.42.57
```

### 服务器执行

```bash
# 一键部署（复制上面的完整脚本）
```

### 验证

```powershell
# 本地测试
Invoke-RestMethod -Uri "http://8.130.42.57:8088/api/yijing/hexagrams"
```

---

**准备就绪！** 现在可以开始部署了！
