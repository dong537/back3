@echo off
chcp 65001 >nul
echo ========================================
echo MCP 服务构建和测试脚本
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] 安装依赖...
call npm install
if errorlevel 1 (
    echo ❌ 依赖安装失败！
    pause
    exit /b 1
)
echo ✅ 依赖安装完成
echo.

echo [2/3] 构建项目...
call npm run build
if errorlevel 1 (
    echo ❌ 构建失败！
    pause
    exit /b 1
)
echo ✅ 构建完成
echo.

echo [3/3] 测试 MCP 服务...
echo 请确保后端服务已在 http://localhost:8088 运行
echo.
echo 按任意键开始测试，或 Ctrl+C 取消...
pause >nul

echo.
echo 启动 MCP 服务（开发模式）...
echo 按 Ctrl+C 停止
echo.
call npm run dev

pause
