#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-ecommerce}"
DEPLOYMENT="${1:?Usage: rolling-restart-k8s.sh <deployment-name>}"

echo "Rolling restart of ${DEPLOYMENT} in namespace ${NAMESPACE}..."
kubectl rollout restart "deployment/${DEPLOYMENT}" -n "${NAMESPACE}"
kubectl rollout status "deployment/${DEPLOYMENT}" -n "${NAMESPACE}" --timeout=300s
echo "Rollout complete. Existing JWT sessions remain valid."
