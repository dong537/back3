@echo off
chcp 65001 >nul
echo ========================================
echo 易经占卜后端服务启动脚本
echo ========================================
echo.

cd /d "%~dp0"

echo [1/2] 检查 JAR 文件...
if not exist "target\bazi-0.0.1-SNAPSHOT.jar" (
    echo ❌ JAR 文件不存在，开始编译...
    echo.
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ❌ 编译失败！
        pause
        exit /b 1
    )
    echo ✅ 编译完成
    echo.
) else (
    echo ✅ JAR 文件已存在
    echo.
)

echo [2/2] 启动后端服务...
echo 后端地址: http://localhost:8088
echo 按 Ctrl+C 停止服务
echo.
echo ========================================
echo.

java -jar target\bazi-0.0.1-SNAPSHOT.jar

pause
