# Nginx 服务器操作命令指南

## 1. 查看 Nginx 配置位置

```bash
# 查找nginx配置文件位置
sudo nginx -t

# 查看nginx主配置文件
cat /etc/nginx/nginx.conf

# 查看所有启用的站点配置
ls -la /etc/nginx/sites-enabled/

# 查看所有可用的站点配置
ls -la /etc/nginx/sites-available/

# 查看conf.d目录下的配置
ls -la /etc/nginx/conf.d/
```

## 2. 查看当前 Nginx 配置

```bash
# 查看你的应用使用的配置文件（根据你的项目，可能是default或自定义名称）
sudo cat /etc/nginx/sites-enabled/default

# 或者如果配置在conf.d目录
sudo cat /etc/nginx/conf.d/bazi-app.conf

# 查看完整的nginx配置（包括所有include的文件）
sudo nginx -T

# 只查看主配置文件
sudo cat /etc/nginx/nginx.conf
```

## 3. 编辑 Nginx 配置

```bash
# 使用nano编辑器（推荐新手）
sudo nano /etc/nginx/sites-enabled/default

# 使用vim编辑器
sudo vim /etc/nginx/sites-enabled/default

# 如果配置在conf.d目录
sudo nano /etc/nginx/conf.d/bazi-app.conf

# 或者直接编辑你项目中的配置文件，然后复制到服务器
# 在本地编辑 back3/baziback/nginx-config.conf
# 然后使用scp上传：
# scp back3/baziback/nginx-config.conf user@your-server:/tmp/
# 然后在服务器上：
# sudo cp /tmp/nginx-config.conf /etc/nginx/sites-enabled/default
```

## 4. 检查配置语法

```bash
# 检查nginx配置语法（非常重要！修改配置后必须执行）
sudo nginx -t

# 如果语法正确，会显示：
# nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
# nginx: configuration file /etc/nginx/nginx.conf test is successful
```

## 5. 重新加载/重启 Nginx

```bash
# 重新加载配置（不中断服务，推荐）
sudo nginx -s reload

# 或者使用systemd
sudo systemctl reload nginx

# 完全重启nginx（会短暂中断服务）
sudo systemctl restart nginx

# 停止nginx
sudo systemctl stop nginx

# 启动nginx
sudo systemctl start nginx

# 查看nginx状态
sudo systemctl status nginx
```

## 6. 查看 Nginx 日志

```bash
# 查看错误日志（实时）
sudo tail -f /var/log/nginx/error.log

# 查看访问日志（实时）
sudo tail -f /var/log/nginx/access.log

# 如果你的配置中指定了自定义日志路径
sudo tail -f /var/log/nginx/ip_access_error.log
sudo tail -f /var/log/nginx/ip_access_access.log

# 查看最近的错误日志（最后50行）
sudo tail -n 50 /var/log/nginx/error.log

# 搜索特定错误
sudo grep -i "403" /var/log/nginx/error.log
sudo grep -i "forbidden" /var/log/nginx/error.log
```

## 7. 测试后端服务

```bash
# 检查后端服务是否在8088端口运行
sudo netstat -tlnp | grep 8088
# 或者
sudo ss -tlnp | grep 8088

# 直接测试后端接口（绕过nginx）
curl -v http://127.0.0.1:8088/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 通过nginx测试（使用localhost）
curl -v http://localhost/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 通过域名测试（如果已配置）
curl -v https://your-domain.com/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'
```

## 8. 查找并替换配置的完整流程

```bash
# 步骤1: 备份当前配置
sudo cp /etc/nginx/sites-enabled/default /etc/nginx/sites-enabled/default.backup.$(date +%Y%m%d_%H%M%S)

# 步骤2: 查看当前配置
sudo cat /etc/nginx/sites-enabled/default

# 步骤3: 编辑配置
sudo nano /etc/nginx/sites-enabled/default

# 步骤4: 检查语法
sudo nginx -t

# 步骤5: 如果语法正确，重新加载
sudo nginx -s reload

# 步骤6: 查看日志确认
sudo tail -f /var/log/nginx/error.log
```

## 9. 快速诊断命令

```bash
# 一键诊断脚本
echo "=== Nginx状态 ==="
sudo systemctl status nginx | head -5

echo -e "\n=== 配置语法检查 ==="
sudo nginx -t

echo -e "\n=== 后端服务检查 ==="
sudo ss -tlnp | grep 8088

echo -e "\n=== 最近的错误日志 ==="
sudo tail -n 20 /var/log/nginx/error.log

echo -e "\n=== 最近的访问日志 ==="
sudo tail -n 10 /var/log/nginx/access.log
```

## 10. 常见问题排查

```bash
# 如果nginx无法启动，查看详细错误
sudo journalctl -u nginx -n 50

# 检查端口占用
sudo lsof -i :80
sudo lsof -i :443

# 检查nginx进程
ps aux | grep nginx

# 检查selinux状态（如果启用）
getenforce
# 如果返回Enforcing，可能需要：
# sudo setenforce 0  # 临时关闭
# 或配置selinux规则
```

## 11. 更新配置文件的推荐方法

```bash
# 方法1: 直接编辑（适合小改动）
sudo nano /etc/nginx/sites-enabled/default

# 方法2: 从项目文件复制（适合大改动）
# 在本地编辑 back3/baziback/nginx-config.conf
# 然后：
scp back3/baziback/nginx-config.conf user@server:/tmp/nginx-config.conf
ssh user@server
sudo cp /tmp/nginx-config.conf /etc/nginx/sites-enabled/default
sudo nginx -t && sudo nginx -s reload

# 方法3: 使用版本控制（推荐生产环境）
# 将配置纳入git管理，通过CI/CD部署
```

## 注意事项

1. **修改配置前一定要备份**
2. **修改后必须执行 `sudo nginx -t` 检查语法**
3. **优先使用 `reload` 而不是 `restart`**（避免服务中断）
4. **修改配置后查看日志确认是否生效**
5. **如果配置错误导致nginx无法启动，可以恢复备份文件**
