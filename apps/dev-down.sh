#!/usr/bin/env bash
# QartNET — tear down the dev stack started by dev-up.sh.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/apps/backend/qartnet"
LOG_DIR="$ROOT_DIR/.dev-logs"

stop_pid() {
  local label="$1" pid_file="$2"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file")"
    if kill -0 "$pid" 2>/dev/null; then
      echo "▶ Stopping $label (pid=$pid)"
      # Kill the entire process group to catch the maven/node child trees too.
      kill -TERM -- -"$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null || true
      sleep 1
      kill -KILL "$pid" 2>/dev/null || true
    fi
    rm -f "$pid_file"
  fi
}

stop_pid "backend"  "$LOG_DIR/backend.pid"
stop_pid "frontend" "$LOG_DIR/frontend.pid"

if [[ "${1:-}" != "--keep-db" ]]; then
  echo "▶ Stopping Postgres container"
  (cd "$BACKEND_DIR" && docker compose down) || true
fi

echo "✓ Stack stopped."
