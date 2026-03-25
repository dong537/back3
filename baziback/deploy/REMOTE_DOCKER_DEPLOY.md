# Remote Docker Deploy

This project now includes a local helper that can upload the prepared bundle to a server and start the Docker stack for you.

## Files

- `deploy/remote_docker_deploy.py`
- `deploy/docker-server-package-*.tar.gz`

## What it does

1. Waits for SSH connectivity.
2. Uploads the latest `docker-server-package-*.tar.gz`.
3. Extracts it under `/opt/baziback-docker`.
4. Installs Docker if it is missing.
5. Runs `docker compose --profile local-db up -d --build`.

## Command

```powershell
python deploy/remote_docker_deploy.py --host 35.198.208.41 --user root --password "YOUR_ROOT_PASSWORD"
```

## Remote stack

- Frontend: `http://SERVER_IP:3000`
- Backend: `http://SERVER_IP:8088`
- Database: MariaDB in Docker, internal port `3306`, host port `3307`

## Notes

- The generated `.env` inside the bundle is already set up to use the local MariaDB container.
- AI keys are intentionally left blank. Add them in the remote `.env` after deployment if needed.
- If SSH still times out, fix the server firewall or security group first, then rerun the command above.
