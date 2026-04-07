# API Gateway

## Overview

The API Gateway is the single entry point for all client requests in the e-commerce microservices architecture. Built with Spring Cloud Gateway, it provides intelligent routing, load balancing, authentication, rate limiting, and cross-cutting concerns management. The gateway acts as a reverse proxy that routes requests to appropriate microservices while handling security, monitoring, and protocol translation..

## Features

### Core Functionality
- **Intelligent Routing**: Dynamic service discovery and load balancing
- **Authentication & Authorization**: Centralized security management
- **Rate Limiting**: Protect backend services from overload
- **CORS Support**: Cross-origin resource sharing configuration
- **Request/Response Transformation**: Modify requests and responses
- **Circuit Breaker**: Fault tolerance and resilience patterns

### Advanced Features
- **Service Discovery Integration**: Automatic service registration with Eureka
- **Load Balancing**: Multiple load balancing algorithms
- **Health Checks**: Monitor backend service health
- **Distributed Tracing**: Request tracing with Sleuth and Zipkin
- **Metrics & Monitoring**: Comprehensive gateway analytics
- **SSL Termination**: Handle HTTPS encryption/decryption

## Technology Stack

- **Framework**: Spring Boot 3.x with Spring Cloud Gateway
- **Reactive Stack**: WebFlux with Netty server
- **Service Discovery**: Eureka Client
- **Security**: JWT token validation
- **Monitoring**: Actuator with Prometheus metrics
- **Tracing**: Spring Cloud Sleuth with Zipkin
- **Build Tool**: Maven
- **Java Version**: 17+

## Architecture

### Gateway Flow
```
Client Request → API Gateway → Authentication → Rate Limiting → Routing → Backend Service
                     ↓
            Response Transformation ← Circuit Breaker ← Load Balancer
```

### Service Routes
The gateway routes requests to the following microservices:

| Service | Path Pattern | Backend URL |
|---------|-------------|-------------|
| User Service | `/api/users/**`, `/api/auth/**` | `lb://user-service` |
| Product Service | `/api/products/**` | `lb://product-service` |
| Cart Service | `/api/cart/**` | `lb://cart-service` |
| Order Service | `/api/orders/**` | `lb://order-service` |
| Payment Service | `/api/payments/**` | `lb://payment-service` |
| Inventory Service | `/api/inventory/**` | `lb://inventory-service` |
| Shipping Service | `/api/shipping/**` | `lb://shipping-service` |
| Notification Service | `/api/notifications/**` | `lb://notification-service` |
| Review Service | `/api/reviews/**` | `lb://review-service` |
| Recommendation Service | `/api/recommendations/**` | `lb://recommendation-service` |

## Configuration

