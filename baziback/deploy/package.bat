@echo off
chcp 65001 >nul
echo ========================================
echo MCP Server Package Script
echo ========================================
echo.

cd /d "%~dp0\.."

echo [1/4] Building MCP Server...
cd mcp-server
call npm install --production
if errorlevel 1 (
    echo Error: npm install failed
    pause
    exit /b 1
)

call npm run build
if errorlevel 1 (
    echo Error: build failed
    pause
    exit /b 1
)
echo Build completed
echo.

echo [2/4] Creating temp directory...
set TEMP_DIR=%TEMP%\yijing-mcp-deploy
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"

echo [3/4] Copying files...
xcopy /E /I /Y dist "%TEMP_DIR%\dist"
copy /Y package.json "%TEMP_DIR%\"
copy /Y README.md "%TEMP_DIR%\"
copy /Y LICENSE "%TEMP_DIR%\"
if exist package-lock.json copy /Y package-lock.json "%TEMP_DIR%\"

echo [4/4] Creating archive...
cd "%TEMP_DIR%"
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/: " %%a in ('time /t') do (set mytime=%%a%%b)
set PACKAGE_NAME=yijing-mcp-server-%mydate%-%mytime%.tar.gz

cd /d "%~dp0\.."
tar -czf "deploy\%PACKAGE_NAME%" -C "%TEMP_DIR%" .

echo.
echo ========================================
echo Package created successfully!
echo ========================================
echo.
echo Package: deploy\%PACKAGE_NAME%
echo.
echo Next steps:
echo 1. Upload to server: scp deploy\%PACKAGE_NAME% user@server:/tmp/
echo 2. Upload backend JAR: scp target\bazi-0.0.1-SNAPSHOT.jar user@server:/tmp/
echo 3. Run deploy script on server
echo.
echo See CLOUD_DEPLOYMENT_GUIDE.md for details
echo.

rmdir /s /q "%TEMP_DIR%"
pause
