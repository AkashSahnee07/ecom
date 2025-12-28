# Config Server

## Overview

The Config Server is a centralized configuration management service for the e-commerce microservices architecture. Built with Spring Cloud Config, it provides externalized configuration in a distributed system, allowing all microservices to retrieve their configuration from a single, centralized location. This enables dynamic configuration updates, environment-specific settings, and consistent configuration management across the entire platform.

## Features

### Core Functionality
- **Centralized Configuration**: Single source of truth for all service configurations
- **Environment-Specific Configs**: Support for dev, test, staging, and production environments
- **Dynamic Refresh**: Runtime configuration updates without service restart
- **Version Control Integration**: Git-based configuration versioning
- **Native File System Support**: Local file system configuration option
- **Encryption/Decryption**: Secure handling of sensitive configuration data

### Advanced Features
- **Profile-Based Configuration**: Environment and service-specific profiles
- **Configuration Validation**: Schema validation for configuration files
- **Audit Trail**: Track configuration changes and access
- **High Availability**: Clustered config server deployment
- **Caching**: Client-side configuration caching
- **Fallback Mechanism**: Local configuration fallback on server unavailability

## Technology Stack

- **Framework**: Spring Boot 3.x with Spring Cloud Config Server
- **Configuration Storage**: Git Repository / Native File System
- **Service Discovery**: Netflix Eureka
- **Monitoring**: Spring Boot Actuator
- **Security**: Spring Security (optional)
- **Build Tool**: Maven
- **Java Version**: 17+

## Architecture

### Configuration Flow
```
Git Repository ──→ Config Server ──→ Microservices
       ↓                ↓               ↓
   Version Control   Centralized    Runtime Config
   Configuration     Management      Refresh
```

### Configuration Hierarchy
```
config-repo/
├── application.yml              # Common configuration for all services
├── application-dev.yml          # Development environment
├── application-prod.yml         # Production environment
├── user-service.yml            # User service specific config
├── user-service-dev.yml        # User service dev config
├── product-service.yml         # Product service specific config
├── order-service.yml           # Order service specific config
├── kafka-common.yml            # Shared Kafka configuration
└── tracing-common.yml          # Shared tracing configuration
```

## Configuration Structure

### Common Configuration (application.yml)
```yaml
# Distributed Tracing
spring:
  sleuth:
    zipkin:
      base-url: http://localhost:9411
    sampler:
      probability: 1.0

# Service Discovery
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true

# Monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

# Kafka Configuration
spring.kafka:
  bootstrap-servers: localhost:9092
  producer:
    key-serializer: org.apache.kafka.common.serialization.StringSerializer
    value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### Kafka Common Configuration
```yaml
# Comprehensive Kafka setup for all services
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      acks: all
      retries: 3
      batch-size: 16384
      compression-type: snappy
      enable-idempotence: true
    consumer:
      group-id: ${spring.application.name}
      auto-offset-reset: earliest
      enable-auto-commit: false

# Topic Definitions
kafka:
  topics:
    user-registered: user.registered
    product-updated: product.updated
    order-created: order.created
    payment-completed: payment.completed
    # ... more topics
