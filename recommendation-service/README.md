# Recommendation Service

## Overview

The Recommendation Service is an intelligent microservice that provides personalized product recommendations using advanced machine learning algorithms. It implements multiple recommendation strategies including collaborative filtering, content-based filtering, and hybrid approaches to deliver highly relevant product suggestions to users.

## Features

### Core Features
- **Multiple Recommendation Algorithms**
  - Collaborative filtering recommendations
  - Content-based filtering recommendations
  - Hybrid recommendation algorithms
  - Trending products identification
- **Real-time Processing**
  - Real-time user behavior tracking
  - Live recommendation updates
  - Dynamic scoring and ranking
- **Machine Learning Capabilities**
  - Product similarity calculations
  - User preference learning
  - Personalized recommendation scoring
  - Model training and inference

### Advanced Features
- **A/B Testing** for recommendation strategies
- **Performance Analytics** and monitoring
- **Caching and Optimization** for fast responses
- **Multi-device Support** with device-specific recommendations
- **Location-based Recommendations**
- **Cross-selling and Upselling** suggestions

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: MongoDB (primary), PostgreSQL (analytics)
- **Cache**: Redis
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Monitoring**: Spring Boot Actuator, Micrometer
- **Circuit Breaker**: Hystrix
- **HTTP Client**: OpenFeign

## Database Schema

### MongoDB Collections

#### User Behavior Collection
```javascript
{
  "_id": "ObjectId",
  "userId": "string",
  "productId": "string",
  "behaviorType": "VIEW|PURCHASE|ADD_TO_CART|LIKE|SHARE",
  "sessionId": "string",
  "deviceType": "string",
  "location": "string",
  "timestamp": "ISODate",
  "duration": "number",
  "metadata": {}
}
```

#### Product Recommendations Collection
```javascript
{
  "_id": "ObjectId",
  "userId": "string",
  "recommendedProductId": "string",
  "sourceProductId": "string",
  "algorithmType": "COLLABORATIVE|CONTENT_BASED|HYBRID|TRENDING",
  "score": "number",
  "confidence": "number",
  "rankPosition": "number",
  "reason": "string",
  "expiresAt": "ISODate",
  "isActive": "boolean",
  "metadata": {}
}
```

#### User Profile Collection
```javascript
{
  "_id": "ObjectId",
  "userId": "string",
  "preferredCategories": ["string"],
  "preferredBrands": ["string"],
  "priceRange": {
    "min": "number",
    "max": "number"
  },
  "demographics": {},
  "lastUpdated": "ISODate"
}
```

## Key Components

### Application Layer
- **RecommendationServiceApplication**: Main Spring Boot application class
- **RecommendationController**: REST API endpoints for recommendations
- **RecommendationEngine**: Core recommendation processing engine

### Service Layer
- **CollaborativeFilteringService**: Implements collaborative filtering algorithms
- **ContentBasedFilteringService**: Implements content-based filtering
- **HybridRecommendationService**: Combines multiple algorithms
- **TrendingRecommendationService**: Identifies trending products
- **UserBehaviorService**: Tracks and analyzes user behavior
- **ProductSimilarityService**: Calculates product similarities

### Data Layer
- **ProductRecommendationRepository**: Recommendation data access
- **UserBehaviorRepository**: User behavior data access
- **ProductSimilarityRepository**: Product similarity data access

### Integration Layer
- **ProductServiceClient**: Feign client for product service
- **UserServiceClient**: Feign client for user service
- **KafkaConfig**: Message broker configuration

## API Endpoints

### Recommendation Endpoints
```http
# Get personalized recommendations
GET /api/v1/recommendations/users/{userId}
GET /api/v1/recommendations/users/{userId}/products/{productId}/similar
GET /api/v1/recommendations/trending
GET /api/v1/recommendations/categories/{categoryId}

# Track user behavior
POST /api/v1/recommendations/behavior
PUT /api/v1/recommendations/behavior/{behaviorId}

# Recommendation management
POST /api/v1/recommendations/generate/{userId}
DELETE /api/v1/recommendations/users/{userId}
PUT /api/v1/recommendations/{recommendationId}/feedback

# Analytics endpoints
GET /api/v1/recommendations/analytics/performance
GET /api/v1/recommendations/analytics/users/{userId}/insights
```

