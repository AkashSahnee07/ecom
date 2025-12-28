# Eureka Server

## Overview

The Eureka Server is the service registry and discovery server for the e-commerce microservices architecture. Built with Netflix Eureka, it provides a centralized registry where all microservices register themselves and discover other services. This enables dynamic service discovery, load balancing, and fault tolerance across the distributed system.

## Features

### Core Functionality
- **Service Registration**: Automatic registration of microservices
- **Service Discovery**: Dynamic discovery of available services
- **Health Monitoring**: Continuous health checks of registered services
- **Load Balancing**: Client-side load balancing support
- **Fault Tolerance**: Automatic service deregistration on failure
- **Self-Preservation Mode**: Protect against network partitions

### Advanced Features
- **Multi-Zone Support**: Cross-zone service discovery
- **Metadata Management**: Custom service metadata storage
- **REST API**: Programmatic access to registry
- **Dashboard UI**: Web-based service monitoring
- **Replication**: Multi-node Eureka cluster support
- **Security**: Authentication and authorization support

## Technology Stack

- **Framework**: Spring Boot 3.x with Spring Cloud Netflix
- **Service Discovery**: Netflix Eureka Server
- **Web Server**: Embedded Tomcat
- **Monitoring**: Spring Boot Actuator
- **Build Tool**: Maven
- **Java Version**: 17+

## Architecture

### Service Discovery Flow
```
Microservice A ──register──→ Eureka Server ←──register──── Microservice B
       ↓                           ↓                           ↑
   discover B ──────────────→ Service Registry ←──────── discover A
       ↓                                                       ↑
   call B directly ←─────────────────────────────────────────┘
```

### Registered Services
The following microservices register with Eureka:

| Service Name | Default Port | Health Check URL |
|--------------|--------------|------------------|
| api-gateway | 8080 | `/actuator/health` |
| user-service | 8081 | `/actuator/health` |
| product-service | 8082 | `/actuator/health` |
| cart-service | 8083 | `/actuator/health` |
| inventory-service | 8084 | `/actuator/health` |
| order-service | 8085 | `/actuator/health` |
| payment-service | 8086 | `/actuator/health` |
| notification-service | 8087 | `/actuator/health` |
| shipping-service | 8088 | `/actuator/health` |
| review-service | 8089 | `/actuator/health` |

## Configuration

### Application Properties
```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
    renewal-percent-threshold: 0.85
    renewal-threshold-update-interval-ms: 15000
    expected-client-renewal-interval-seconds: 30
    response-cache-auto-expiration-in-seconds: 180
    response-cache-update-interval-ms: 30000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,eureka
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.netflix.eureka: INFO
    com.netflix.discovery: INFO
```

### Environment Variables
- `EUREKA_HOSTNAME`: Eureka server hostname (default: localhost)
- `EUREKA_PORT`: Eureka server port (default: 8761)
- `SELF_PRESERVATION`: Enable self-preservation mode (default: false)
- `EVICTION_INTERVAL`: Service eviction interval in ms (default: 5000)

## Key Components

### EurekaServerApplication
- **Location**: `com.ecommerce.eureka.EurekaServerApplication`
- **Purpose**: Main Spring Boot application with Eureka Server enabled
- **Annotations**: `@SpringBootApplication`, `@EnableEurekaServer`

### Eureka Dashboard
- **URL**: `http://localhost:8761`
- **Purpose**: Web-based monitoring and management interface
- **Features**: Service status, instance details, system information

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Network connectivity for service registration
- Sufficient memory for service registry (minimum 512MB)

## Setup Instructions

### 1. Environment Setup
```bash
# Set environment variables (optional)
export EUREKA_HOSTNAME=localhost
export EUREKA_PORT=8761
export SELF_PRESERVATION=false
export EVICTION_INTERVAL=5000
```

### 2. Build and Run
```bash
# Navigate to eureka-server directory
cd eureka-server

# Build the application
mvn clean compile

# Run tests
mvn test

# Start Eureka Server
mvn spring-boot:run
```

### 3. Verify Installation
```bash
# Check Eureka Server health
curl http://localhost:8761/actuator/health

# Access Eureka Dashboard
open http://localhost:8761

# Check registered services (initially empty)
curl http://localhost:8761/eureka/apps
```

