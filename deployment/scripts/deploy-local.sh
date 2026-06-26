#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/deployment/docker/docker-compose.local.yml"
ENV_FILE="${ROOT_DIR}/deployment/.env"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "No deployment/.env found — using .env.example"
  ENV_FILE="${ROOT_DIR}/deployment/.env.example"
fi

echo "Starting local infrastructure + observability..."
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" up -d

"${ROOT_DIR}/deployment/scripts/wait-for-infra.sh"

echo ""
echo "Local stack is ready."
echo "  Zipkin:     http://localhost:9411"
echo "  Prometheus: http://localhost:9090"
echo "  Grafana:    http://localhost:3002 (admin / see .env)"