### Health and Monitoring
```http
GET /actuator/health
GET /actuator/metrics
GET /actuator/info
GET /api-docs
GET /swagger-ui.html
```

## Configuration

### Application Properties
```yaml
# MongoDB Configuration
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/recommendation_db
      auto-index-creation: true

# Redis Configuration
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 2000ms

# Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: recommendation-service
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer

# Algorithm Configuration
app:
  recommendation:
    algorithm-weights:
      collaborative-filtering: 0.4
      content-based: 0.3
      hybrid: 0.2
      trending: 0.1
```

### Environment Variables
```bash
# Database
MONGO_URI=mongodb://localhost:27017/recommendation_db
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_CLIENT_SERVICE_URL=http://localhost:8761/eureka

# External Services
PRODUCT_SERVICE_URL=http://localhost:8083
USER_SERVICE_URL=http://localhost:8081

# ML Configuration
ML_MODEL_PATH=/models/recommendation
ML_TRAINING_SCHEDULE=0 0 2 * * ?
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MongoDB 5.0+
- Redis 6.0+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)

## Setup Instructions

### 1. Clone and Build
```bash
# Navigate to recommendation service directory
cd recommendation-service

# Build the application
mvn clean compile
```

### 2. Database Setup
```bash
# Start MongoDB
mongod --dbpath /data/db

# Start Redis
redis-server

# Create indexes (optional - auto-created)
mongo recommendation_db --eval "db.userBehavior.createIndex({userId: 1, timestamp: -1})"
```

### 3. Start Dependencies
```bash
# Start Kafka
bin/kafka-server-start.sh config/server.properties

# Start Eureka Server
cd ../eureka-server && mvn spring-boot:run

# Start Config Server
cd ../config-server && mvn spring-boot:run
```

### 4. Run the Service
```bash
# Development mode
mvn spring-boot:run

# Production mode
mvn clean package
java -jar target/recommendation-service-1.0.0.jar

# With specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 5. Verify Installation
```bash
# Check service health
curl http://localhost:8086/actuator/health

# View API documentation
open http://localhost:8086/swagger-ui.html

# Test recommendation endpoint
curl http://localhost:8086/api/v1/recommendations/users/user123
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=RecommendationEngineTest

# Run tests with coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Run integration tests
mvn verify -P integration-tests

# Test with embedded MongoDB
mvn test -Dspring.profiles.active=test
```

### API Testing
```bash
# Test recommendation generation
curl -X POST http://localhost:8086/api/v1/recommendations/generate/user123

# Test behavior tracking
curl -X POST http://localhost:8086/api/v1/recommendations/behavior \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","productId":"prod456","behaviorType":"VIEW"}'

# Test similar products
curl http://localhost:8086/api/v1/recommendations/users/user123/products/prod456/similar
```

## Kafka Events

### Published Events
```yaml
# Recommendation Generated
topic: recommendation.generated
payload:
  userId: string
  recommendationId: string
  algorithmType: string
  score: number
  timestamp: string

# User Behavior Tracked
topic: user.behavior.tracked
payload:
  userId: string
  productId: string
  behaviorType: string
  sessionId: string
  timestamp: string
```

### Consumed Events
```yaml
# Order Completed
topic: order.completed
handler: updateUserPreferences()

# Product Updated
topic: product.updated
handler: recalculateProductSimilarity()

# User Profile Updated
topic: user.profile.updated
handler: refreshUserRecommendations()
```

## Machine Learning Models

### Collaborative Filtering
- **Algorithm**: Matrix Factorization (SVD)
- **Training**: Scheduled daily at 2 AM
- **Features**: User-item interactions, ratings, purchase history
- **Output**: User-product affinity scores

### Content-Based Filtering
- **Algorithm**: TF-IDF with Cosine Similarity
- **Features**: Product categories, brands, descriptions, prices
- **Training**: Real-time updates
- **Output**: Product similarity scores

### Hybrid Approach
- **Combination**: Weighted average of multiple algorithms
- **Weights**: Configurable via application properties
- **Fallback**: Trending products for cold start users

## Monitoring & Observability

