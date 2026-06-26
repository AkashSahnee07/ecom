#!/usr/bin/env bash
set -euo pipefail

GATEWAY="${GATEWAY_URL:-http://localhost:8080}"

declare -A SERVICES=(
  ["eureka"]="http://localhost:8761/actuator/health"
  ["config-server"]="http://localhost:8888/actuator/health"
  ["api-gateway"]="${GATEWAY}/actuator/health"
  ["user-service"]="http://localhost:8081/actuator/health"
  ["product-service"]="http://localhost:8082/actuator/health"
  ["cart-service"]="http://localhost:8083/actuator/health"
  ["order-service"]="http://localhost:8084/actuator/health"
  ["payment-service"]="http://localhost:8085/actuator/health"
)

failed=0
for name in "${!SERVICES[@]}"; do
  url="${SERVICES[$name]}"
  if curl -sf "${url}" >/dev/null; then
    printf "✓ %-18s %s\n" "${name}" "${url}"
  else
    printf "✗ %-18s %s\n" "${name}" "${url}"
    failed=$((failed + 1))
  fi
done

if [[ ${failed} -gt 0 ]]; then
  echo "${failed} service(s) unhealthy."
  exit 1
fi

echo "All checked services are healthy."
