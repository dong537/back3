# 测试后端API的PowerShell脚本

Write-Host "=== 测试每日运势详情API ===" -ForegroundColor Cyan

# 测试1：检查后端服务是否运行
Write-Host "`n[1] 检查后端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8088/api/daily-fortune-detail/today" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ 后端服务正常运行" -ForegroundColor Green
    Write-Host "  状态码: $($response.StatusCode)" -ForegroundColor Gray
    
    # 解析JSON响应
    $json = $response.Content | ConvertFrom-Json
    Write-Host "`n[2] API响应内容:" -ForegroundColor Yellow
    Write-Host "  Success: $($json.success)" -ForegroundColor Gray
    if ($json.success) {
        Write-Host "  ✓ API调用成功" -ForegroundColor Green
        if ($json.data) {
            Write-Host "  ✓ 返回了数据" -ForegroundColor Green
            Write-Host "  日期: $($json.data.date)" -ForegroundColor Gray
            if ($json.data.aspects) {
                Write-Host "  运势维度数量: $($json.data.aspects.PSObject.Properties.Count)" -ForegroundColor Gray
            }
        } else {
            Write-Host "  ✗ 没有返回数据" -ForegroundColor Red
        }
    } else {
        Write-Host "  ✗ API调用失败: $($json.message)" -ForegroundColor Red
    }
    
    # 显示完整响应（格式化）
    Write-Host "`n[3] 完整响应JSON:" -ForegroundColor Yellow
    $json | ConvertTo-Json -Depth 5 | Write-Host
    
} catch {
    Write-Host "✗ 后端服务无法访问" -ForegroundColor Red
    Write-Host "  错误: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`n请检查:" -ForegroundColor Yellow
    Write-Host "  1. 后端服务是否启动 (mvn spring-boot:run)" -ForegroundColor Gray
    Write-Host "  2. 端口8088是否被占用" -ForegroundColor Gray
    Write-Host "  3. 防火墙是否阻止了连接" -ForegroundColor Gray
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Cyan
