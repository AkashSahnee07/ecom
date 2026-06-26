# Deployment Module

All infrastructure, observability, Kubernetes, and deployment documentation for the e-commerce platform.

## Quick start

```bash
# 1. Configure environment
cp deployment/.env.example deployment/.env

# 2. Start infra + observability
./deployment/scripts/deploy-local.sh

# 3. Build & run microservices (from repo root)
mvn clean install -DskipTests
# Start eureka → config-server → api-gateway → business services

# 4. Health check
./deployment/scripts/health-check-all.sh
```

## Documentation

| Document | Description |
|----------|-------------|
| [DEPLOYMENT.md](./DEPLOYMENT.md) | Full deployment guide (local, Docker, K8s, zero-downtime) |

## Directory layout

```
deployment/
├── DEPLOYMENT.md              # Main deployment guide
├── .env.example               # Environment template
├── docker/                    # Docker Compose + Dockerfile
│   ├── docker-compose.infra.yml
│   ├── docker-compose.observability.yml
│   ├── docker-compose.tools.yml
│   ├── docker-compose.local.yml
│   └── Dockerfile.service
├── kubernetes/                # K8s manifests (rolling updates, 2 replicas)
├── observability/             # Prometheus, Grafana, Zipkin docs
├── scripts/                   # deploy, health-check, build helpers
├── nginx/                     # Load balancer example
└── github/                    # CI/CD workflow examples
```

## Access URLs (local defaults)

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Eureka | http://localhost:8761 |
| Zipkin | http://localhost:9411 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3002 |
| pgAdmin (tools profile) | http://localhost:5050 |

See [DEPLOYMENT.md](./DEPLOYMENT.md) for production deployment, zero-downtime rollouts, and multi-client (web/mobile) considerations.
