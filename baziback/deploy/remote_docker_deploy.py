#!/usr/bin/env python3
"""
Upload the prepared Docker deployment bundle to a remote server and start it.

Usage:
  python deploy/remote_docker_deploy.py --host 35.198.208.41 --user root --password YOUR_PASSWORD
"""

from __future__ import annotations

import argparse
import pathlib
import posixpath
import socket
import sys
import time
from typing import Optional

import paramiko


REPO_ROOT = pathlib.Path(__file__).resolve().parents[1]
DEPLOY_DIR = REPO_ROOT / "deploy"


def find_latest_package() -> pathlib.Path:
    packages = sorted(
        DEPLOY_DIR.glob("docker-server-package-*.tar.gz"),
        key=lambda item: item.stat().st_mtime,
        reverse=True,
    )
    if not packages:
        raise FileNotFoundError(
            "No deployment archive found in deploy/. Build one before running this script."
        )
    return packages[0]


def print_step(message: str) -> None:
    print(f"[deploy] {message}", flush=True)


def wait_for_tcp(host: str, port: int, timeout_seconds: int) -> None:
    deadline = time.time() + timeout_seconds
    last_error: Optional[Exception] = None
    while time.time() < deadline:
        sock = socket.socket()
        sock.settimeout(5)
        try:
            sock.connect((host, port))
            return
        except Exception as exc:  # noqa: BLE001
            last_error = exc
            time.sleep(2)
        finally:
            sock.close()
    raise TimeoutError(f"Timed out connecting to {host}:{port}: {last_error}")


def run_remote(
    client: paramiko.SSHClient,
    command: str,
    *,
    timeout: int = 1800,
    check: bool = True,
) -> int:
    print_step(f"remote> {command}")
    stdin, stdout, stderr = client.exec_command(command, timeout=timeout, get_pty=True)
    stdin.close()

    while True:
        if stdout.channel.recv_ready():
            data = stdout.channel.recv(4096).decode("utf-8", errors="replace")
            if data:
                print(data, end="", flush=True)
        if stdout.channel.recv_stderr_ready():
            data = stdout.channel.recv_stderr(4096).decode("utf-8", errors="replace")
            if data:
                print(data, end="", file=sys.stderr, flush=True)
        if stdout.channel.exit_status_ready():
            break
        time.sleep(0.2)

    while stdout.channel.recv_ready():
        data = stdout.channel.recv(4096).decode("utf-8", errors="replace")
        if data:
            print(data, end="", flush=True)
    while stdout.channel.recv_stderr_ready():
        data = stdout.channel.recv_stderr(4096).decode("utf-8", errors="replace")
        if data:
            print(data, end="", file=sys.stderr, flush=True)

    exit_code = stdout.channel.recv_exit_status()
    if check and exit_code != 0:
        raise RuntimeError(f"Remote command failed with exit code {exit_code}: {command}")
    return exit_code


def upload_file(
    sftp: paramiko.SFTPClient,
    local_path: pathlib.Path,
    remote_path: str,
) -> None:
    print_step(f"uploading {local_path.name} -> {remote_path}")
    sftp.put(str(local_path), remote_path)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="35.198.208.41")
    parser.add_argument("--port", type=int, default=22)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", required=True)
    parser.add_argument(
        "--package",
        type=pathlib.Path,
        default=find_latest_package(),
        help="Deployment archive to upload.",
    )
    parser.add_argument(
        "--remote-root",
        default="/opt/baziback-docker",
        help="Remote directory where the project will be extracted.",
    )
    parser.add_argument(
        "--upload-dir",
        default="/root",
        help="Temporary directory for the uploaded archive.",
    )
    parser.add_argument(
        "--connect-timeout",
        type=int,
        default=20,
        help="SSH connect timeout in seconds.",
    )
    parser.add_argument(
        "--wait-timeout",
        type=int,
        default=60,
        help="How long to wait for TCP connectivity before failing.",
    )
    args = parser.parse_args()

    package_path = args.package.resolve()
    if not package_path.exists():
        raise FileNotFoundError(f"Package not found: {package_path}")

    print_step(f"waiting for {args.host}:{args.port}")
    wait_for_tcp(args.host, args.port, args.wait_timeout)

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    print_step(f"connecting to {args.user}@{args.host}:{args.port}")
    client.connect(
        hostname=args.host,
        port=args.port,
        username=args.user,
        password=args.password,
        timeout=args.connect_timeout,
        banner_timeout=args.connect_timeout,
        auth_timeout=args.connect_timeout,
        look_for_keys=False,
        allow_agent=False,
    )

    remote_package = posixpath.join(args.upload_dir, package_path.name)
    package_stem = package_path.name
    if package_stem.endswith(".tar.gz"):
        package_stem = package_stem[: -len(".tar.gz")]
    remote_extract_dir = posixpath.join(args.remote_root, package_stem)

    try:
        sftp = client.open_sftp()
        upload_file(sftp, package_path, remote_package)
        sftp.close()

        run_remote(client, f"mkdir -p {args.remote_root}")
        run_remote(client, f"rm -rf {remote_extract_dir}")
        run_remote(client, f"tar -xzf {remote_package} -C {args.remote_root}")

        install_docker = """
if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sh
fi
systemctl enable docker
systemctl start docker
if docker compose version >/dev/null 2>&1; then
  docker compose version
elif command -v docker-compose >/dev/null 2>&1; then
  docker-compose --version
else
  echo "Docker Compose is not available" >&2
  exit 1
fi
"""
        run_remote(client, install_docker)

        deploy_command = f"""
cd {remote_extract_dir}
docker compose --profile local-db down -v || true
docker compose --profile local-db up -d --build
docker compose ps
docker compose logs backend --tail=50
"""
        run_remote(client, deploy_command, timeout=3600)

        print_step("deployment finished")
        print_step(f"frontend: http://{args.host}:3000")
        print_step(f"backend: http://{args.host}:8088")
        return 0
    finally:
        client.close()


if __name__ == "__main__":
    raise SystemExit(main())