### 4. Start Microservices
After Eureka Server is running, start the microservices:
```bash
# Each service will automatically register with Eureka
# Check the dashboard to see registered services
```

## API Endpoints

### Eureka REST API
```http
GET    /eureka/apps                        # Get all registered applications
GET    /eureka/apps/{appName}              # Get specific application
GET    /eureka/apps/{appName}/{instanceId} # Get specific instance
POST   /eureka/apps/{appName}              # Register new instance
PUT    /eureka/apps/{appName}/{instanceId} # Send heartbeat
DELETE /eureka/apps/{appName}/{instanceId} # Deregister instance
```

### Management Endpoints
```http
GET    /actuator/health                    # Server health status
GET    /actuator/info                      # Server information
GET    /actuator/metrics                   # Server metrics
GET    /actuator/eureka                    # Eureka-specific metrics
```

### Dashboard
```http
GET    /                                   # Eureka Dashboard home
GET    /eureka/status                      # System status page
GET    /eureka/lastn                       # Last N registered services
```

## Service Registration

### Client Configuration
Microservices register with Eureka using this configuration:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
    healthcheck:
      enabled: true
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    prefer-ip-address: true
    metadata-map:
      version: 1.0
      environment: dev
```

### Registration Process
1. **Service Startup**: Microservice starts and reads Eureka configuration
2. **Registration**: Service registers itself with Eureka Server
3. **Heartbeat**: Service sends periodic heartbeats (every 30 seconds)
4. **Discovery**: Service fetches registry of other services
5. **Health Check**: Eureka monitors service health
6. **Deregistration**: Service deregisters on shutdown

## High Availability

### Multi-Node Setup
For production environments, run multiple Eureka servers:

```yaml
# eureka-server-1 (peer1)
eureka:
  instance:
    hostname: eureka-peer1
  client:
    service-url:
      defaultZone: http://eureka-peer2:8761/eureka/,http://eureka-peer3:8761/eureka/

# eureka-server-2 (peer2)
eureka:
  instance:
    hostname: eureka-peer2
  client:
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/,http://eureka-peer3:8761/eureka/
```

### Client Configuration for HA
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/,http://eureka-peer2:8761/eureka/,http://eureka-peer3:8761/eureka/
```

## Monitoring & Observability

### Health Checks
```bash
# Check Eureka Server health
curl http://localhost:8761/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "eureka": {
      "status": "UP",
      "details": {
        "applications": 5
      }
    }
  }
}
```

### Metrics
```bash
# Get Eureka-specific metrics
curl http://localhost:8761/actuator/metrics/eureka.server.registry.size

# Get all metrics
curl http://localhost:8761/actuator/metrics
```

### Custom Health Indicators
```java
@Component
public class EurekaServerHealthIndicator implements HealthIndicator {
    
    @Autowired
    private EurekaServerContext eurekaServerContext;
    
    @Override
    public Health health() {
        PeerAwareInstanceRegistry registry = eurekaServerContext.getRegistry();
        int registeredServices = registry.getApplications().size();
        
        return Health.up()
            .withDetail("registeredServices", registeredServices)
            .withDetail("selfPreservationMode", registry.isSelfPreservationModeEnabled())
            .build();
    }
}
```

## Security

### Basic Authentication
```yaml
spring:
  security:
    user:
      name: eureka
      password: ${EUREKA_PASSWORD:password}
      roles: ADMIN

eureka:
  client:
    service-url:
      defaultZone: http://eureka:password@localhost:8761/eureka/
```

### HTTPS Configuration
```yaml
server:
  port: 8761
  ssl:
    enabled: true
    key-store: classpath:eureka-server.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### Client Security Configuration
```yaml
eureka:
  client:
    service-url:
      defaultZone: https://eureka:password@eureka-server:8761/eureka/
    tls:
      enabled: true
      trust-store: classpath:truststore.jks
      trust-store-password: ${TRUSTSTORE_PASSWORD}
