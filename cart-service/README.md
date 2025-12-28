# Cart Service

## Overview

The Cart Service is a microservice responsible for managing shopping carts in the e-commerce platform. Built with Spring Boot and Redis, it provides high-performance cart operations with real-time updates through Kafka events. The service handles cart creation, item management, cart persistence, and seamless integration with other microservices.

## Features

### Core Functionality
- **Cart Management**: Create, retrieve, update, and delete shopping carts
- **Item Operations**: Add, update, remove items from cart
- **Cart Persistence**: Redis-based storage for fast access
- **Guest Cart Support**: Handle anonymous user carts
- **Cart Merging**: Merge guest cart with user cart on login
- **Real-time Updates**: Kafka event publishing for cart changes

### Advanced Features
- **Cart Summary**: Quick cart statistics and totals
- **Item Validation**: Product information validation
- **Quantity Management**: Smart quantity updates and limits
- **Price Calculation**: Automatic subtotal and total calculations
- **TTL Support**: Automatic cart expiration
- **Concurrent Access**: Thread-safe cart operations

## Technology Stack

- **Framework**: Spring Boot 3.x with Spring Data Redis
- **Database**: Redis (In-memory data store)
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Monitoring**: Spring Boot Actuator with Prometheus
- **Build Tool**: Maven
- **Java Version**: 17+

## Database Schema

### Redis Data Structure

#### Cart Entity
```json
{
  "id": "cart:user:123",
  "userId": 123,
  "items": [
    {
      "productId": 456,
      "productName": "Product Name",
      "productSku": "SKU123",
      "price": 29.99,
      "quantity": 2,
      "subtotal": 59.98,
      "imageUrl": "https://example.com/image.jpg",
      "brand": "Brand Name",
      "categoryId": 789,
      "categoryName": "Category Name",
      "addedAt": "2024-01-15T10:30:00"
    }
  ],
  "totalAmount": 59.98,
  "totalItems": 2,
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Redis Keys Pattern
- **Cart**: `cart:user:{userId}`
- **Session Cart**: `cart:session:{sessionId}`
- **Cart Index**: `cart:index:user:{userId}`

## Key Components

### CartServiceApplication
- **Location**: `com.ecommerce.cart.CartServiceApplication`
- **Purpose**: Main Spring Boot application with Kafka enabled
- **Annotations**: `@SpringBootApplication`, `@EnableKafka`

### CartService
- **Location**: `com.ecommerce.cart.service.CartService`
- **Purpose**: Core business logic for cart operations
- **Key Methods**:
  - `getOrCreateCart()`: Get existing or create new cart
  - `addItemToCart()`: Add product to cart
  - `updateItemQuantity()`: Update item quantity
  - `removeItemFromCart()`: Remove item from cart
  - `clearCart()`: Clear all items from cart
  - `mergeGuestCart()`: Merge guest cart with user cart

### CartController
- **Location**: `com.ecommerce.cart.controller.CartController`
- **Purpose**: REST API endpoints for cart operations
- **Base Path**: `/api/v1/cart`

### Cart Entity
- **Location**: `com.ecommerce.cart.entity.Cart`
- **Purpose**: Redis hash entity for cart data
- **Annotations**: `@RedisHash`, `@Indexed`

### CartRepository
- **Location**: `com.ecommerce.cart.repository.CartRepository`
- **Purpose**: Redis repository for cart data access
- **Extends**: `CrudRepository<Cart, String>`

## API Endpoints

### Cart Operations
```http
GET    /api/v1/cart/{userId}                    # Get or create cart
POST   /api/v1/cart/{userId}/items              # Add item to cart
PUT    /api/v1/cart/{userId}/items/{productId}  # Update item quantity
DELETE /api/v1/cart/{userId}/items/{productId}  # Remove item from cart
DELETE /api/v1/cart/{userId}                    # Clear cart
GET    /api/v1/cart/{userId}/summary            # Get cart summary
POST   /api/v1/cart/{userId}/merge              # Merge guest cart
```

### Request/Response Examples

#### Add Item to Cart
```http
POST /api/v1/cart/123/items
Content-Type: application/json

{
  "productId": 456,
  "productName": "Wireless Headphones",
  "productSku": "WH-001",
  "price": 99.99,
  "quantity": 1,
  "imageUrl": "https://example.com/headphones.jpg",
  "brand": "TechBrand",
  "categoryId": 10,
  "categoryName": "Electronics"
}
```

#### Cart Response
```json
{
  "id": "cart:user:123",
  "userId": 123,
  "items": [
    {
      "productId": 456,
      "productName": "Wireless Headphones",
      "productSku": "WH-001",
      "price": 99.99,
      "quantity": 1,
      "subtotal": 99.99,
      "imageUrl": "https://example.com/headphones.jpg",
      "brand": "TechBrand",
      "categoryId": 10,
      "categoryName": "Electronics",
      "addedAt": "2024-01-15T10:30:00"
    }
  ],
  "totalAmount": 99.99,
  "totalItems": 1,
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## Configuration

### Application Properties
```yaml
server:
  port: 8082

spring:
  application:
    name: cart-service
  
  # Redis Configuration
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
  
  # Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true

# Custom Properties
app:
  cart:
    default-ttl: 86400 # 24 hours
    max-items-per-cart: 50
```

### Environment Variables
- `REDIS_HOST`: Redis server host (default: localhost)
- `REDIS_PORT`: Redis server port (default: 6379)
- `REDIS_PASSWORD`: Redis password (optional)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers
- `EUREKA_SERVER_URL`: Eureka server URL
- `CART_TTL`: Cart time-to-live in seconds
- `MAX_CART_ITEMS`: Maximum items per cart

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis Server 6.0+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)

