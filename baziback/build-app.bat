@echo off
chcp 65001 >nul
echo 开始打包 Electron 应用...

cd /d "%~dp0"

echo 安装 electron...
call npm install electron@latest --save-dev

echo 安装 @electron/packager...
call npm install @electron/packager --save-dev

echo 检查前端构建...
if not exist "dist\index.html" (
    echo 构建前端...
    call npm run build
)

echo 开始打包 Windows 应用...
call npx --yes @electron/packager . "易经占卜" --platform=win32 --arch=x64 --out=release --overwrite --asar

if exist "release\易经占卜-win32-x64" (
    echo.
    echo 打包完成！应用位于 release\易经占卜-win32-x64 目录
    echo 可执行文件: release\易经占卜-win32-x64\易经占卜.exe
) else (
    echo.
    echo 打包可能失败，请检查错误信息
)

pause
