# Zipkin distributed tracing

Zipkin is included in `deployment/docker/docker-compose.infra.yml`.

- **UI:** http://localhost:9411
- **Health:** http://localhost:9411/health
- **Span endpoint (Micrometer):** http://localhost:9411/api/v2/spans

All Spring Boot services should set:

```yaml
management:
  tracing:
    sampling:
      probability: ${TRACING_SAMPLING:0.1}
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

In Kubernetes, use the in-cluster Zipkin service DNS name instead of localhost.
