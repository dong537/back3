# 打包 MCP 服务用于云服务器部署
# 使用方法: .\package-for-server.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "易经占卜 MCP 服务 - 云服务器打包脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
$mcpServerDir = Join-Path $projectRoot "mcp-server"
$deployDir = $PSScriptRoot
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$packageName = "yijing-mcp-server-$timestamp.tar.gz"

# 步骤 1: 检查环境
Write-Host "[1/5] 检查环境..." -ForegroundColor Yellow

# 检查 Node.js
try {
    $nodeVersion = node --version
    Write-Host "  ✓ Node.js: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Node.js 未安装！" -ForegroundColor Red
    exit 1
}

# 检查 npm
try {
    $npmVersion = npm --version
    Write-Host "  ✓ npm: v$npmVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ npm 未安装！" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤 2: 构建 MCP 服务
Write-Host "[2/5] 构建 MCP 服务..." -ForegroundColor Yellow
Set-Location $mcpServerDir

# 安装依赖
Write-Host "  - 安装依赖..." -ForegroundColor Gray
npm install --production 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ✗ 依赖安装失败！" -ForegroundColor Red
    exit 1
}

# 构建
Write-Host "  - 编译 TypeScript..." -ForegroundColor Gray
npm run build 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ✗ 构建失败！" -ForegroundColor Red
    exit 1
}

Write-Host "  ✓ 构建完成" -ForegroundColor Green
Write-Host ""

# 步骤 3: 准备部署文件
Write-Host "[3/5] 准备部署文件..." -ForegroundColor Yellow

# 创建临时目录
$tempDir = Join-Path $env:TEMP "yijing-mcp-deploy"
if (Test-Path $tempDir) {
    Remove-Item -Path $tempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $tempDir | Out-Null

# 复制必要文件
$deployFiles = @(
    "dist",
    "package.json",
    "package-lock.json",
    "README.md",
    "LICENSE"
)

foreach ($file in $deployFiles) {
    $source = Join-Path $mcpServerDir $file
    if (Test-Path $source) {
        Copy-Item -Path $source -Destination $tempDir -Recurse -Force
        Write-Host "  ✓ 复制 $file" -ForegroundColor Green
    }
}

Write-Host ""

# 步骤 4: 打包
Write-Host "[4/5] 创建部署包..." -ForegroundColor Yellow

Set-Location $tempDir
$outputPath = Join-Path $deployDir $packageName

# 使用 tar 打包（Windows 10+ 自带）
tar -czf $outputPath *
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ✗ 打包失败！" -ForegroundColor Red
    exit 1
}

Write-Host "  ✓ 打包完成: $packageName" -ForegroundColor Green
Write-Host ""

# 步骤 5: 清理
Write-Host "[5/5] 清理临时文件..." -ForegroundColor Yellow
Remove-Item -Path $tempDir -Recurse -Force
Write-Host "  ✓ 清理完成" -ForegroundColor Green
Write-Host ""

# 显示结果
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "打包完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "部署包位置: $outputPath" -ForegroundColor White
Write-Host "文件大小: $((Get-Item $outputPath).Length / 1KB) KB" -ForegroundColor White
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "  1. 上传到云服务器: scp $packageName user@server:/path/to/deploy/" -ForegroundColor Gray
Write-Host "  2. 在服务器上解压: tar -xzf $packageName" -ForegroundColor Gray
Write-Host "  3. 安装依赖: npm install --production" -ForegroundColor Gray
Write-Host "  4. 配置并启动服务" -ForegroundColor Gray
Write-Host ""
Write-Host "详细部署说明请查看: CLOUD_DEPLOYMENT_GUIDE.md" -ForegroundColor Cyan
Write-Host ""

Set-Location $projectRoot