## Setup Instructions

### 1. Infrastructure Setup
```bash
# Start Redis
redis-server

# Start Kafka (with Zookeeper)
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

# Create Kafka topics
bin/kafka-topics.sh --create --topic cart.updated --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic cart.cleared --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic cart.item.added --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic cart.item.removed --bootstrap-server localhost:9092
```

### 2. Environment Setup
```bash
# Set environment variables
export REDIS_HOST=localhost
export REDIS_PORT=6379
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export EUREKA_SERVER_URL=http://localhost:8761/eureka/
export CART_TTL=86400
export MAX_CART_ITEMS=50
```

### 3. Build and Run
```bash
# Navigate to cart-service directory
cd cart-service

# Build the application
mvn clean compile

# Run tests
mvn test

# Start the service
mvn spring-boot:run
```

### 4. Verify Installation
```bash
# Check service health
curl http://localhost:8082/actuator/health

# Test cart creation
curl -X GET http://localhost:8082/api/v1/cart/123

# Add item to cart
curl -X POST http://localhost:8082/api/v1/cart/123/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 456,
    "productName": "Test Product",
    "productSku": "TEST-001",
    "price": 29.99,
    "quantity": 1,
    "imageUrl": "https://example.com/test.jpg",
    "brand": "TestBrand",
    "categoryId": 1,
    "categoryName": "Test Category"
  }'
```

## Testing

### Unit Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CartServiceTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
class CartServiceIntegrationTest {
    
    @Autowired
    private CartService cartService;
    
    @Test
    void shouldCreateAndRetrieveCart() {
        // Test cart creation and retrieval
    }
    
    @Test
    void shouldAddItemToCart() {
        // Test item addition
    }
}
```

### API Testing
```bash
# Test cart operations with curl

# Get cart
curl -X GET http://localhost:8082/api/v1/cart/123

# Add item
curl -X POST http://localhost:8082/api/v1/cart/123/items \
  -H "Content-Type: application/json" \
  -d '{"productId":456,"productName":"Test","price":29.99,"quantity":1}'

# Update quantity
curl -X PUT http://localhost:8082/api/v1/cart/123/items/456 \
  -H "Content-Type: application/json" \
  -d '{"quantity":3}'

# Remove item
curl -X DELETE http://localhost:8082/api/v1/cart/123/items/456