```

### Tracing Common Configuration
```yaml
# Distributed tracing setup
spring:
  sleuth:
    enabled: true
    sampler:
      probability: ${SLEUTH_SAMPLING_PROBABILITY:1.0}
    zipkin:
      base-url: ${ZIPKIN_BASE_URL:http://localhost:9411}
      enabled: true
    baggage:
      correlation-fields:
        - user-id
        - session-id
        - request-id
```

## Key Components

### ConfigServerApplication
- **Location**: `com.ecommerce.config.ConfigServerApplication`
- **Purpose**: Main Spring Boot application with Config Server enabled
- **Annotations**: `@SpringBootApplication`, `@EnableConfigServer`

### Configuration Sources
1. **Git Repository**: Remote Git-based configuration storage
2. **Native File System**: Local classpath-based configuration
3. **Vault Integration**: HashiCorp Vault for secrets management
4. **Database**: JDBC-based configuration storage

## Configuration

### Server Configuration
```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        # Git-based configuration
        git:
          uri: https://github.com/your-org/ecommerce-config-repo
          clone-on-start: true
          default-label: main
          search-paths: config
          username: ${GIT_USERNAME}
          password: ${GIT_PASSWORD}
        # Native file system configuration
        native:
          search-locations: classpath:/config
  profiles:
    active: native  # or 'git' for Git-based config

# Service Discovery
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,refresh,env,configprops
  endpoint:
    health:
      show-details: always
```

### Environment Variables
- `CONFIG_SERVER_PORT`: Config server port (default: 8888)
- `GIT_REPO_URI`: Git repository URI for configurations
- `GIT_USERNAME`: Git repository username
- `GIT_PASSWORD`: Git repository password or token
- `EUREKA_SERVER_URL`: Eureka server URL
- `ENCRYPT_KEY`: Encryption key for sensitive data

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git repository (for Git-based configuration)
- Eureka Server (for service discovery)
- Network access to configuration repository

## Setup Instructions

### 1. Configuration Repository Setup

#### Option A: Git Repository
```bash
# Create configuration repository
git init ecommerce-config-repo
cd ecommerce-config-repo

# Create configuration files
mkdir config
touch config/application.yml
touch config/user-service.yml
touch config/product-service.yml

# Commit and push
git add .
git commit -m "Initial configuration"
git remote add origin https://github.com/your-org/ecommerce-config-repo.git
git push -u origin main
```

#### Option B: Native File System
```bash
# Configuration files are stored in src/main/resources/config/
mkdir -p src/main/resources/config
cp application.yml src/main/resources/config/
cp kafka-common.yml src/main/resources/config/
cp tracing-common.yml src/main/resources/config/
```

### 2. Environment Setup
```bash
# Set environment variables
export CONFIG_SERVER_PORT=8888
export GIT_REPO_URI=https://github.com/your-org/ecommerce-config-repo.git
export GIT_USERNAME=your-username
export GIT_PASSWORD=your-token
export EUREKA_SERVER_URL=http://localhost:8761/eureka/
export ENCRYPT_KEY=your-encryption-key
```

### 3. Build and Run
```bash
# Navigate to config-server directory
cd config-server

# Build the application
mvn clean compile

# Run tests
mvn test

# Start Config Server
mvn spring-boot:run
```

### 4. Verify Installation
```bash
# Check Config Server health
curl http://localhost:8888/actuator/health

# Test configuration retrieval
curl http://localhost:8888/user-service/default
curl http://localhost:8888/user-service/dev
curl http://localhost:8888/application/default

# Check available configurations
curl http://localhost:8888/actuator/env
```

## API Endpoints

### Configuration Retrieval
```http
GET /{application}/{profile}[/{label}]     # Get configuration for service
GET /{application}-{profile}.yml           # Get YAML configuration
GET /{application}-{profile}.json          # Get JSON configuration
GET /{application}-{profile}.properties    # Get Properties configuration
```

### Management Endpoints
```http
GET /actuator/health                       # Server health status
GET /actuator/info                         # Server information
GET /actuator/env                          # Environment properties
GET /actuator/configprops                  # Configuration properties
POST /actuator/refresh                     # Refresh configuration
```

### Configuration Examples

#### Get User Service Configuration
```bash
# Default profile
curl http://localhost:8888/user-service/default

# Development profile
curl http://localhost:8888/user-service/dev

# Production profile
curl http://localhost:8888/user-service/prod

# Specific Git branch/tag
curl http://localhost:8888/user-service/prod/v1.2.0
```

#### Response Format
```json
{
  "name": "user-service",
  "profiles": ["dev"],
  "label": null,
  "version": "abc123",
  "state": null,
  "propertySources": [
    {
      "name": "https://github.com/your-org/config-repo/user-service-dev.yml",
      "source": {
        "server.port": 8081,
        "spring.datasource.url": "jdbc:postgresql://localhost:5432/userdb_dev",
        "logging.level.com.ecommerce.user": "DEBUG"
      }
    }
  ]
}
```

## Client Configuration

### Service Configuration (bootstrap.yml)
```yaml
spring:
  application:
    name: user-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
        max-interval: 2000
        multiplier: 1.1
  profiles:
    active: dev
```

### Configuration Refresh
```java
@RestController
@RefreshScope
public class ConfigurableController {
    
    @Value("${app.message:Default Message}")
    private String message;
    
    @GetMapping("/message")
    public String getMessage() {
        return message;
    }
}
```

### Refresh Configuration
```bash
# Refresh specific service configuration
curl -X POST http://user-service:8081/actuator/refresh

# Refresh all services using Spring Cloud Bus
curl -X POST http://localhost:8888/actuator/bus-refresh
```

## Security

### Basic Authentication
```yaml
spring:
  security:
    user:
      name: config-user
      password: ${CONFIG_SERVER_PASSWORD:config-password}
      roles: ADMIN
```

### Client Authentication
```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
      username: config-user
      password: config-password
```

### Encryption/Decryption
```yaml
# Server configuration
encrypt:
  key: ${ENCRYPT_KEY:my-secret-key}

# Or use key store
encrypt:
  key-store:
    location: classpath:config-server.jks
    password: ${KEYSTORE_PASSWORD}
    alias: config-server
```

#### Encrypt Sensitive Data
```bash
# Encrypt a value
curl -X POST http://localhost:8888/encrypt -d "mysecretpassword"
# Returns: {cipher}AQA...

# Decrypt a value
curl -X POST http://localhost:8888/decrypt -d "{cipher}AQA..."
# Returns: mysecretpassword
```

#### Use Encrypted Values
```yaml
# In configuration file
spring:
  datasource:
    password: '{cipher}AQA...encrypted-password...'
```

## High Availability

### Multiple Config Server Instances
```yaml
# Client configuration for HA
spring:
  cloud:
    config:
      uri: http://config-server-1:8888,http://config-server-2:8888,http://config-server-3:8888
      fail-fast: true
      retry:
        max-attempts: 6
```

### Load Balancer Configuration
```yaml
# Using Ribbon for load balancing
spring:
  cloud:
    config:
      discovery:
        enabled: true
        service-id: config-server

config-server:
  ribbon:
    listOfServers: config-server-1:8888,config-server-2:8888
```

## Monitoring & Observability

### Health Checks
```bash
# Config Server health
curl http://localhost:8888/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "configServer": {
      "status": "UP",
      "details": {
        "repository": "git",
        "propertySources": 15
      }
    }
  }
}
```

### Metrics
```bash
# Configuration access metrics
curl http://localhost:8888/actuator/metrics/config.server.requests

