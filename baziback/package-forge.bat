@echo off
chcp 65001 >nul
echo ========================================
echo 使用 Electron Forge 打包应用
echo ========================================
echo.

cd /d "%~dp0"

echo [1/4] 安装 Electron Forge...
call npm install --save-dev @electron-forge/cli

echo.
echo [2/4] 初始化 Electron Forge...
call npx electron-forge import --force

echo.
echo [3/4] 确保前端已构建...
if not exist "dist\index.html" (
    echo 构建前端...
    call npm run build
)

echo.
echo [4/4] 开始打包 Windows 应用...
call npm run package -- --platform=win32

echo.
if exist "out" (
    echo ========================================
    echo 打包完成！
    echo 应用位于 out 目录
    echo ========================================
) else (
    echo 打包可能失败，请检查错误信息
)

pause
