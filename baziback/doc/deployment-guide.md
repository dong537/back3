# 部署指南

## 服务器信息

- 服务器地址：35.198.208.41
- SSH 连接：`ssh -i ~/.ssh/id_rsa_google_longterm a1@35.198.208.41`
- 操作系统：Debian 12 (bookworm)
- 域名：destiny.agentpit.io

## 架构概览

```
用户 → Caddy (80/443) → Docker 容器
                          ├── app-backend-1  (8088) - Spring Boot
                          ├── app-frontend-1 (3000→8080) - Nginx + React
                          └── app-mysql-1    (3307→3306) - MariaDB 11.4
```

## 关键目录

| 路径 | 说明 |
|------|------|
| `/opt/baziback-docker/app/` | Docker 部署根目录 |
| `/opt/baziback-docker/app/.env` | 环境变量配置（OAuth 凭证、DB 连接等） |
| `/opt/baziback-docker/app/docker-compose.yml` | Docker Compose 编排文件 |
| `/opt/baziback-docker/app/docker/Dockerfile.backend` | 后端镜像构建文件 |
| `/opt/baziback-docker/app/docker/Dockerfile.frontend` | 前端镜像构建文件 |
| `/opt/baziback-docker/app/src/` | 后端 Java 源码 |
| `/opt/baziback-docker/app/target/` | Maven 编译产物 |
| `/home/a1/back3/` | GitHub 代码克隆目录 |

## 部署流程（更新代码）

### 重要规则：禁止修改数据库

**绝对不要执行任何 SQL 脚本（包括建表、删表、数据初始化），以避免清空生产数据。**
数据库变更需要人工确认后，仅使用 `CREATE TABLE IF NOT EXISTS` 或 `ALTER TABLE` 等安全方式。

### 步骤

```bash
# 1. SSH 连接服务器
ssh -i ~/.ssh/id_rsa_google_longterm a1@35.198.208.41

# 2. 拉取最新代码
cd ~/back3 && git pull origin master

# 3. 同步源码到 Docker 部署目录
sudo cp -r ~/back3/baziback/src /opt/baziback-docker/app/src
sudo cp ~/back3/baziback/pom.xml /opt/baziback-docker/app/pom.xml

# 4. 编译（跳过测试）
cd /opt/baziback-docker/app
sudo mvn clean package -DskipTests -q

# 5. 重建并重启 backend 容器（不碰数据库和前端）
sudo docker compose build backend
sudo docker compose down backend
sudo docker compose up -d backend

# 6. 查看启动日志，确认成功
sleep 15
sudo docker logs app-backend-1 --tail 30
# 确认看到 "Started Demo1Application" 字样

# 7. 验证接口
curl -s 'http://localhost:8088/api/auth/agentpit/sso?returnUrl=/' -D - -o /dev/null | grep Location
```

### 如果同时需要更新前端

```bash
# 重建并重启 frontend 容器
cd /opt/baziback-docker/app
sudo cp -r ~/back3/baziback/src-frontend /opt/baziback-docker/app/src-frontend
sudo docker compose build frontend
sudo docker compose down frontend
sudo docker compose up -d frontend
```

## 环境变量说明（.env）

| 变量 | 说明 | 当前值 |
|------|------|--------|
| `AGENTPIT_OAUTH_CLIENT_ID` | OAuth2 Client ID | `cmnkiszzv003b60t9kfs52kn9` |
| `AGENTPIT_OAUTH_CLIENT_SECRET` | OAuth2 Client Secret | `cmnkiszzv003c60t9oalntnw2` |
| `AGENTPIT_OAUTH_CALLBACK_URL` | OAuth2 回调地址 | `https://destiny.agentpit.io/api/auth/agentpit/callback` |
| `DB_HOST` | 数据库主机 | `mysql`（Docker 容器内）|
| `DB_USERNAME` | 数据库用户 | `bazi` |
| `DB_PASSWORD` | 数据库密码 | `bazi123456` |
| `JWT_SECRET` | JWT 签名密钥 | （见 .env 文件） |
| `CORS_ALLOWED_ORIGINS` | 跨域白名单 | `https://destiny.agentpit.io,...` |

完整变量列表见 `/opt/baziback-docker/app/.env`。

## 常用运维命令

```bash
# 查看所有容器状态
sudo docker ps

# 查看 backend 日志
sudo docker logs app-backend-1 --tail 100 -f

# 重启 backend（不重建镜像）
sudo docker compose restart backend

# 进入容器排查
sudo docker exec -it app-backend-1 sh

# 查看 Caddy 配置
sudo cat /etc/caddy/Caddyfile

# 查看端口占用
sudo lsof -i :8088 -P -n
```