# Git repository metrics
curl http://localhost:8888/actuator/metrics/config.server.git.requests
```

### Custom Health Indicators
```java
@Component
public class ConfigRepositoryHealthIndicator implements HealthIndicator {
    
    @Autowired
    private EnvironmentRepository repository;
    
    @Override
    public Health health() {
        try {
            Environment env = repository.findOne("application", "default", null);
            return Health.up()
                .withDetail("propertySources", env.getPropertySources().size())
                .withDetail("version", env.getVersion())
                .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

## Configuration Management

### Configuration File Organization
```
config-repo/
├── application.yml                 # Global configuration
├── application-{profile}.yml       # Environment-specific global config
├── {service-name}.yml             # Service-specific configuration
├── {service-name}-{profile}.yml   # Service + environment specific
├── shared/
│   ├── kafka-common.yml           # Shared Kafka configuration
│   ├── tracing-common.yml         # Shared tracing configuration
│   ├── database-common.yml        # Shared database configuration
│   └── security-common.yml        # Shared security configuration
└── secrets/
    ├── database-credentials.yml    # Encrypted database credentials
    └── api-keys.yml               # Encrypted API keys
```

### Configuration Precedence
1. `{service-name}-{profile}.yml`
2. `{service-name}.yml`
3. `application-{profile}.yml`
4. `application.yml`

### Best Practices
```yaml
# Use environment variables for sensitive data
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/ecommerce}
    username: ${DATABASE_USERNAME:ecommerce_user}
    password: ${DATABASE_PASSWORD:password}

# Use profiles for environment-specific configuration
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

# Use meaningful property names
app:
  features:
    recommendation-engine: ${ENABLE_RECOMMENDATIONS:true}
    real-time-notifications: ${ENABLE_REAL_TIME_NOTIFICATIONS:false}
  limits:
    max-cart-items: ${MAX_CART_ITEMS:50}
    session-timeout: ${SESSION_TIMEOUT:1800}
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test
mvn test -Dtest=ConfigServerApplicationTests
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.config.server.native.search-locations=classpath:/test-config"
})
class ConfigServerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldReturnConfiguration() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/user-service/default", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("user-service");
    }
}
```

### Configuration Testing
```bash
# Test configuration retrieval
curl http://localhost:8888/user-service/test

# Test configuration refresh
curl -X POST http://localhost:8888/actuator/refresh

# Test encryption/decryption
curl -X POST http://localhost:8888/encrypt -d "test-secret"
curl -X POST http://localhost:8888/decrypt -d "{cipher}encrypted-value"
```

## Performance Optimization

### Caching Configuration
```yaml
spring:
  cloud:
    config:
      server:
        git:
          # Enable caching
          clone-on-start: true
          # Cache timeout
          timeout: 10
        # Enable caching
        overrides:
          spring.cache.type: caffeine
```

### Client-Side Caching
```yaml
spring:
  cloud:
    config:
      # Enable client-side caching
      allow-override: true
      override-none: true
      override-system-properties: false
```

### JVM Tuning
```bash
# JVM options for Config Server
export JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
mvn spring-boot:run
```

## Troubleshooting

### Common Issues

1. **Configuration Not Found**
   ```bash
   # Check if configuration exists
   curl http://localhost:8888/user-service/default
   
   # Check Config Server logs
   tail -f logs/config-server.log
   
   # Verify Git repository access
   git clone https://github.com/your-org/ecommerce-config-repo.git
   ```

2. **Git Repository Access Issues**
   ```bash
   # Test Git connectivity
   git ls-remote https://github.com/your-org/ecommerce-config-repo.git
   
   # Check credentials
   curl -u username:token https://api.github.com/user
   
   # Verify SSH keys (if using SSH)
   ssh -T git@github.com
   ```

3. **Service Cannot Connect to Config Server**
   ```bash
   # Check network connectivity
   telnet config-server 8888
   
   # Verify service discovery
   curl http://eureka-server:8761/eureka/apps/CONFIG-SERVER
   
   # Check client configuration
   curl http://user-service:8081/actuator/configprops
   ```

### Performance Issues
- **Slow Configuration Loading**: Enable Git cloning on startup
- **High Memory Usage**: Optimize Git repository size and caching
- **Network Timeouts**: Increase timeout values and implement retry logic

### Debugging
```yaml
logging:
  level:
    org.springframework.cloud.config: DEBUG
    org.springframework.web: DEBUG
    org.eclipse.jgit: DEBUG
```

## Deployment

### Docker Configuration
```dockerfile
FROM openjdk:17-jre-slim
VOLUME /tmp
COPY target/config-server-1.0.0.jar app.jar
EXPOSE 8888
ENV GIT_REPO_URI=https://github.com/your-org/ecommerce-config-repo.git
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  config-server:
    build: .
    ports:
      - "8888:8888"
    environment:
      - GIT_REPO_URI=https://github.com/your-org/ecommerce-config-repo.git
      - GIT_USERNAME=${GIT_USERNAME}
      - GIT_PASSWORD=${GIT_PASSWORD}
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server
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
  name: config-server
spec:
  replicas: 2
  selector:
    matchLabels:
      app: config-server
  template:
    metadata:
      labels:
        app: config-server
    spec:
      containers:
      - name: config-server
        image: ecommerce/config-server:latest
        ports:
        - containerPort: 8888
        env:
        - name: GIT_REPO_URI
          valueFrom:
            secretKeyRef:
              name: config-server-secrets
              key: git-repo-uri
        - name: GIT_USERNAME
          valueFrom:
            secretKeyRef:
              name: config-server-secrets
              key: git-username
        - name: GIT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: config-server-secrets
              key: git-password
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: config-server
spec:
  selector:
    app: config-server
  ports:
  - port: 8888
    targetPort: 8888
  type: ClusterIP
```

## Best Practices

### Configuration Management
- Use meaningful property names and organize by feature
- Implement proper versioning and branching strategy
- Encrypt sensitive data using Config Server encryption
- Use environment variables for deployment-specific values
- Implement configuration validation and testing

### Security
- Enable authentication for Config Server access
- Use HTTPS for all communications
- Implement proper access controls for configuration repository
- Regularly rotate encryption keys
- Audit configuration access and changes

### Performance
- Enable Git cloning on startup for faster access
- Implement proper caching strategies
- Use native profiles for development environments
- Monitor configuration access patterns
- Optimize Git repository structure

### Operations
- Implement health checks and monitoring
- Set up proper logging and alerting
- Plan for disaster recovery and backup
- Document configuration changes and dependencies
- Implement automated configuration testing

## Contributing

1. **Configuration Standards**: Follow naming conventions and organization patterns
2. **Security**: Ensure proper encryption and access controls
3. **Documentation**: Update configuration documentation and examples
4. **Testing**: Implement comprehensive configuration testing

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For Config Server issues:
- **Configuration Issues**: Check repository access and file structure
- **Performance**: Monitor Git operations and caching effectiveness
- **Security**: Verify encryption and authentication setup
- **Integration**: Check client configuration and service discovery