# Docker Build and Run

## Files

- `docker-compose.yml`
  Starts frontend and backend by default and connects them to the host MySQL.
- `docker-compose.env.example`
  Example runtime environment variables.
- `docker/Dockerfile.backend`
  Runtime image for the Spring Boot backend.
- `docker/Dockerfile.frontend`
  Builds the frontend and serves it with Nginx.
- `docker/nginx/default.conf`
  SPA routing and `/api` reverse proxy.
- `docker-package.ps1`
  One-command packaging script.

## Default mode

Default startup uses the MySQL already running on the host machine:

- `DB_HOST=host.docker.internal`
- `DB_PORT=3306`
- `DB_NAME=bazi`
- `DB_USERNAME=root`
- `DB_PASSWORD=123456`

Run:

```powershell
docker compose up --build -d backend frontend
```

Open:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8088`

## One-command packaging

Build images:

```powershell
.\docker-package.ps1
```

Build and start containers:

```powershell
.\docker-package.ps1 -Up
```

## Local database container

The optional `local-db` profile starts a MariaDB container and imports:

- `database/bazi_all.sql`
- `sql/create_contact_record_table.sql`
- `sql/fix_achievement_compatibility.sql`
- `sql/update_achievement_points.sql`

Run:

```powershell
.\docker-package.ps1 -Up -UseLocalDb
```

Details:

- Image: `mariadb:11.4`
- Host port: `3307`
- Container port: `3306`
- Backend will automatically switch to `mysql:3306` inside Compose when `-UseLocalDb` is used.

You can also start it manually:

```powershell
$env:DB_HOST='mysql'
$env:DB_PORT='3306'
$env:DB_NAME='bazi'
$env:DB_USERNAME='bazi'
$env:DB_PASSWORD='bazi123456'
docker compose --profile local-db up --build -d
```

## Environment variables

Copy the template first:

```powershell
Copy-Item docker-compose.env.example .env
```

Common variables:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MYSQL_PORT`
- `JWT_SECRET`
- `DEEPSEEK_API_KEY`
- `ONE_API_KEY`
- `GEMINI_API_KEY`
- `AGENTPIT_OAUTH_AUTHORIZATION_URL`
- `AGENTPIT_OAUTH_TOKEN_URL`
- `AGENTPIT_OAUTH_USERINFO_URL`
- `AGENTPIT_OAUTH_CLIENT_ID`
- `AGENTPIT_OAUTH_CLIENT_SECRET`
- `AGENTPIT_OAUTH_CALLBACK_URL`
- `AGENTPIT_OAUTH_SCOPE`
- `AGENTPIT_OAUTH_USE_PKCE`
- `VITE_PANGLE_SLOT_ID`
- `VITE_PANGLE_CAROUSEL_SLOT_ID`
- `VITE_PANGLE_SPLASH_SLOT_ID`

## Useful commands

Status:

```powershell
docker compose ps
```

Logs:

```powershell
docker compose logs -f backend
docker compose logs -f frontend
docker compose --profile local-db logs -f mysql
```

Stop:

```powershell
docker compose down
```

Stop and remove volumes:

```powershell
docker compose --profile local-db down -v
```
