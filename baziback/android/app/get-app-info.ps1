# 获取应用商店特征信息脚本
# 用于快速获取包名、公钥、MD5指纹等信息

# 设置密钥库信息
$keystorePath = "release-key.jks"
$alias = "release-key"
$password = "xuanxue123"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   天机明理 - 应用商店特征信息" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查密钥库文件是否存在
if (-not (Test-Path $keystorePath)) {
    Write-Host "错误：找不到密钥库文件 $keystorePath" -ForegroundColor Red
    Write-Host "请确保在 android/app 目录下运行此脚本" -ForegroundColor Yellow
    exit 1
}

Write-Host "=== 1. 安卓平台软件包名称 ===" -ForegroundColor Green
Write-Host "com.xuanxue.divination" -ForegroundColor Yellow
Write-Host ""

Write-Host "=== 2. 证书详细信息 ===" -ForegroundColor Green
Write-Host "正在获取证书信息..." -ForegroundColor Cyan
Write-Host ""

# 获取证书详细信息
$certInfo = keytool -list -v -keystore $keystorePath -alias $alias -storepass $password 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "错误：无法读取证书信息" -ForegroundColor Red
    Write-Host $certInfo -ForegroundColor Red
    exit 1
}

Write-Host $certInfo
Write-Host ""

Write-Host "=== 3. MD5指纹（直接复制使用）===" -ForegroundColor Green
$md5Line = $certInfo | Select-String "MD5"
if ($md5Line) {
    $md5Value = $md5Line -replace ".*MD5:\s*", "" -replace ":", ""
    Write-Host "MD5（带冒号）: " -NoNewline -ForegroundColor Yellow
    Write-Host ($md5Line -replace ".*MD5:\s*", "") -ForegroundColor White
    Write-Host "MD5（无冒号）: " -NoNewline -ForegroundColor Yellow
    Write-Host $md5Value -ForegroundColor White
} else {
    Write-Host "未找到MD5指纹" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== 4. SHA1指纹（备用）===" -ForegroundColor Green
$sha1Line = $certInfo | Select-String "SHA1"
if ($sha1Line) {
    Write-Host ($sha1Line -replace ".*SHA1:\s*", "") -ForegroundColor White
} else {
    Write-Host "未找到SHA1指纹" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== 5. 导出证书文件（用于获取公钥）===" -ForegroundColor Green
$certFile = "cert.cer"
keytool -exportcert -alias $alias -keystore $keystorePath -storepass $password -file $certFile 2>&1 | Out-Null

if (Test-Path $certFile) {
    Write-Host "证书已导出到: $certFile" -ForegroundColor Yellow
    Write-Host "可以使用以下在线工具解析证书获取公钥：" -ForegroundColor Cyan
    Write-Host "  - https://www.sslshopper.com/certificate-decoder.html" -ForegroundColor Cyan
    Write-Host "  - https://www.ssllabs.com/ssltest/" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "或者使用以下命令查看证书内容：" -ForegroundColor Cyan
    Write-Host "  keytool -printcert -file $certFile" -ForegroundColor Yellow
} else {
    Write-Host "证书导出失败" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "提示：将上述信息复制到应用商店后台填写" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
