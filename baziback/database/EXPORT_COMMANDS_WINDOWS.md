# Windows CMD 数据库导出命令指南

## 问题说明
之前的命令在 Windows CMD 中失败，原因是：
- Windows CMD 不支持 bash 风格的 `$(date +%Y%m%d_%H%M%S)` 语法
- 路径需要使用反斜杠或正斜杠，但不能混合使用

## 解决方案

### 方案 1：使用 PowerShell（推荐）
PowerShell 支持更灵活的时间戳和路径处理。在 Windows 中打开 PowerShell 并运行：

```powershell
# 1. 完整导出（结构 + 数据）
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u root -p bazi | Out-File -Encoding UTF8 "back3\baziback\database\full_backup_$timestamp.sql"

# 2. 仅导出数据（不包括结构）
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u root -p bazi --no-create-info | Out-File -Encoding UTF8 "back3\baziback\database\data_only_$timestamp.sql"

# 3. 仅导出结构（不包括数据）
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u root -p bazi --no-data | Out-File -Encoding UTF8 "back3\baziback\database\structure_only_$timestamp.sql"

# 4. 推荐：一键完整导出（带优化参数）
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u root -p bazi --single-transaction --quick --lock-tables=false | Out-File -Encoding UTF8 "back3\baziback\database\bazi_complete_backup_$timestamp.sql"
```

### 方案 2：使用 CMD 的简单方式
如果你只想用 CMD，可以手动指定文件名（不用时间戳）：

```cmd
# 1. 完整导出
mysqldump -u root -p bazi > back3\baziback\database\full_backup.sql

# 2. 仅数据
mysqldump -u root -p bazi --no-create-info > back3\baziback\database\data_only.sql

# 3. 仅结构
mysqldump -u root -p bazi --no-data > back3\baziback\database\structure_only.sql

# 4. 推荐：完整导出（优化参数）
mysqldump -u root -p bazi --single-transaction --quick --lock-tables=false > back3\baziback\database\bazi_complete_backup.sql
```

### 方案 3：导出到桌面
如果要导出到桌面，使用完整路径：

```powershell
# PowerShell 方式
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u root -p bazi | Out-File -Encoding UTF8 "C:\Users\Lenovo\Desktop\bazi_backup_$timestamp.sql"
```

```cmd
# CMD 方式
mysqldump -u root -p bazi > C:\Users\Lenovo\Desktop\bazi_backup.sql
```

## 导出为 INSERT 语法格式

如果要导出为 INSERT 语法（便于查看和导入），使用以下命令：

```powershell
# PowerShell
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u root -p bazi --no-create-info --extended-insert=FALSE | Out-File -Encoding UTF8 "back3\baziback\database\data_insert_$timestamp.sql"
```

```cmd
# CMD
mysqldump -u root -p bazi --no-create-info --extended-insert=FALSE > back3\baziback\database\data_insert.sql
```

## 参数说明

| 参数 | 说明 |
|------|------|
| `-u root` | 用户名为 root |
| `-p` | 提示输入密码（更安全） |
| `bazi` | 数据库名称 |
| `--no-create-info` | 不导出表结构，仅导出数据 |
| `--no-data` | 不导出数据，仅导出表结构 |
| `--single-transaction` | 使用单一事务，保证数据一致性 |
| `--quick` | 快速导出，减少内存占用 |
| `--lock-tables=false` | 不锁表，允许其他连接继续操作 |
| `--extended-insert=FALSE` | 每条 INSERT 语句只插入一行（便于查看） |

## 执行步骤

1. **打开 PowerShell**（推荐）或 CMD
2. **导航到项目目录**（可选）
3. **复制上面的命令并粘贴**
4. **按 Enter 执行**
5. **输入 MySQL 密码**（提示时）
6. **等待导出完成**

## 验证导出

导出完成后，检查文件是否存在：

```powershell
# PowerShell
Get-ChildItem back3\baziback\database\*.sql | Sort-Object LastWriteTime -Descending | Select-Object -First 5

# CMD
dir back3\baziback\database\*.sql
```

## 上传到云服务器

导出完成后，可以使用 SCP 或其他工具上传到云服务器：

```powershell
# 使用 SCP（需要安装 OpenSSH）
scp back3\baziback\database\bazi_complete_backup.sql user@cloud-server:/path/to/destination/
```

---

**建议**：使用 PowerShell 方案 1 中的第 4 个命令（推荐），它包含了最佳的导出参数，能确保数据一致性和导出效率。