### Application Properties
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**,/api/auth/**
          filters:
            - StripPrefix=1
            - name: AuthenticationFilter
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=1
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: false

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway

spring.sleuth:
  zipkin:
    base-url: http://localhost:9411
  sampler:
    probability: 1.0
```

### Environment Variables
- `EUREKA_SERVER_URL`: Eureka server URL (default: http://localhost:8761/eureka/)
- `ZIPKIN_URL`: Zipkin server URL (default: http://localhost:9411)
- `JWT_SECRET`: JWT token secret for validation
- `RATE_LIMIT_REQUESTS`: Rate limit per minute (default: 100)
- `CORS_ALLOWED_ORIGINS`: Allowed CORS origins

## Key Components

### ApiGatewayApplication
- **Location**: `com.ecommerce.gateway.ApiGatewayApplication`
- **Purpose**: Main Spring Boot application class
- **Features**: Auto-configuration for Spring Cloud Gateway

### AuthenticationFilter
- **Location**: `com.ecommerce.gateway.filter.AuthenticationFilter`
- **Purpose**: JWT token validation and authentication
- **Features**: 
  - Public endpoint bypass
  - Bearer token validation
  - User context extraction
  - Error handling

### Route Configuration
- **Location**: `application.yml`
- **Purpose**: Define routing rules and filters
- **Features**: Path-based routing, load balancing, request transformation

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Eureka Server (for service discovery)
- Backend microservices running
- Zipkin Server (optional, for tracing)

## Setup Instructions

### 1. Environment Setup
```bash
# Set environment variables
export EUREKA_SERVER_URL=http://localhost:8761/eureka/
export ZIPKIN_URL=http://localhost:9411
export JWT_SECRET=mySecretKey
export RATE_LIMIT_REQUESTS=100
export CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

### 2. Build and Run
```bash
# Navigate to api-gateway directory
cd api-gateway

# Build the application
mvn clean compile

# Run tests
mvn test

# Start the gateway
mvn spring-boot:run
```

### 3. Verify Installation
```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Check gateway routes
curl http://localhost:8080/actuator/gateway/routes

# Test routing through gateway
curl http://localhost:8080/api/products
```

### 4. Service Dependencies
Ensure the following services are running:
```bash
# Start Eureka Server (port 8761)
# Start backend microservices
# Start Zipkin (optional, port 9411)
```

## API Endpoints

### Gateway Management
```http
GET    /actuator/health                    # Gateway health status
GET    /actuator/gateway/routes            # List all routes
GET    /actuator/gateway/filters           # List all filters
POST   /actuator/gateway/refresh           # Refresh routes
GET    /actuator/metrics                   # Gateway metrics
```

### Proxied Endpoints
All backend service endpoints are accessible through the gateway:

```http
# User Service
POST   /api/auth/login                     # User authentication
POST   /api/auth/register                  # User registration
GET    /api/users/profile                  # User profile

# Product Service
GET    /api/products                       # List products
GET    /api/products/{id}                  # Get product details
POST   /api/products                       # Create product (admin)

# Order Service
GET    /api/orders                         # List user orders
POST   /api/orders                         # Create new order
GET    /api/orders/{id}                    # Get order details

# Payment Service
POST   /api/payments                       # Process payment
GET    /api/payments/{id}                  # Get payment status

# And all other microservice endpoints...
```

## Authentication & Security

### JWT Token Validation
```java
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Skip authentication for public endpoints
            if (isPublicEndpoint(request.getPath().toString())) {
                return chain.filter(exchange);
            }
            
            // Validate JWT token
            String token = extractToken(request);
            if (!isValidToken(token)) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
            
            return chain.filter(exchange);
        };
    }
}
```

### Public Endpoints
The following endpoints bypass authentication:
- `/api/auth/login`
- `/api/auth/register`
- `/api/products` (GET only)
- `/actuator/**`

### Security Headers
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - AddResponseHeader=X-Response-Default-Foo, Default-Bar
        - AddResponseHeader=X-Frame-Options, DENY
        - AddResponseHeader=X-Content-Type-Options, nosniff
        - AddResponseHeader=X-XSS-Protection, 1; mode=block
```

## Rate Limiting

### Redis-based Rate Limiting
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

### Custom Key Resolver
```java
@Bean
KeyResolver userKeyResolver() {
    return exchange -> exchange.getRequest().getHeaders()
        .getFirst("X-User-Id")
        .map(Mono::just)
        .orElse(Mono.just("anonymous"));
}
```

## Circuit Breaker

### Resilience4j Integration
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: userServiceCircuitBreaker
                fallbackUri: forward:/fallback/users

resilience4j:
  circuitbreaker:
    instances:
      userServiceCircuitBreaker:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
```

### Fallback Controller
```java
@RestController
public class FallbackController {
    
    @RequestMapping("/fallback/users")
    public ResponseEntity<String> userServiceFallback() {
        return ResponseEntity.ok("User service is temporarily unavailable");
    }
}
```

## Load Balancing

### Load Balancer Configuration
```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
      cache:
        enabled: true
        ttl: 35s
        capacity: 256

# Service-specific load balancing
user-service:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule
    ConnectTimeout: 3000
    ReadTimeout: 60000
```

### Health Check Integration
```java
@Component
public class CustomHealthIndicator implements ReactiveHealthIndicator {
    
    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth()
            .map(isHealthy -> isHealthy ? 
                Health.up().withDetail("services", "All services healthy").build() :
                Health.down().withDetail("services", "Some services down").build());
    }
}
```

## Monitoring & Observability

### Metrics
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

### Custom Metrics
```java
@Component
public class GatewayMetrics {
    private final Counter requestCounter;
    private final Timer requestTimer;
    
    public GatewayMetrics(MeterRegistry meterRegistry) {
        this.requestCounter = Counter.builder("gateway.requests.total")
            .description("Total gateway requests")
            .register(meterRegistry);
        this.requestTimer = Timer.builder("gateway.request.duration")
            .description("Gateway request duration")
            .register(meterRegistry);
    }
}
```

### Distributed Tracing
```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://localhost:9411
    sampler:
      probability: 1.0
    web:
      skip-pattern: "/actuator.*|/health.*"
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=AuthenticationFilterTest
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class ApiGatewayApplicationTests {
    
    @Test
    void testRouting() {
        // Test gateway routing functionality
    }
}
```

### Load Testing
```bash
# Test gateway performance
ab -n 1000 -c 10 http://localhost:8080/api/products

# Test with authentication
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/profile
```

## Request/Response Transformation

### Request Filters
```java
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        
        return chain.filter(exchange.mutate()
            .request(request.mutate()
                .header("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
                .build())
            .build());
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

### Response Filters
```java
@Component
public class ResponseLoggingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            log.info("Response: {}", response.getStatusCode());
        }));
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}
```

## Error Handling

### Global Error Handler
```java
@Component
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
    
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                        ApplicationContext applicationContext,
                                        ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }
    
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }
    
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}
```

## Performance Optimization

### Connection Pooling
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: elastic
          max-connections: 1000
          max-idle-time: 30s
        connect-timeout: 3000
        response-timeout: 30s
```

### Memory Management
```yaml
spring:
  webflux:
    multipart:
      max-in-memory-size: 1MB
      max-disk-usage-per-part: 10MB
      max-parts: 128
```

## Security Best Practices

### HTTPS Configuration
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
    key-alias: gateway
```

### Security Headers
```java
@Bean
public WebFilter securityHeadersFilter() {
    return (exchange, chain) -> {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("X-Frame-Options", "DENY");
        response.getHeaders().add("X-Content-Type-Options", "nosniff");
        response.getHeaders().add("X-XSS-Protection", "1; mode=block");
        response.getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        return chain.filter(exchange);
    };
}
```

## Troubleshooting

### Common Issues

1. **Service Discovery Issues**
   ```bash
   # Check Eureka registration
   curl http://localhost:8761/eureka/apps
   
   # Check gateway routes
   curl http://localhost:8080/actuator/gateway/routes
   ```

2. **Authentication Failures**
   ```bash
   # Test token validation
   curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/profile
   
   # Check logs for authentication errors
   tail -f logs/api-gateway.log | grep "Authentication"
   ```

3. **Circuit Breaker Issues**
   ```bash
   # Check circuit breaker status
   curl http://localhost:8080/actuator/circuitbreakers
   
   # Monitor circuit breaker events
   curl http://localhost:8080/actuator/circuitbreakerevents
   ```

### Performance Issues
- **High Latency**: Check backend service response times
- **Memory Leaks**: Monitor JVM heap usage
- **Connection Pool Exhaustion**: Adjust pool settings

### Debugging
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty.http.client: DEBUG
    org.springframework.web.reactive: DEBUG
```

## Deployment

### Docker Configuration
```dockerfile
FROM openjdk:17-jre-slim
VOLUME /tmp
COPY target/api-gateway-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: ecommerce/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: EUREKA_SERVER_URL
          value: "http://eureka-server:8761/eureka/"
```

## Contributing

1. **Routing**: Follow RESTful conventions for route patterns
2. **Security**: Ensure all sensitive endpoints are protected
3. **Performance**: Monitor and optimize gateway performance
4. **Documentation**: Update route documentation for new services

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For API Gateway issues:
- **Routing Problems**: Check service discovery and route configuration
- **Authentication Issues**: Verify JWT token validation logic
- **Performance Issues**: Monitor gateway metrics and backend services
- **Circuit Breaker**: Check downstream service health and thresholds