#!/usr/bin/env bash
set -euo pipefail

POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"
KAFKA_HOST="${KAFKA_HOST:-localhost}"
KAFKA_PORT="${KAFKA_PORT:-9092}"
ZIPKIN_HOST="${ZIPKIN_HOST:-localhost}"
ZIPKIN_PORT="${ZIPKIN_PORT:-9411}"
MAX_WAIT="${MAX_WAIT:-180}"

wait_for() {
  local name="$1" host="$2" port="$3"
  local elapsed=0
  echo -n "Waiting for ${name} at ${host}:${port}..."
  while ! bash -c "echo > /dev/tcp/${host}/${port}" 2>/dev/null; do
    sleep 2
    elapsed=$((elapsed + 2))
    if [[ ${elapsed} -ge ${MAX_WAIT} ]]; then
      echo " TIMEOUT"
      exit 1
    fi
    echo -n "."
  done
  echo " OK"
}

wait_for "PostgreSQL" "${POSTGRES_HOST}" "${POSTGRES_PORT}"
wait_for "Redis" "${REDIS_HOST}" "${REDIS_PORT}"
wait_for "Kafka" "${KAFKA_HOST}" "${KAFKA_PORT}"
wait_for "Zipkin" "${ZIPKIN_HOST}" "${ZIPKIN_PORT}"

echo "Infrastructure is ready."
