# 打包 Electron 应用脚本
Write-Host "开始打包 Electron 应用..." -ForegroundColor Green

# 检查依赖
if (-not (Test-Path "node_modules\electron")) {
    Write-Host "安装 electron..." -ForegroundColor Yellow
    npm install electron --save-dev
}

if (-not (Test-Path "node_modules\electron-packager")) {
    Write-Host "安装 electron-packager..." -ForegroundColor Yellow
    npm install electron-packager --save-dev
}

# 确保前端已构建
if (-not (Test-Path "dist\index.html")) {
    Write-Host "构建前端..." -ForegroundColor Yellow
    npm run build
}

# 使用 @electron/packager 打包
Write-Host "开始打包 Windows 应用..." -ForegroundColor Green
$appName = "易经占卜"
$outputDir = "release"

# 创建输出目录
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

# 运行 @electron/packager
Write-Host "使用 @electron/packager 打包..." -ForegroundColor Yellow
npx --yes @electron/packager . $appName --platform=win32 --arch=x64 --out=$outputDir --overwrite --asar

if (Test-Path "$outputDir\$appName-win32-x64") {
    Write-Host "打包完成！应用位于 $outputDir\$appName-win32-x64 目录" -ForegroundColor Green
    Write-Host "可执行文件: $outputDir\$appName-win32-x64\$appName.exe" -ForegroundColor Cyan
} else {
    Write-Host "打包可能失败，请检查错误信息" -ForegroundColor Red
}
