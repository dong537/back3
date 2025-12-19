# Windows 系统打包上传指南

## ✅ 已完成：MCP 服务源码打包

您的 MCP 服务源码已成功打包为：
- **文件名**: `mcp-server-source.zip`
- **位置**: `c:\Users\Lenovo\Desktop\n8n\back3\baziback\mcp-server-source.zip`

---

## 📤 上传到云服务器

### 方法一：使用 SCP（推荐）

```powershell
# 上传 MCP 服务源码
scp c:\Users\Lenovo\Desktop\n8n\back3\baziback\mcp-server-source.zip user@your-server:/tmp/

# 如果还没有编译后端，先编译
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
mvn clean package -DskipTests

# 上传后端 JAR
scp target\bazi-0.0.1-SNAPSHOT.jar user@your-server:/tmp/
```

**替换说明**：
- `user` - 您的服务器用户名
- `your-server` - 您的服务器 IP 地址或域名

### 方法二：使用 WinSCP（图形界面）

1. 下载并安装 [WinSCP](https://winscp.net/)
2. 连接到您的服务器
3. 上传以下文件到 `/tmp/` 目录：
   - `mcp-server-source.zip`
   - `bazi-0.0.1-SNAPSHOT.jar`（如果已编译）

### 方法三：使用 SFTP

```powershell
# 打开 SFTP 连接
sftp user@your-server

# 上传文件
put c:\Users\Lenovo\Desktop\n8n\back3\baziback\mcp-server-source.zip /tmp/
put c:\Users\Lenovo\Desktop\n8n\back3\baziback\target\bazi-0.0.1-SNAPSHOT.jar /tmp/

# 退出
quit
```

---

## 🖥️ 在服务器上部署

### 步骤 1: SSH 登录服务器

```bash
ssh user@your-server
```

### 步骤 2: 解压并构建 MCP 服务

```bash
# 安装 unzip（如果没有）
sudo apt install unzip -y

# 解压到 /opt 目录
cd /opt
sudo unzip /tmp/mcp-server-source.zip
sudo chown -R $USER:$USER mcp-server

# 进入目录
cd mcp-server

# 安装依赖
npm install

# 构建
npm run build

# 验证构建结果
ls -la dist/
# 应该看到 index.js 等文件
```

### 步骤 3: 部署后端服务

```bash
# 创建后端目录
sudo mkdir -p /opt/yijing-backend
sudo chown -R $USER:$USER /opt/yijing-backend

# 复制 JAR 文件
cp /tmp/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/

# 创建 systemd 服务
sudo tee /etc/systemd/system/yijing-backend.service > /dev/null <<'EOF'
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

# 注意：需要手动替换 $USER
sudo sed -i "s/\$USER/$USER/g" /etc/systemd/system/yijing-backend.service

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable yijing-backend
sudo systemctl start yijing-backend

# 检查状态
sudo systemctl status yijing-backend
```

### 步骤 4: 验证部署

```bash
# 等待几秒让服务启动
sleep 5

# 测试 API
curl http://localhost:8088/api/yijing/hexagrams

# 如果返回 JSON 数据，说明部署成功！
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

### 步骤 6: 测试外部访问

在**本地 Windows 电脑**上测试：

```powershell
# 获取服务器 IP（在服务器上执行）
curl ifconfig.me

# 在本地测试（替换为实际 IP）
Invoke-RestMethod -Uri "http://your-server-ip:8088/api/yijing/hexagrams"
```

---

## 🎯 一键部署脚本（推荐）

将以下内容保存为 `deploy.sh` 并上传到服务器：

```bash
#!/bin/bash
set -e

echo "========================================="
echo "易经占卜服务一键部署脚本"
echo "========================================="
echo ""

# 检查文件
if [ ! -f "/tmp/mcp-server-source.zip" ]; then
    echo "错误: 未找到 mcp-server-source.zip"
    echo "请先上传文件到 /tmp/"
    exit 1
fi

if [ ! -f "/tmp/bazi-0.0.1-SNAPSHOT.jar" ]; then
    echo "错误: 未找到 bazi-0.0.1-SNAPSHOT.jar"
    echo "请先上传文件到 /tmp/"
    exit 1
fi

# 安装依赖
echo "[1/5] 安装系统依赖..."
sudo apt update
sudo apt install -y openjdk-17-jdk nodejs npm unzip

# 部署 MCP 服务
echo "[2/5] 部署 MCP 服务..."
sudo mkdir -p /opt/mcp-server
cd /opt
sudo unzip -o /tmp/mcp-server-source.zip
sudo chown -R $USER:$USER mcp-server
cd mcp-server
npm install
npm run build

# 部署后端
echo "[3/5] 部署后端服务..."
sudo mkdir -p /opt/yijing-backend
sudo chown -R $USER:$USER /opt/yijing-backend
cp /tmp/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/

# 创建服务
echo "[4/5] 配置系统服务..."
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

sudo systemctl daemon-reload
sudo systemctl enable yijing-backend
sudo systemctl start yijing-backend

# 配置防火墙
echo "[5/5] 配置防火墙..."
sudo ufw allow 8088/tcp 2>/dev/null || true

# 验证
echo ""
echo "等待服务启动..."
sleep 5

if curl -s http://localhost:8088/api/yijing/hexagrams > /dev/null; then
    echo ""
    echo "========================================="
    echo "部署成功！"
    echo "========================================="
    echo ""
    echo "服务器 IP: $(curl -s ifconfig.me)"
    echo "API 地址: http://$(curl -s ifconfig.me):8088"
    echo ""
    echo "查看状态: sudo systemctl status yijing-backend"
    echo "查看日志: sudo journalctl -u yijing-backend -f"
else
    echo ""
    echo "警告: 服务可能未正常启动"
    echo "请检查日志: sudo journalctl -u yijing-backend -n 50"
fi
```

使用方法：

```powershell
# 1. 在 Windows 上传文件
scp mcp-server-source.zip user@server:/tmp/
scp target\bazi-0.0.1-SNAPSHOT.jar user@server:/tmp/
scp deploy\deploy.sh user@server:/tmp/

# 2. SSH 登录并执行
ssh user@server
chmod +x /tmp/deploy.sh
bash /tmp/deploy.sh
```

---

## 📋 完整部署流程总结

### 在 Windows 本地执行：

```powershell
# 1. 打包 MCP 服务（已完成）
# 文件：mcp-server-source.zip

# 2. 编译后端（如果还没有）
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
mvn clean package -DskipTests

# 3. 上传到服务器
scp mcp-server-source.zip user@your-server:/tmp/
scp target\bazi-0.0.1-SNAPSHOT.jar user@your-server:/tmp/
```

### 在服务器上执行：

```bash
# 方法 A: 使用一键脚本（推荐）
bash /tmp/deploy.sh

# 方法 B: 手动部署
# 按照上面"在服务器上部署"的步骤执行
```

---

## 👥 团队成员配置

部署完成后，通知团队成员：

### 配置信息
- **服务器地址**: `http://your-server-ip:8088`
- **项目代码**: 从共享位置获取或 Git 克隆

### 配置步骤

1. **构建本地 MCP 服务**：
```powershell
cd path\to\baziback\mcp-server
npm install
npm run build
```

2. **配置 Claude Desktop**：

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
        "YIJING_BACKEND_URL": "http://服务器IP:8088"
      }
    }
  }
}
```

3. **重启 Claude Desktop 并测试**

---

## ✅ 验证清单

- [ ] mcp-server-source.zip 已创建
- [ ] 后端 JAR 已编译
- [ ] 文件已上传到服务器
- [ ] 服务器上已解压并构建
- [ ] 后端服务运行正常
- [ ] API 可以访问
- [ ] 防火墙已配置
- [ ] 团队成员配置文档已准备

---

**下一步**: 上传文件到服务器并执行部署脚本！