# Clear cart
curl -X DELETE http://localhost:8082/api/v1/cart/123
```

## Kafka Events

### Published Events

#### Cart Updated Event
```json
{
  "topic": "cart.updated",
  "key": "123",
  "value": {
    "userId": 123,
    "cartId": "cart:user:123",
    "totalAmount": 99.99,
    "totalItems": 2,
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

#### Item Added Event
```json
{
  "topic": "cart.item.added",
  "key": "123",
  "value": {
    "userId": 123,
    "productId": 456,
    "productName": "Wireless Headphones",
    "quantity": 1,
    "price": 99.99,
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

#### Item Removed Event
```json
{
  "topic": "cart.item.removed",
  "key": "123",
  "value": {
    "userId": 123,
    "productId": 456,
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

#### Cart Cleared Event
```json
{
  "topic": "cart.cleared",
  "key": "123",
  "value": {
    "userId": 123,
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

### Event Consumers
Other services can consume these events:
- **Inventory Service**: Update stock reservations
- **Recommendation Service**: Update user preferences
- **Analytics Service**: Track cart behavior
- **Notification Service**: Send cart abandonment emails

## Redis Operations

### Data Storage Patterns
```redis
# Cart storage
HSET cart:user:123 userId 123 totalAmount 99.99 totalItems 2

# Cart items (as JSON)
HSET cart:user:123 items '[{"productId":456,"quantity":1}]'

# Cart expiration
EXPIRE cart:user:123 86400

# Cart indexing
SADD cart:index:user:123 cart:user:123
```

### Performance Optimization
```yaml
# Redis connection pooling
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms
    timeout: 2000ms
```

## Monitoring & Observability

### Health Checks
```bash
# Service health
curl http://localhost:8082/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.6"
      }
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

### Metrics
```bash
# Cart-specific metrics
curl http://localhost:8082/actuator/metrics/cart.operations.total
curl http://localhost:8082/actuator/metrics/cart.items.added
curl http://localhost:8082/actuator/metrics/redis.connections.active
```

### Custom Metrics
```java
@Component
public class CartMetrics {
    
    private final Counter cartCreatedCounter;
    private final Counter itemAddedCounter;
    private final Timer cartOperationTimer;
    
    public CartMetrics(MeterRegistry meterRegistry) {
        this.cartCreatedCounter = Counter.builder("cart.created.total")
            .description("Total number of carts created")
            .register(meterRegistry);
            
        this.itemAddedCounter = Counter.builder("cart.items.added.total")
            .description("Total number of items added to carts")
            .register(meterRegistry);
            
        this.cartOperationTimer = Timer.builder("cart.operation.duration")
            .description("Cart operation duration")
            .register(meterRegistry);
    }
}
```

## Performance Optimization

### Redis Optimization
```yaml
# Connection pooling
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
    timeout: 2000ms

# Serialization optimization
spring:
  redis:
    serializer:
      key: string
      value: json
```

### Caching Strategy
```java
@Service
public class CartService {
    
    @Cacheable(value = "carts", key = "#userId")
    public CartResponseDto getCart(Long userId) {
        // Implementation
    }
    
    @CacheEvict(value = "carts", key = "#userId")
    public void clearCart(Long userId) {
        // Implementation
    }
}
```

### Async Processing
```java
@Async
public CompletableFuture<Void> publishCartEvent(CartEvent event) {
    kafkaTemplate.send("cart.events", event);
    return CompletableFuture.completedFuture(null);
}
```

## Error Handling

### Custom Exceptions
```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCartOperationException extends RuntimeException {
    public InvalidCartOperationException(String message) {
        super(message);
    }
}
```

### Global Exception Handler
```java
@ControllerAdvice
public class CartExceptionHandler {
    
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartNotFound(CartNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("CART_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(RedisConnectionException.class)
    public ResponseEntity<ErrorResponse> handleRedisConnection(RedisConnectionException ex) {
        ErrorResponse error = new ErrorResponse("REDIS_CONNECTION_ERROR", "Cart service temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
```

## Security Considerations

### Input Validation
```java
public class AddToCartDto {
    
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name too long")
    private String productName;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 50, message = "Quantity cannot exceed 50")
    private Integer quantity;
}
```

### Rate Limiting
```java
@Component
public class CartRateLimiter {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String userId, int maxRequests, Duration window) {
        String key = "rate_limit:cart:" + userId;
        String count = redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", window);
            return true;
        }
        
        int currentCount = Integer.parseInt(count);
        if (currentCount >= maxRequests) {
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        return true;
    }
}
```

## Troubleshooting

### Common Issues

1. **Redis Connection Issues**
   ```bash
   # Check Redis connectivity
   redis-cli ping
   
   # Check Redis logs
   tail -f /var/log/redis/redis-server.log
   
   # Test connection from application
   curl http://localhost:8082/actuator/health
   ```

2. **Kafka Connection Issues**
   ```bash
   # Check Kafka topics
   bin/kafka-topics.sh --list --bootstrap-server localhost:9092
   
   # Check consumer groups
   bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
   
   # Monitor Kafka logs
   tail -f logs/server.log
   ```

3. **Cart Data Inconsistency**
   ```bash
   # Check Redis data
   redis-cli
   > KEYS cart:user:*
   > HGETALL cart:user:123
   
   # Clear corrupted cart
   > DEL cart:user:123
   ```

### Performance Issues
- **Slow Redis Operations**: Check Redis memory usage and network latency
- **High Memory Usage**: Implement cart TTL and cleanup policies
- **Kafka Lag**: Monitor consumer lag and increase partitions if needed

### Debugging
```yaml
logging:
  level:
    com.ecommerce.cart: DEBUG
    org.springframework.data.redis: DEBUG
    org.springframework.kafka: DEBUG
```

## Deployment

### Docker Configuration
```dockerfile
FROM openjdk:17-jre-slim
VOLUME /tmp
COPY target/cart-service-1.0.0.jar app.jar
EXPOSE 8082
ENV REDIS_HOST=redis
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  cart-service:
    build: .
    ports:
      - "8082:8082"
    environment:
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
    depends_on:
      - redis
      - kafka
      - eureka-server
    networks:
      - ecommerce-network

  redis:
    image: redis:6.2-alpine
    ports:
      - "6379:6379"
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
  name: cart-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cart-service
  template:
    metadata:
      labels:
        app: cart-service
    spec:
      containers:
      - name: cart-service
        image: ecommerce/cart-service:latest
        ports:
        - containerPort: 8082
        env:
        - name: REDIS_HOST
          value: "redis-service"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: cart-service
spec:
  selector:
    app: cart-service
  ports:
  - port: 8082
    targetPort: 8082
  type: ClusterIP
```

## Contributing

1. **Code Style**: Follow Spring Boot and Redis best practices
2. **Testing**: Ensure comprehensive test coverage for cart operations
3. **Performance**: Optimize Redis operations and Kafka event publishing
4. **Documentation**: Update API documentation and Redis schema

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For Cart Service issues:
- **Redis Issues**: Check connection, memory usage, and data consistency
- **Performance**: Monitor Redis operations and optimize queries
- **Kafka Events**: Verify event publishing and consumer processing
- **API Issues**: Check request validation and error handling