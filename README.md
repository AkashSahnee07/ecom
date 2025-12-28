# E-commerce Microservices Platform

A comprehensive e-commerce platform built with Spring Boot microservices architecture, featuring intelligent product recommendations, real-time user behavior tracking, and complete business functionality.

## 🏗️ Architecture Overview

### Infrastructure Services
- **Eureka Server** (Port 8761) - Service Discovery
- **API Gateway** (Port 8080) - Centralized routing and load balancing
- **Config Server** (Port 8888) - Centralized configuration management
- **Zipkin** (Port 9411) - Distributed tracing

### Business Services
- **User Service** (Port 8081) - User management and authentication
- **Product Service** (Port 8082) - Product catalog management
- **Cart Service** (Port 8083) - Shopping cart functionality
- **Order Service** (Port 8084) - Order processing
- **Payment Service** (Port 8085) - Payment processing
- **Inventory Service** (Port 8086) - Stock management
- **Recommendation Service** (Port 8087) - AI-powered recommendations
- **Shipping Service** (Port 8088) - Delivery management
- **Review Service** (Port 8089) - Product reviews and ratings
- **Notification Service** (Port 8090) - Messaging and notifications

## 🚀 Prerequisites

### Required Software
- **Java 17** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **PostgreSQL 13+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **Apache Kafka 2.8+**

### Optional (for development)
- **IntelliJ IDEA** or **VS Code**
- **Postman** for API testing
- **pgAdmin** for PostgreSQL management
- **MySQL Workbench** for MySQL management

## 🛠️ Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd ecom
```

### 2. Start Infrastructure Services with Docker

#### Option A: Using Docker Compose (Recommended)
```bash
# Start all infrastructure services
docker-compose -f docker-compose-zipkin.yml up -d

# This will start:
# - PostgreSQL (Port 5432)
# - MySQL (Port 3306)
# - Redis (Port 6379)
# - Kafka & Zookeeper (Ports 9092, 2181)
# - Zipkin (Port 9411)
```

#### Option B: Manual Setup
```bash
# PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_DB=ecommerce \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:13

# MySQL
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=product_db \
  -p 3306:3306 mysql:8.0

# Redis
docker run -d --name redis -p 6379:6379 redis:6-alpine

# Kafka (with Zookeeper)
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest \
  -e ZOOKEEPER_CLIENT_PORT=2181

docker run -d --name kafka -p 9092:9092 \
  --link zookeeper:zookeeper \
  confluentinc/cp-kafka:latest \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1

# Zipkin
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin
```

### 3. Build All Services
```bash
# Build all microservices
mvn clean install -DskipTests

# Or build individual services
cd eureka-server && mvn clean install -DskipTests && cd ..
cd config-server && mvn clean install -DskipTests && cd ..
# ... repeat for other services
```

## 🏃‍♂️ Running the Platform

### Method 1: Using Maven (Development)

#### Start services in the following order:

1. **Start Eureka Server** (Service Discovery)
```bash
cd eureka-server
mvn spring-boot:run
# Wait for startup, then verify at http://localhost:8761
```

2. **Start Config Server** (Configuration Management)
```bash
cd config-server
mvn spring-boot:run
# Verify at http://localhost:8888/actuator/health
```

3. **Start API Gateway** (Routing)
```bash
cd api-gateway
mvn spring-boot:run
# Verify at http://localhost:8080/actuator/health
```

4. **Start Business Services** (can be started in parallel)
```bash
# Terminal 1
cd user-service && mvn spring-boot:run

# Terminal 2
cd product-service && mvn spring-boot:run

# Terminal 3
cd cart-service && mvn spring-boot:run

# Terminal 4
cd order-service && mvn spring-boot:run

# Terminal 5
cd payment-service && mvn spring-boot:run

# Terminal 6
cd inventory-service && mvn spring-boot:run

# Terminal 7
cd recommendation-service && mvn spring-boot:run

# Terminal 8
cd shipping-service && mvn spring-boot:run

# Terminal 9
cd review-service && mvn spring-boot:run

# Terminal 10
cd notification-service && mvn spring-boot:run
```

### Method 2: Using Docker (Production)

#### Build Docker images for all services:
```bash
# Build all Docker images
for service in eureka-server config-server api-gateway user-service product-service cart-service order-service payment-service inventory-service recommendation-service shipping-service review-service notification-service; do
  cd $service
  docker build -t ecommerce/$service:latest .
  cd ..
