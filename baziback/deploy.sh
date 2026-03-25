#!/bin/bash
# 易经占卜系统部署脚本

# 停止旧服务
echo "停止旧服务..."
pkill -f "bazi-0.0.1-SNAPSHOT.jar" || true

# 创建目录
mkdir -p /opt/bazi-app/frontend
mkdir -p /opt/bazi-app/logs

# 备份旧JAR
if [ -f /opt/bazi-app/bazi-0.0.1-SNAPSHOT.jar ]; then
    mv /opt/bazi-app/bazi-0.0.1-SNAPSHOT.jar /opt/bazi-app/bazi-0.0.1-SNAPSHOT.jar.bak
fi

echo "部署完成，启动服务..."

# 启动后端服务
cd /opt/bazi-app
nohup java -jar bazi-0.0.1-SNAPSHOT.jar --server.port=8088 > logs/app.log 2>&1 &

echo "服务已启动，端口: 8088"
echo "日志文件: /opt/bazi-app/logs/app.log"

# 检查服务状态
sleep 3
if pgrep -f "bazi-0.0.1-SNAPSHOT.jar" > /dev/null; then
    echo "✓ 后端服务启动成功"
else
    echo "✗ 后端服务启动失败，请检查日志"
fi
