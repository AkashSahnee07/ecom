# Distributed Tracing Setup Guide

This guide explains how to set up and use distributed tracing with Sleuth and Zipkin in the eCommerce microservices platform.

## Overview

Distributed tracing helps you understand the flow of requests across multiple microservices, identify performance bottlenecks, and debug issues in a distributed system.

### Components

- **Spring Cloud Sleuth**: Provides distributed tracing capabilities
- **Zipkin**: Collects and visualizes trace data
- **Brave**: Underlying tracing library
- **Kafka**: Optional transport for trace data

## Quick Start

### 1. Start Zipkin Server

```bash
# Start Zipkin and supporting services
docker-compose -f docker-compose-zipkin.yml up -d

# Verify Zipkin is running
curl http://localhost:9411/health
```

### 2. Start Microservices

```bash
# Start all microservices (they will automatically connect to Zipkin)
mvn spring-boot:run -pl eureka-server
mvn spring-boot:run -pl config-server
mvn spring-boot:run -pl api-gateway
mvn spring-boot:run -pl order-service
# ... other services
```

### 3. Access Zipkin UI

Open your browser and navigate to: http://localhost:9411

## Configuration

### Common Configuration

All microservices inherit tracing configuration from `config-server/src/main/resources/config/tracing-common.yml`:

```yaml
spring:
  sleuth:
    enabled: true
    sampler:
      probability: 1.0  # Sample 100% of requests (adjust for production)
    zipkin:
      base-url: http://localhost:9411
      enabled: true
```

### Service-Specific Configuration

Each service can override tracing settings in their `application.yml`:

```yaml
spring:
  sleuth:
    sampler:
      probability: 0.1  # Sample only 10% in production
```

## Features

### 1. Automatic Instrumentation

- **HTTP Requests**: Automatically traced
- **Database Queries**: JPA/JDBC operations traced
- **Kafka Messages**: Producer/consumer operations traced
- **Service Calls**: Inter-service communication traced

### 2. Custom Spans

Add custom spans to your business logic:

```java
@Service
public class OrderService {
    
    @Autowired
    private Tracer tracer;
    
    @NewSpan("process-order")
    public void processOrder(@SpanTag("orderId") String orderId) {
        // Business logic here
        
        // Add custom span
        Span customSpan = tracer.nextSpan()
            .name("validate-order")
            .tag("order.id", orderId)
            .start();
            
        try {
            // Validation logic
            customSpan.tag("validation.result", "success");
        } catch (Exception e) {
            customSpan.tag("error", e.getMessage());
            throw e;
        } finally {
            customSpan.end();
        }
    }
}
```

### 3. Correlation IDs

Traces are automatically correlated across services. You can also add custom correlation:

```java
// Add correlation headers
HttpHeaders headers = new HttpHeaders();
headers.set("X-Correlation-ID", UUID.randomUUID().toString());
headers.set("X-User-ID", userId);
```

### 4. Error Tracking

Errors are automatically captured and tagged:

```java
@RestController
public class OrderController {
    
    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable String id) {
        try {
            return orderService.findById(id);
        } catch (OrderNotFoundException e) {
            // Error automatically captured in trace
            throw e;
        }
    }
}
```

## Monitoring and Alerting

### 1. Performance Monitoring

- **Latency**: Track request duration across services
- **Throughput**: Monitor request rates
- **Error Rates**: Track failure percentages

### 2. Service Dependencies

Zipkin automatically builds a service dependency graph showing:
- Which services call which other services
- Request volumes between services
- Error rates between services

### 3. Custom Metrics

Export tracing metrics to Prometheus:

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      service: ${spring.application.name}
```

## Production Considerations

### 1. Sampling

Adjust sampling rates for production:

```yaml
spring:
  sleuth:
    sampler:
      probability: 0.01  # Sample 1% of requests
```

### 2. Storage

For production, use persistent storage:

```yaml
# docker-compose-zipkin.yml
zipkin:
  environment:
    - STORAGE_TYPE=mysql
    - MYSQL_HOST=mysql-server
    - MYSQL_DB=zipkin
```

### 3. Performance Impact

- Tracing adds ~1-5% overhead
- Use appropriate sampling rates
- Monitor memory usage

### 4. Security

- Don't trace sensitive data
- Use secure transport (HTTPS)
- Implement proper access controls

## Troubleshooting

### Common Issues

1. **Traces not appearing in Zipkin**
   - Check Zipkin connectivity: `curl http://localhost:9411/health`
   - Verify sampling configuration
   - Check application logs for errors

2. **Missing spans**
   - Ensure all services have Sleuth dependencies
   - Check service discovery (Eureka)
   - Verify network connectivity

3. **Performance issues**
   - Reduce sampling rate
   - Check Zipkin server resources
   - Monitor application memory usage

### Debug Configuration

Enable debug logging:

```yaml
logging:
  level:
    org.springframework.cloud.sleuth: DEBUG
    zipkin2: DEBUG
```

## Advanced Features

### 1. Custom Baggage

Propagate custom context across services:

```java
// Set baggage
BaggageField userId = BaggageField.create("user-id");
userId.updateValue("12345");

// Read baggage in another service
String currentUserId = userId.getValue();
```

### 2. Async Tracing

Trace asynchronous operations:

```java
@Async
@NewSpan("async-operation")
public CompletableFuture<String> processAsync() {
    // Async logic here
    return CompletableFuture.completedFuture("result");
}
```

### 3. Database Tracing

Custom database span names:

```java
@Repository
public class OrderRepository {
    
    @NewSpan("db-find-order")
    public Order findById(@SpanTag("order.id") String id) {
        // Database query
    }
}
```

## Integration with Other Tools

### 1. Prometheus + Grafana

- Export tracing metrics to Prometheus
- Create Grafana dashboards for visualization
- Set up alerts based on trace data

### 2. ELK Stack

- Send trace IDs to logs
- Correlate logs with traces
- Search logs by trace ID

### 3. APM Tools

- Integrate with New Relic, Datadog, etc.
- Use OpenTelemetry for vendor-neutral tracing

## Best Practices

1. **Meaningful Span Names**: Use descriptive names for custom spans
2. **Appropriate Tags**: Add relevant business context
3. **Error Handling**: Always capture and tag errors
4. **Performance**: Monitor tracing overhead
5. **Security**: Don't trace sensitive information
6. **Documentation**: Document custom tracing patterns

## Resources

- [Spring Cloud Sleuth Documentation](https://spring.io/projects/spring-cloud-sleuth)
- [Zipkin Documentation](https://zipkin.io/)
- [Brave Documentation](https://github.com/openzipkin/brave)
- [OpenTelemetry](https://opentelemetry.io/)