done
```

#### Run with Docker Compose:
```bash
# Create a docker-compose.yml for all services and run
docker-compose up -d
```

### Method 3: Using JAR Files
```bash
# After building with Maven, run JAR files
java -jar eureka-server/target/eureka-server-1.0.0.jar &
java -jar config-server/target/config-server-1.0.0.jar &
java -jar api-gateway/target/api-gateway-1.0.0.jar &
# ... continue for other services
```

## 🔍 Verification and Testing

### 1. Check Service Registration
Visit Eureka Dashboard: http://localhost:8761
- All services should be registered and show as "UP"

### 2. Health Checks
```bash
# Check individual service health
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Cart Service
# ... check other services
```

### 3. API Gateway Routing
```bash
# Test routing through API Gateway
curl http://localhost:8080/user-service/actuator/health
curl http://localhost:8080/product-service/actuator/health
curl http://localhost:8080/recommendation-service/actuator/health
```

### 4. API Documentation
- **API Gateway Swagger**: http://localhost:8080/swagger-ui.html
- **User Service**: http://localhost:8081/swagger-ui.html
- **Product Service**: http://localhost:8082/swagger-ui.html
- **Recommendation Service**: http://localhost:8087/swagger-ui.html
- **Review Service**: http://localhost:8089/swagger-ui.html

### 5. Monitoring and Tracing
- **Zipkin Tracing**: http://localhost:9411
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus

## 🧪 Sample API Calls

### User Management
```bash
# Register a new user
curl -X POST http://localhost:8080/user-service/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","firstName":"John","lastName":"Doe"}'

# Login
curl -X POST http://localhost:8080/user-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Product Management
```bash
# Get all products
curl http://localhost:8080/product-service/api/products

# Search products
curl "http://localhost:8080/product-service/api/products/search?query=laptop&category=electronics"
```

### Recommendations
```bash
# Get personalized recommendations
curl "http://localhost:8080/recommendation-service/api/recommendations/personalized/1?limit=10"

# Get similar products
curl "http://localhost:8080/recommendation-service/api/recommendations/similar/1?limit=5"

# Track user behavior
curl -X POST http://localhost:8080/recommendation-service/api/recommendations/behavior \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"actionType":"VIEW","sessionId":"session123"}'
```

### Shopping Cart
```bash
# Add item to cart
curl -X POST http://localhost:8080/cart-service/api/cart/1/items \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# Get cart
curl http://localhost:8080/cart-service/api/cart/1
```

### Orders
```bash
# Create order
curl -X POST http://localhost:8080/order-service/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"items":[{"productId":1,"quantity":2,"price":99.99}]}'
```

## 🐛 Troubleshooting

### Common Issues

1. **Service Registration Issues**
   - Ensure Eureka Server is running first
   - Check network connectivity
   - Verify application.yml configurations

2. **Database Connection Issues**
   - Verify database containers are running
   - Check connection strings and credentials
   - Ensure databases are created

3. **Kafka Connection Issues**
   - Verify Kafka and Zookeeper are running
   - Check Kafka broker configuration
   - Ensure topics are created

4. **Memory Issues**
   - Increase JVM heap size: `-Xmx1024m`
   - Monitor memory usage with `docker stats`

### Logs
```bash
# View service logs
docker logs <container-name>

# Follow logs in real-time
docker logs -f <container-name>

# View application logs (if running with Maven)
tail -f <service-name>/logs/<service-name>.log
```

## 📊 Performance Tuning

### JVM Options
```bash
# For production deployment
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"
```

### Database Optimization
- Configure connection pools appropriately
- Enable query caching
- Use database indexes for frequently queried fields

### Caching Strategy
- Redis for session storage and frequently accessed data
- EhCache for application-level caching
- HTTP caching for static resources

## 🔒 Security Considerations

- Change default passwords in production
- Use environment variables for sensitive configuration
- Enable HTTPS in production
- Implement proper authentication and authorization
- Regular security updates for dependencies

## 📈 Scaling

### Horizontal Scaling
```bash
# Scale specific services
docker-compose up -d --scale user-service=3
docker-compose up -d --scale product-service=2
docker-compose up -d --scale recommendation-service=2
```

### Load Balancing
- API Gateway provides client-side load balancing
- Use external load balancer (nginx, HAProxy) for production

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.