```

## Performance Tuning

### Server Optimization
```yaml
eureka:
  server:
    # Disable self-preservation in development
    enable-self-preservation: false
    
    # Faster eviction for development
    eviction-interval-timer-in-ms: 5000
    
    # Production settings
    # enable-self-preservation: true
    # eviction-interval-timer-in-ms: 60000
    
    # Response cache settings
    response-cache-auto-expiration-in-seconds: 180
    response-cache-update-interval-ms: 30000
    
    # Delta settings
    retention-time-in-m-s-in-delta-queue: 180000
    delta-retention-timer-interval-in-ms: 30000
```

### JVM Tuning
```bash
# JVM options for Eureka Server
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
mvn spring-boot:run
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test
mvn test -Dtest=EurekaServerApplicationTests
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class EurekaServerApplicationTests {
    
    @Test
    void contextLoads() {
        // Test that Eureka Server starts successfully
    }
    
    @Test
    void eurekaServerIsUp() {
        // Test that Eureka endpoints are accessible
    }
}
```

### Service Registration Testing
```bash
# Test service registration
curl -X POST http://localhost:8761/eureka/apps/TEST-SERVICE \
  -H "Content-Type: application/json" \
  -d '{
    "instance": {
      "instanceId": "test-instance-1",
      "hostName": "localhost",
      "app": "TEST-SERVICE",
      "ipAddr": "127.0.0.1",
      "port": {"$": 8080, "@enabled": true},
      "securePort": {"$": 8443, "@enabled": false},
      "status": "UP"
    }
  }'

# Verify registration
curl http://localhost:8761/eureka/apps/TEST-SERVICE
```

## Troubleshooting

### Common Issues

1. **Services Not Registering**
   ```bash
   # Check Eureka Server logs
   tail -f logs/eureka-server.log
   
   # Verify client configuration
   curl http://service-host:service-port/actuator/health
   
   # Check network connectivity
   telnet localhost 8761
   ```

2. **Self-Preservation Mode**
   ```bash
   # Check if self-preservation is enabled
   curl http://localhost:8761/eureka/status
   
   # Disable for development
   # Set eureka.server.enable-self-preservation=false
   ```

3. **Slow Service Discovery**
   ```yaml
   # Reduce cache intervals for faster discovery
   eureka:
     server:
       response-cache-update-interval-ms: 5000
     client:
       registry-fetch-interval-seconds: 5
   ```

### Performance Issues
- **High Memory Usage**: Increase JVM heap size
- **Slow Response**: Optimize cache settings
- **Network Timeouts**: Check network connectivity and firewall rules

### Debugging
```yaml
logging:
  level:
    com.netflix.eureka: DEBUG
    com.netflix.discovery: DEBUG
    org.springframework.cloud.netflix.eureka: DEBUG
```

## Deployment

### Docker Configuration
```dockerfile
FROM openjdk:17-jre-slim
VOLUME /tmp
COPY target/eureka-server-1.0.0.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  eureka-server:
    build: .
    ports:
      - "8761:8761"
    environment:
      - EUREKA_HOSTNAME=eureka-server
    networks:
      - ecommerce-network

networks:
  ecommerce-network:
    driver: bridge
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
spec:
  replicas: 2
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: ecommerce/eureka-server:latest
        ports:
        - containerPort: 8761
        env:
        - name: EUREKA_HOSTNAME
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
spec:
  selector:
    app: eureka-server
  ports:
  - port: 8761
    targetPort: 8761
  type: LoadBalancer
```

## Best Practices

### Development Environment
- Disable self-preservation mode
- Use faster eviction intervals
- Enable debug logging
- Use single Eureka instance

### Production Environment
- Enable self-preservation mode
- Use multiple Eureka instances
- Configure proper health checks
- Enable security (HTTPS, authentication)
- Monitor service registry size
- Set up proper logging and monitoring

### Client Configuration
- Use proper lease renewal intervals
- Enable health checks
- Configure retry mechanisms
- Use multiple Eureka server URLs

## Contributing

1. **Configuration**: Follow Spring Cloud Netflix conventions
2. **Security**: Ensure proper authentication and authorization
3. **Monitoring**: Add comprehensive health checks and metrics
4. **Documentation**: Update service registry documentation

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For Eureka Server issues:
- **Service Registration**: Check client configuration and network connectivity
- **Performance**: Monitor JVM metrics and optimize cache settings
- **High Availability**: Verify peer-to-peer replication
- **Security**: Check authentication and SSL configuration