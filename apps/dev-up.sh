#!/usr/bin/env bash
# QartNET — bring up the full stack for the academic demo.
# Starts Postgres (docker), backend (Spring Boot), frontend (Angular dev server).
# Usage:
#   ./apps/dev-up.sh             — start everything
#   ./apps/dev-up.sh --no-docker — assume Postgres is already running locally

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/apps/backend/qartnet"
FRONTEND_DIR="$ROOT_DIR/apps/webapp/qartnet-frontend"
LOG_DIR="$ROOT_DIR/.dev-logs"
mkdir -p "$LOG_DIR"

USE_DOCKER=1
for arg in "$@"; do
  [[ "$arg" == "--no-docker" ]] && USE_DOCKER=0
done

# 1. Postgres
if [[ "$USE_DOCKER" == "1" ]]; then
  echo "▶ Starting Postgres via docker compose..."
  (cd "$BACKEND_DIR" && docker compose up -d postgres)
  echo "▶ Waiting for Postgres to become healthy..."
  for i in {1..30}; do
    if (cd "$BACKEND_DIR" && docker compose ps postgres | grep -q "healthy"); then
      echo "  ✓ Postgres is ready"
      break
    fi
    sleep 1
  done
else
  echo "▶ Skipping docker (--no-docker). Make sure Postgres is running on :5432."
fi

# 2. Backend
echo "▶ Starting backend (Spring Boot) — logs: $LOG_DIR/backend.log"
cd "$BACKEND_DIR"
if [[ -f .env ]]; then set -a; source .env; set +a; fi
nohup ./mvnw spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
echo $! > "$LOG_DIR/backend.pid"
cd - >/dev/null

# 3. Frontend
echo "▶ Starting frontend (Angular dev server) — logs: $LOG_DIR/frontend.log"
cd "$FRONTEND_DIR"
nohup npm start > "$LOG_DIR/frontend.log" 2>&1 &
echo $! > "$LOG_DIR/frontend.pid"
cd - >/dev/null

cat <<EOF

✓ Stack is starting up.
  Backend  → http://localhost:8080  (logs: $LOG_DIR/backend.log)
  Frontend → http://localhost:4200  (logs: $LOG_DIR/frontend.log)

Stop everything with:  ./apps/dev-down.sh
Tail logs with:        tail -f $LOG_DIR/backend.log $LOG_DIR/frontend.log
EOF
