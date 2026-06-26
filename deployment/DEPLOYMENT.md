# Deployment Guide

Complete guide for deploying the e-commerce microservices platform locally, with Docker, and on Kubernetes — including zero-downtime rollouts and observability.

---

## Table of contents

1. [Overview](#1-overview)
2. [Prerequisites](#2-prerequisites)
3. [Module structure](#3-module-structure)
4. [Local development](#4-local-development)
5. [Docker infrastructure](#5-docker-infrastructure)
6. [Building service images](#6-building-service-images)
7. [Kubernetes deployment](#7-kubernetes-deployment)
8. [Zero-downtime deploys & logged-in users](#8-zero-downtime-deploys--logged-in-users)
9. [Observability](#9-observability)
10. [Multi-client (web & mobile)](#10-multi-client-web--mobile)
11. [Production checklist](#11-production-checklist)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Overview

The platform uses:

| Layer | Components |
|-------|------------|
| **Edge** | API Gateway (8080) |
| **Platform** | Eureka (8761), Config Server (8888) |
| **Business** | 10 microservices (8081–8090) |
| **Data** | PostgreSQL, MySQL, Redis, MongoDB, Kafka |
| **Auth** | Stateless JWT + Redis blacklist |
| **Observability** | Zipkin, Prometheus, Grafana |

All deployment assets live in the **`deployment/`** module at the repo root.

---

## 2. Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ |
| Maven | 3.8+ |
| Docker & Docker Compose | Latest stable |
| kubectl (optional) | 1.28+ |
| 8 GB+ RAM | Recommended for full local stack |

---

## 3. Module structure

```
deployment/
├── DEPLOYMENT.md                 ← this file
├── README.md                     ← quick index
├── .env.example                  ← copy to .env
├── docker/
│   ├── docker-compose.infra.yml       # DB, Redis, Kafka, Zipkin, MongoDB
│   ├── docker-compose.observability.yml # Prometheus, Grafana, ELK (profile)
│   ├── docker-compose.tools.yml         # pgAdmin, mongo-express (profile)
│   ├── docker-compose.local.yml         # All-in-one local stack
│   └── Dockerfile.service               # Generic image build for any service
├── kubernetes/
│   ├── namespace.yaml
│   ├── secrets.example.yaml
│   ├── configmap-common.yaml
│   ├── platform/                        # eureka, config-server, api-gateway
│   ├── services/                        # business service templates
│   └── ingress.yaml
├── observability/
│   ├── prometheus/                      # scrape config + alert rules
│   ├── grafana/                         # provisioning + dashboards
│   └── zipkin/
├── scripts/
│   ├── deploy-local.sh
│   ├── wait-for-infra.sh
│   ├── health-check-all.sh
│   ├── build-service-image.sh
│   └── rolling-restart-k8s.sh
├── nginx/                               # HA gateway example
└── github/                              # CI/CD workflow example
```

---

## 4. Local development

### 4.1 Configure environment

```bash
cp deployment/.env.example deployment/.env
# Edit JWT_SECRET and passwords for non-dev use
```

### 4.2 Start infrastructure + observability

```bash
./deployment/scripts/deploy-local.sh
```

Or manually:

```bash
docker compose -f deployment/docker/docker-compose.local.yml --env-file deployment/.env up -d
./deployment/scripts/wait-for-infra.sh
```

### 4.3 Optional profiles

```bash
# Admin UIs (pgAdmin, mongo-express on port 8091 — avoids user-service port 8081)
docker compose -f deployment/docker/docker-compose.local.yml \
  --profile tools up -d

# ELK log stack
docker compose -f deployment/docker/docker-compose.local.yml \
  --profile logs up -d
```

### 4.4 Build and run microservices

```bash
mvn clean install -DskipTests
```

**Startup order:**

1. `eureka-server` (8761)
2. `config-server` (8888)
3. `api-gateway` (8080)
4. Business services (any order after platform is UP)

```bash
cd eureka-server && mvn spring-boot:run
# new terminals for each subsequent service...
```

### 4.5 Verify

```bash
./deployment/scripts/health-check-all.sh
curl http://localhost:8761          # Eureka dashboard
curl http://localhost:8080/actuator/health
```

---

## 5. Docker infrastructure

### Compose files

| File | Purpose |
|------|---------|
| `docker-compose.infra.yml` | Core data + messaging + Zipkin |
| `docker-compose.observability.yml` | Prometheus + Grafana |
| `docker-compose.tools.yml` | Admin UIs (profile `tools`) |
| `docker-compose.local.yml` | Includes all of the above |

### Root shortcut

The repo root `docker-compose.yml` includes the deployment module:

```bash
docker compose up -d   # from repo root
```

### Network

All containers share the **`ecom-network`** bridge network.

### Database initialization

PostgreSQL schemas are created from `init-scripts/` on first startup (mounted read-only into the postgres container).

---

## 6. Building service images

Generic Dockerfile builds any microservice from the repo root:

```bash
./deployment/scripts/build-service-image.sh cart-service 1.0.0
# → ecommerce/cart-service:1.0.0
```

Manual:

```bash
docker build -f deployment/docker/Dockerfile.service \
  --build-arg SERVICE_MODULE=cart-service \
  -t ecommerce/cart-service:latest .
```

Supported `SERVICE_MODULE` values match Maven module names (`api-gateway`, `user-service`, `order-service`, etc.).

---

## 7. Kubernetes deployment

### 7.1 Prepare secrets

```bash
cp deployment/kubernetes/secrets.example.yaml deployment/kubernetes/secrets.yaml
# Edit with real secrets — DO NOT commit secrets.yaml
kubectl apply -f deployment/kubernetes/namespace.yaml
kubectl apply -f deployment/kubernetes/secrets.yaml
kubectl apply -f deployment/kubernetes/configmap-common.yaml
```

### 7.2 Deploy platform first

```bash
kubectl apply -f deployment/kubernetes/platform/
kubectl wait --for=condition=available deployment/eureka-server -n ecommerce --timeout=300s
kubectl wait --for=condition=available deployment/config-server -n ecommerce --timeout=300s
kubectl apply -f deployment/kubernetes/platform/api-gateway.yaml
```

### 7.3 Deploy business services

```bash
kubectl apply -f deployment/kubernetes/services/
kubectl apply -f deployment/kubernetes/ingress.yaml
```

### 7.4 Rolling update (single service)

All K8s deployments use **`replicas: 2`** and **`maxUnavailable: 0`** for zero-downtime rollouts:

```bash
./deployment/scripts/build-service-image.sh cart-service 1.0.1
# push to your registry, then:
kubectl set image deployment/cart-service \
  cart-service=your-registry/ecommerce/cart-service:1.0.1 \
  -n ecommerce
kubectl rollout status deployment/cart-service -n ecommerce
```

Or:

```bash
./deployment/scripts/rolling-restart-k8s.sh cart-service
```

See [kubernetes/README.md](./kubernetes/README.md) for details.

---

## 8. Zero-downtime deploys & logged-in users

### Why JWT users stay logged in

Authentication is **stateless JWT**. Tokens live on the client (web/mobile). Deploying one service does **not** invalidate tokens unless you:

- Change `JWT_SECRET` during deploy
- Restart Redis without persistence (blacklist/cart affected)
- Deploy the API Gateway as a single instance without rolling update

### Per-service impact

| Service restarted | Users logged out? | User-visible impact |
|-------------------|-------------------|---------------------|
| cart-service | No | Cart API retries briefly |
| order-service | No | Order API retries briefly |
| user-service | No | Login/refresh unavailable briefly; existing tokens work |
| api-gateway (1 instance) | No | All API calls fail until gateway is back |
| Redis | Risk | Blacklist + cart data affected |

### Requirements for true zero-downtime

1. **≥ 2 replicas** per service being deployed
2. **RollingUpdate** with `maxUnavailable: 0`
3. **Readiness/liveness probes** on `/actuator/health/*`
4. **Graceful shutdown** in Spring Boot:

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

5. **Do not rotate JWT secret** mid-deploy without dual-key validation
6. **Keep Redis/DB running** during application deploys

### Safe deploy order

1. Business services (cart, order, product, …)
2. `user-service` (existing sessions unaffected)
3. `api-gateway` last (or run 2+ instances behind nginx — see `nginx/gateway-upstream.conf.example`)

---

## 9. Observability

### 9.1 Zipkin (distributed tracing)

- **URL:** http://localhost:9411
- Included in infra compose
- Configure sampling via `TRACING_SAMPLING` (default `0.1` in prod, `1.0` in dev)

### 9.2 Prometheus (metrics)

- **URL:** http://localhost:9090
- Scrapes all services at `/actuator/prometheus`
- Alert rules in `observability/prometheus/alerts.yml`:
  - ServiceDown
  - HighErrorRate
  - AuthFailureSpike
  - HighLatencyP99

### 9.3 Grafana (dashboards)

- **URL:** http://localhost:3002
- **Default login:** admin / admin123 (change in `.env`)
- Pre-provisioned Prometheus datasource
- Dashboard: `Ecommerce Platform Overview`

### 9.4 Auth metrics

| Metric | Description |
|--------|-------------|
| `auth.jwt.validation.total` | JWT validations by result (success/failure) and layer (gateway/service) |
| `auth.jwt.validation.duration` | Validation latency |

### 9.5 Optional ELK stack

```bash
docker compose -f deployment/docker/docker-compose.local.yml --profile logs up -d
# Elasticsearch: http://localhost:9200
# Kibana:        http://localhost:5601
```

Correlate logs with traces using `traceId` in log pattern (configured in `config-server/.../jwt-common.yml`).

---

## 10. Multi-client (web & mobile)

The backend is client-agnostic. All clients use the same API:

```
POST /api/auth/login     → { accessToken, refreshToken, expiresIn, user }
Authorization: Bearer <accessToken>
```

| Client | Notes |
|--------|-------|
| **Web SPA** | CORS configured on API Gateway; use gateway URL only |
| **Mobile (iOS/Android)** | No CORS needed; store tokens in Keychain/Keystore |
| **Future clients** | Same JWT contract; consider `/api/v1` versioning |

During rolling deploys, clients should implement **retry + refresh-on-401** — users stay logged in without re-entering credentials.

---

## 11. Production checklist

- [ ] Change all default passwords in `deployment/.env`
- [ ] Set strong `JWT_SECRET` (same value on gateway + all services)
- [ ] Run gateway with **≥ 2 replicas** behind load balancer
- [ ] Run critical services with **≥ 2 replicas**
- [ ] Enable graceful shutdown on all Spring Boot services
- [ ] Set `TRACING_SAMPLING=0.01` to `0.1` (not 100%)
- [ ] Do not expose internal service ports publicly
- [ ] Use persistent volumes for PostgreSQL, Redis, Kafka
- [ ] Configure Prometheus alerts → PagerDuty/Slack
- [ ] Use HTTPS/TLS on ingress (cert-manager or cloud LB)
- [ ] Copy `github/deploy-infra.example.yml` → `.github/workflows/` and customize registry

---

## 12. Troubleshooting

### Infrastructure not ready

```bash
docker compose -f deployment/docker/docker-compose.local.yml ps
./deployment/scripts/wait-for-infra.sh
docker compose logs postgres redis kafka
```

### Service not registering in Eureka

- Ensure Eureka starts first
- Wait 30–60 seconds after business service startup
- Check `http://localhost:8761`

### 401 after deploy

- Verify `JWT_SECRET` unchanged across all services
- Check Redis is running (blacklist)
- Confirm gateway and service clocks are synchronized

### Prometheus shows targets down

- Services must expose `/actuator/prometheus`
- On Docker Desktop, `host.docker.internal` resolves host JVM services
- In K8s, use pod/service DNS instead of static targets

### Port conflicts

| Port | Service |
|------|---------|
| 8080 | API Gateway |
| 8081 | user-service (avoid mongo-express on 8081 — use tools profile port 8091) |
| 8761 | Eureka |

```bash
lsof -i :8080
```

---

## Related documents

- [README.md](../README.md) — project overview
- [QUICK_START.md](../QUICK_START.md) — developer quick start
- [TRACING_SETUP.md](../TRACING_SETUP.md) — tracing configuration details
- [deployment/kubernetes/README.md](./kubernetes/README.md) — K8s specifics
