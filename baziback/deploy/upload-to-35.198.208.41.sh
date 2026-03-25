#!/usr/bin/env bash
set -euo pipefail

PACKAGE="/mnt/c/Users/Lenovo/Desktop/n8n/back3/baziback/deploy/docker-server-package-20260324-204751.tar.gz"

if [ ! -f "$PACKAGE" ]; then
  echo "Package not found: $PACKAGE" >&2
  exit 1
fi

sshpass -p '4IeLk4OcMlnYMYvaR7tN' scp -o StrictHostKeyChecking=no "$PACKAGE" root@35.198.208.41:/root/
