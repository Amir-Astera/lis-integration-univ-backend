#!/usr/bin/env bash
set -euo pipefail

LABA_ROOT="${LABA_ROOT:-/opt/laba}"
BACKEND_DIR="${LABA_ROOT}/backend"
FRONTEND_DIR="${LABA_ROOT}/frontend"

echo "==> pull backend"
git -C "${BACKEND_DIR}" pull --ff-only

echo "==> pull frontend"
git -C "${FRONTEND_DIR}" pull --ff-only

echo "==> rebuild backend + postgres"
docker compose -f "${BACKEND_DIR}/docker-compose.backend.prod.yml" \
  --env-file "${BACKEND_DIR}/.env.backend" \
  -p laboratory \
  up -d --build

echo "==> build frontend"
cd "${FRONTEND_DIR}"
npm ci
npm run build

echo "==> publish frontend"
sudo rsync -a --delete "${FRONTEND_DIR}/dist/" /var/www/laba/

echo "==> reload nginx"
sudo nginx -t
sudo systemctl reload nginx

echo "Done."