### Health Checks
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  health:
    mongo:
      enabled: true
    redis:
      enabled: true
    kafka:
      enabled: true
```

### Custom Metrics
- `recommendation.generation.count`: Number of recommendations generated
- `recommendation.algorithm.performance`: Algorithm performance metrics
- `user.behavior.tracking.rate`: Behavior tracking rate
- `model.training.duration`: ML model training time
- `cache.hit.ratio`: Redis cache performance

### Logging
```yaml
logging:
  level:
    com.ecommerce.recommendation: INFO
    org.springframework.data.mongodb: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/recommendation-service.log
```

## Performance Optimization

### Caching Strategy
- **Redis Cache**: User recommendations (TTL: 1 hour)
- **Application Cache**: Product similarities (TTL: 6 hours)
- **Database Indexes**: Optimized for frequent queries

### Async Processing
- **Behavior Tracking**: Asynchronous event processing
- **Model Training**: Scheduled background jobs
- **Recommendation Generation**: Parallel algorithm execution

### Database Optimization
- **MongoDB Sharding**: User-based sharding strategy
- **Read Replicas**: Separate read/write operations
- **Connection Pooling**: Optimized connection management

## Error Handling

### Custom Exceptions
- `RecommendationException`: Base recommendation service exception
- `UserNotFoundException`: User not found in system
- `ModelTrainingException`: ML model training failures
- `AlgorithmException`: Algorithm execution errors

### Global Exception Handler
```java
@RestControllerAdvice
public class RecommendationExceptionHandler {
    // Handles all recommendation-related exceptions
    // Returns appropriate HTTP status codes and error messages
}
```

## Security Considerations

### Data Privacy
- **User Consent**: Explicit consent for behavior tracking
- **Data Anonymization**: Personal data anonymization
- **GDPR Compliance**: Right to be forgotten implementation

### API Security
- **Input Validation**: Comprehensive request validation
- **Rate Limiting**: API rate limiting per user
- **Authentication**: JWT token validation
- **Authorization**: Role-based access control

## Troubleshooting

### Common Issues

**MongoDB Connection Issues**
```bash
# Check MongoDB status
mongod --version
db.runCommand({connectionStatus: 1})

# Verify connection string
echo $MONGO_URI
```

**Redis Connection Issues**
```bash
# Test Redis connectivity
redis-cli ping
redis-cli info replication
```

**Kafka Issues**
```bash
# Check Kafka topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Monitor consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group recommendation-service
```

**Model Training Issues**
```bash
# Check model training logs
tail -f logs/recommendation-service.log | grep "ModelTraining"

# Verify model files
ls -la /models/recommendation/
```

### Performance Issues
```bash
# Monitor JVM metrics
curl http://localhost:8086/actuator/metrics/jvm.memory.used

# Check database performance
curl http://localhost:8086/actuator/metrics/mongodb.driver.pool.size

# Monitor cache performance
curl http://localhost:8086/actuator/metrics/cache.gets
```

## Deployment

### Docker
```dockerfile
# Build image
docker build -t recommendation-service:latest .

# Run container
docker run -d \
  --name recommendation-service \
  -p 8086:8086 \
  -e MONGO_URI=mongodb://mongo:27017/recommendation_db \
  -e REDIS_HOST=redis \
  recommendation-service:latest
```

### Docker Compose
```yaml
version: '3.8'
services:
  recommendation-service:
    build: .
    ports:
      - "8086:8086"
    environment:
      - MONGO_URI=mongodb://mongo:27017/recommendation_db
      - REDIS_HOST=redis
    depends_on:
      - mongo
      - redis
      - kafka
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: recommendation-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: recommendation-service
  template:
    spec:
      containers:
      - name: recommendation-service
        image: recommendation-service:latest
        ports:
        - containerPort: 8086
        env:
        - name: MONGO_URI
          value: "mongodb://mongo:27017/recommendation_db"
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java coding standards
- Write comprehensive unit tests
- Update API documentation
- Test ML model changes thoroughly
- Ensure backward compatibility

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Service Port**: 8086  
**Health Check**: http://localhost:8086/actuator/health  
**API Documentation**: http://localhost:8086/swagger-ui.html  
**Metrics**: http://localhost:8086/actuator/metrics