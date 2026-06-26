# Kubernetes deployment manifests

Apply in order:

```bash
kubectl apply -f namespace.yaml
kubectl apply -f secrets.yaml          # from secrets.example.yaml
kubectl apply -f configmap-common.yaml
kubectl apply -f platform/
kubectl apply -f services/
kubectl apply -f ingress.yaml
```

## Zero-downtime single-service deploy

All deployments use `replicas: 2` and `maxUnavailable: 0` rolling updates.

```bash
# Build & push new image
docker build -f deployment/docker/Dockerfile.service \
  --build-arg SERVICE_MODULE=cart-service \
  -t your-registry/ecommerce/cart-service:1.0.1 .

kubectl set image deployment/cart-service \
  cart-service=your-registry/ecommerce/cart-service:1.0.1 \
  -n ecommerce

kubectl rollout status deployment/cart-service -n ecommerce
```

Logged-in users keep their JWT; only cart endpoints may retry during rollout.

## Generate remaining services

Copy `services/cart-service.yaml` and adjust:
- `name`, `image`, `containerPort`, `SERVER_PORT`
- Resource limits per service load

Platform services (`eureka-server`, `config-server`) should be deployed before business services.
