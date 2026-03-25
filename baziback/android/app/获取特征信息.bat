@echo off
chcp 65001 >nul
echo ========================================
echo    天机明理 - 应用商店特征信息
echo ========================================
echo.

echo === 1. 安卓平台软件包名称 ===
echo com.xuanxue.divination
echo.

echo === 2. 证书信息（包含MD5指纹）===
echo 正在获取证书信息...
echo.
keytool -list -v -keystore release-key.jks -alias release-key -storepass xuanxue123
echo.

echo === 3. 导出证书文件（用于获取公钥）===
keytool -exportcert -alias release-key -keystore release-key.jks -storepass xuanxue123 -file cert.cer
if exist cert.cer (
    echo 证书已导出到: cert.cer
    echo.
    echo 可以使用以下在线工具解析证书获取公钥：
    echo   - https://www.sslshopper.com/certificate-decoder.html
    echo   - https://www.ssllabs.com/ssltest/
    echo.
    echo 或者使用以下命令查看证书内容：
    echo   keytool -printcert -file cert.cer
) else (
    echo 证书导出失败
)
echo.

echo ========================================
echo 提示：将上述信息复制到应用商店后台填写
echo ========================================
echo.
pause
