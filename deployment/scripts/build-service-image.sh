#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SERVICE="${1:?Usage: build-service-image.sh <service-module> [tag]}"
TAG="${2:-latest}"

docker build -f "${ROOT_DIR}/deployment/docker/Dockerfile.service" \
  --build-arg SERVICE_MODULE="${SERVICE}" \
  -t "ecommerce/${SERVICE}:${TAG}" \
  "${ROOT_DIR}"

echo "Built ecommerce/${SERVICE}:${TAG}"
