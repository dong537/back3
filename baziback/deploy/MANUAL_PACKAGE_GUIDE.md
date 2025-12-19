# 手动打包 MCP 服务到云服务器

## 📦 方法一：直接上传源码到服务器构建（推荐）

这是最简单可靠的方法，在服务器上直接构建。

### 步骤 1: 上传源码

```bash
# 压缩整个 mcp-server 目录
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
tar -czf mcp-server-source.tar.gz mcp-server/

# 上传到服务器
scp mcp-server-source.tar.gz user@your-server:/tmp/
```

### 步骤 2: 在服务器上构建

SSH 登录到服务器后：

```bash
# 解压
cd /opt
sudo tar -xzf /tmp/mcp-server-source.tar.gz
sudo chown -R $USER:$USER mcp-server

# 进入目录
cd mcp-server

# 安装依赖并构建
npm install
npm run build

# 验证构建
ls -la dist/
```

---

## 📦 方法二：上传后端 JAR 到服务器

### 步骤 1: 编译后端

```powershell
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
mvn clean package -DskipTests
```

### 步骤 2: 上传 JAR

```bash
scp target/bazi-0.0.1-SNAPSHOT.jar user@your-server:/tmp/
```

### 步骤 3: 在服务器上部署后端

```bash
# SSH 登录
ssh user@your-server

# 创建目录
sudo mkdir -p /opt/yijing-backend
sudo chown -R $USER:$USER /opt/yijing-backend

# 复制 JAR
cp /tmp/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/

# 创建 systemd 服务
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

# 检查状态
sudo systemctl status yijing-backend
```

### 步骤 4: 配置防火墙

```bash
# Ubuntu/Debian
sudo ufw allow 8088/tcp
sudo ufw reload

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=8088/tcp
sudo firewall-cmd --reload
```

### 步骤 5: 验证部署

```bash
# 测试 API
curl http://localhost:8088/api/yijing/hexagrams

# 查看日志
sudo journalctl -u yijing-backend -f
```

---

## 👥 团队成员配置

部署完成后，团队成员在本地配置 Claude Desktop：

### Windows 用户

编辑 `%APPDATA%\Claude\claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "yijing-divination": {
      "command": "node",
      "args": [
        "c:\\Users\\Lenovo\\Desktop\\n8n\\back3\\baziback\\mcp-server\\dist\\index.js"
      ],
      "env": {
        "YIJING_BACKEND_URL": "http://your-server-ip:8088"
      }
    }
  }
}
```

**注意**：
- 将 `your-server-ip` 替换为您的云服务器 IP 地址
- 如果配置了域名，可以使用域名代替 IP
- 本地也需要先构建 MCP 服务（在 mcp-server 目录运行 `npm install && npm run build`）

### 如果已发布到 NPM

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

## 🔧 完整部署脚本（一键部署）

将以下内容保存为 `deploy-backend.sh` 并上传到服务器：

```bash
#!/bin/bash
set -e

echo "========================================="
echo "易经占卜后端服务一键部署"
echo "========================================="
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "安装 Java 17..."
    sudo apt update
    sudo apt install openjdk-17-jdk -y
fi

echo "Java 版本: $(java -version 2>&1 | head -n 1)"
echo ""

# 创建目录
echo "创建部署目录..."
sudo mkdir -p /opt/yijing-backend
sudo chown -R $USER:$USER /opt/yijing-backend

# 复制 JAR
if [ -f "/tmp/bazi-0.0.1-SNAPSHOT.jar" ]; then
    echo "复制 JAR 文件..."
    cp /tmp/bazi-0.0.1-SNAPSHOT.jar /opt/yijing-backend/
else
    echo "错误: 未找到 JAR 文件，请先上传到 /tmp/"
    exit 1
fi

# 创建 systemd 服务
echo "配置系统服务..."
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
echo "启动服务..."
sudo systemctl daemon-reload
sudo systemctl enable yijing-backend
sudo systemctl start yijing-backend

# 等待启动
echo "等待服务启动..."
sleep 5

# 检查状态
if sudo systemctl is-active --quiet yijing-backend; then
    echo ""
    echo "========================================="
    echo "部署成功！"
    echo "========================================="
    echo ""
    echo "服务状态: sudo systemctl status yijing-backend"
    echo "查看日志: sudo journalctl -u yijing-backend -f"
    echo "API 地址: http://$(hostname -I | awk '{print $1}'):8088"
    echo ""
    echo "测试 API: curl http://localhost:8088/api/yijing/hexagrams"
    echo ""
    echo "记得配置防火墙: sudo ufw allow 8088/tcp"
else
    echo ""
    echo "服务启动失败，请查看日志:"
    echo "sudo journalctl -u yijing-backend -n 50"
fi
```

使用方法：

```bash
# 1. 上传 JAR 和脚本
scp target/bazi-0.0.1-SNAPSHOT.jar user@server:/tmp/
scp deploy-backend.sh user@server:/tmp/

# 2. SSH 登录并执行
ssh user@server
chmod +x /tmp/deploy-backend.sh
bash /tmp/deploy-backend.sh
```

---

## 📋 快速命令参考

### 上传文件

```bash
# 上传后端 JAR
scp target/bazi-0.0.1-SNAPSHOT.jar user@server:/tmp/

# 上传 MCP 服务源码
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
tar -czf mcp-server.tar.gz mcp-server/
scp mcp-server.tar.gz user@server:/tmp/
```

### 服务管理

```bash
# 启动
sudo systemctl start yijing-backend

# 停止
sudo systemctl stop yijing-backend

# 重启
sudo systemctl restart yijing-backend

# 状态
sudo systemctl status yijing-backend

# 日志
sudo journalctl -u yijing-backend -f
```

### 测试 API

```bash
# 健康检查
curl http://localhost:8088/api/yijing/hexagrams

# 生成卦象
curl -X POST http://localhost:8088/api/yijing/hexagram/generate \
  -H "Content-Type: application/json" \
  -d '{"question":"测试","method":"time"}'
```

---

## ✅ 部署完成后

1. **记录服务器 IP**：`your-server-ip`
2. **配置防火墙**：开放 8088 端口
3. **通知团队成员**：提供服务器地址
4. **团队成员配置**：修改 Claude Desktop 配置指向服务器

---

**需要帮助？** 查看 `CLOUD_DEPLOYMENT_GUIDE.md` 获取详细说明。
