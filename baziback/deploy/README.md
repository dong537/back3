# 云服务器部署文件夹

本目录包含将易经占卜 MCP 服务部署到云服务器所需的所有脚本和文档。

## 📁 文件说明

- **`MANUAL_PACKAGE_GUIDE.md`** - 手动打包和部署指南（推荐阅读）
- **`deploy-to-server.sh`** - 服务器端自动部署脚本
- **`package.bat`** - Windows 打包脚本
- **`package-for-server.ps1`** - PowerShell 打包脚本

## 🚀 快速开始

### 方法一：直接上传源码到服务器（最简单）

```bash
# 1. 压缩 MCP 服务源码
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
tar -czf mcp-server-source.tar.gz mcp-server/

# 2. 上传到服务器
scp mcp-server-source.tar.gz user@your-server:/tmp/

# 3. SSH 登录服务器
ssh user@your-server

# 4. 在服务器上构建
cd /opt
sudo tar -xzf /tmp/mcp-server-source.tar.gz
sudo chown -R $USER:$USER mcp-server
cd mcp-server
npm install
npm run build
```

### 方法二：部署后端到服务器

```bash
# 1. 编译后端
cd c:\Users\Lenovo\Desktop\n8n\back3\baziback
mvn clean package -DskipTests

# 2. 上传 JAR
scp target/bazi-0.0.1-SNAPSHOT.jar user@your-server:/tmp/

# 3. 上传部署脚本
scp deploy/deploy-to-server.sh user@your-server:/tmp/

# 4. SSH 登录并执行
ssh user@your-server
chmod +x /tmp/deploy-to-server.sh
sudo bash /tmp/deploy-to-server.sh
```

## 📚 详细文档

- **完整部署指南**: `../CLOUD_DEPLOYMENT_GUIDE.md`
- **手动打包指南**: `MANUAL_PACKAGE_GUIDE.md`
- **团队使用指南**: `../TEAM_USAGE_GUIDE.md`

## ✅ 部署检查清单

- [ ] 云服务器已准备（Ubuntu 20.04+ / CentOS 8+）
- [ ] Java 17+ 已安装
- [ ] 后端 JAR 已编译
- [ ] 文件已上传到服务器
- [ ] 部署脚本已执行
- [ ] 服务运行正常
- [ ] 防火墙已配置（开放 8088 端口）
- [ ] 团队成员可访问

## 🔧 常用命令

### 服务管理
```bash
sudo systemctl start yijing-backend    # 启动
sudo systemctl stop yijing-backend     # 停止
sudo systemctl restart yijing-backend  # 重启
sudo systemctl status yijing-backend   # 状态
```

### 查看日志
```bash
sudo journalctl -u yijing-backend -f   # 实时日志
sudo journalctl -u yijing-backend -n 100  # 最近100行
```

### 测试 API
```bash
curl http://localhost:8088/api/yijing/hexagrams
```

## 📞 获取帮助

查看详细文档或检查服务器日志以排查问题。
