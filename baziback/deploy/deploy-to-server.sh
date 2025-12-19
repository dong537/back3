#!/bin/bash
# 云服务器部署脚本 - 在服务器上运行
# 使用方法: bash deploy-to-server.sh

set -e

echo "========================================"
echo "易经占卜 MCP 服务 - 云服务器部署"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置
DEPLOY_DIR="/opt/yijing-mcp-server"
SERVICE_NAME="yijing-mcp"
BACKEND_JAR="bazi-0.0.1-SNAPSHOT.jar"
BACKEND_DIR="/opt/yijing-backend"

# 步骤 1: 检查环境
echo -e "${YELLOW}[1/6] 检查环境...${NC}"

# 检查 Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo -e "${GREEN}  ✓ Node.js: $NODE_VERSION${NC}"
else
    echo -e "${RED}  ✗ Node.js 未安装！${NC}"
    echo "  请安装 Node.js 18+: curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -"
    exit 1
fi

# 检查 Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}  ✓ Java: $JAVA_VERSION${NC}"
else
    echo -e "${RED}  ✗ Java 未安装！${NC}"
    echo "  请安装 Java 17+: sudo apt install openjdk-17-jdk"
    exit 1
fi

echo ""

# 步骤 2: 创建部署目录
echo -e "${YELLOW}[2/6] 创建部署目录...${NC}"

sudo mkdir -p $DEPLOY_DIR
sudo mkdir -p $BACKEND_DIR
sudo chown -R $USER:$USER $DEPLOY_DIR
sudo chown -R $USER:$USER $BACKEND_DIR

echo -e "${GREEN}  ✓ 目录创建完成${NC}"
echo ""

# 步骤 3: 解压 MCP 服务
echo -e "${YELLOW}[3/6] 部署 MCP 服务...${NC}"

if [ -f "yijing-mcp-server-*.tar.gz" ]; then
    PACKAGE=$(ls yijing-mcp-server-*.tar.gz | head -n 1)
    echo "  - 解压 $PACKAGE..."
    tar -xzf $PACKAGE -C $DEPLOY_DIR
    echo -e "${GREEN}  ✓ 解压完成${NC}"
    
    # 安装依赖
    echo "  - 安装依赖..."
    cd $DEPLOY_DIR
    npm install --production
    echo -e "${GREEN}  ✓ 依赖安装完成${NC}"
else
    echo -e "${RED}  ✗ 未找到部署包！${NC}"
    exit 1
fi

echo ""

# 步骤 4: 部署后端服务
echo -e "${YELLOW}[4/6] 部署后端服务...${NC}"

if [ -f "$BACKEND_JAR" ]; then
    echo "  - 复制后端 JAR..."
    cp $BACKEND_JAR $BACKEND_DIR/
    echo -e "${GREEN}  ✓ 后端部署完成${NC}"
else
    echo -e "${YELLOW}  ! 未找到后端 JAR 文件，请手动上传${NC}"
fi

echo ""

# 步骤 5: 创建 systemd 服务
echo -e "${YELLOW}[5/6] 配置系统服务...${NC}"

# 后端服务
cat > /tmp/yijing-backend.service << EOF
[Unit]
Description=Yijing Backend Service
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$BACKEND_DIR
ExecStart=/usr/bin/java -jar $BACKEND_DIR/$BACKEND_JAR
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo mv /tmp/yijing-backend.service /etc/systemd/system/
echo -e "${GREEN}  ✓ 后端服务配置完成${NC}"

# MCP 服务（可选，如果需要作为服务运行）
cat > /tmp/yijing-mcp.service << EOF
[Unit]
Description=Yijing MCP Service
After=network.target yijing-backend.service
Requires=yijing-backend.service

[Service]
Type=simple
User=$USER
WorkingDirectory=$DEPLOY_DIR
Environment="YIJING_BACKEND_URL=http://localhost:8088"
ExecStart=/usr/bin/node $DEPLOY_DIR/dist/index.js
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo mv /tmp/yijing-mcp.service /etc/systemd/system/
echo -e "${GREEN}  ✓ MCP 服务配置完成${NC}"

echo ""

# 步骤 6: 启动服务
echo -e "${YELLOW}[6/6] 启动服务...${NC}"

sudo systemctl daemon-reload

# 启动后端
sudo systemctl enable yijing-backend
sudo systemctl start yijing-backend
echo -e "${GREEN}  ✓ 后端服务已启动${NC}"

# 等待后端启动
echo "  - 等待后端服务就绪..."
sleep 5

# 检查后端状态
if curl -s http://localhost:8088/api/yijing/hexagrams > /dev/null; then
    echo -e "${GREEN}  ✓ 后端服务运行正常${NC}"
else
    echo -e "${YELLOW}  ! 后端服务可能未完全启动，请检查日志${NC}"
fi

echo ""

# 显示结果
echo "========================================"
echo -e "${GREEN}部署完成！${NC}"
echo "========================================"
echo ""
echo -e "${CYAN}服务状态:${NC}"
echo "  后端服务: sudo systemctl status yijing-backend"
echo "  MCP 服务: sudo systemctl status yijing-mcp"
echo ""
echo -e "${CYAN}查看日志:${NC}"
echo "  后端日志: sudo journalctl -u yijing-backend -f"
echo "  MCP 日志: sudo journalctl -u yijing-mcp -f"
echo ""
echo -e "${CYAN}服务管理:${NC}"
echo "  启动: sudo systemctl start yijing-backend"
echo "  停止: sudo systemctl stop yijing-backend"
echo "  重启: sudo systemctl restart yijing-backend"
echo ""
echo -e "${CYAN}访问地址:${NC}"
echo "  后端 API: http://服务器IP:8088"
echo "  健康检查: http://服务器IP:8088/api/yijing/hexagrams"
echo ""
echo -e "${YELLOW}注意事项:${NC}"
echo "  1. 确保防火墙开放 8088 端口"
echo "  2. 配置 DeepSeek API Key（如需 AI 解读）"
echo "  3. 团队成员配置 YIJING_BACKEND_URL 指向此服务器"
echo ""
