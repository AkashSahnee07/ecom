# 🚀 Quick Start Guide

This guide will help you quickly get the e-commerce microservices platform up and running.

## Prerequisites

- **Java 17+** - Download from [OpenJDK](https://openjdk.org/)
- **Maven 3.8+** - Download from [Apache Maven](https://maven.apache.org/)
- **Docker & Docker Compose** - Download from [Docker](https://www.docker.com/)
- **Git** - For version control

## Quick Start (Recommended)

### 1. Start Infrastructure Services
```bash
# Start databases, Kafka, Redis, Zipkin, etc.
docker-compose up -d

# Wait for services to be ready (about 2-3 minutes)
docker-compose logs -f
```

### 2. Start All Microservices
```bash
# Use the automated startup script
./start-services.sh
```

### 3. Verify Services
Once all services are running, you can access:

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Zipkin Tracing**: http://localhost:9411
- **Kibana (Logs)**: http://localhost:5601

## Manual Start (Alternative)

If you prefer to start services manually:

### 1. Start Infrastructure
```bash
docker-compose up -d postgres mysql redis kafka zookeeper zipkin
```

### 2. Build All Services
```bash
mvn clean install -DskipTests
```

### 3. Start Services in Order
```bash
# 1. Service Discovery
cd eureka-server && mvn spring-boot:run &

# 2. Configuration Server
cd config-server && mvn spring-boot:run &

# 3. API Gateway
cd api-gateway && mvn spring-boot:run &

# 4. Business Services (wait 30s between each)
cd user-service && mvn spring-boot:run &
cd product-service && mvn spring-boot:run &
cd cart-service && mvn spring-boot:run &
# ... continue with other services
```

## Testing the Platform

### 1. Health Checks
```bash
# Check all services are healthy
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
```

### 2. API Testing
```bash
# Register a new user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Get products
curl http://localhost:8080/api/products

# Add item to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2
  }'
```

### 3. View API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Individual Services**: http://localhost:808X/swagger-ui.html (replace X with service port)

## Monitoring & Debugging

### View Logs
```bash
# Service logs (if using start script)
tail -f user-service.log
tail -f product-service.log

# Docker logs
docker-compose logs -f postgres
docker-compose logs -f kafka
```

### Distributed Tracing
1. Open Zipkin: http://localhost:9411
2. Make some API calls
3. View traces to see request flow across services

### Database Access
```bash
# PostgreSQL
docker exec -it ecom-postgres psql -U ecommerce -d user_db

# MySQL
docker exec -it ecom-mysql mysql -u product_user -p product_db

# Redis
docker exec -it ecom-redis redis-cli
```

## Stopping Services

### Stop All Services
```bash
./stop-services.sh
```

### Stop Infrastructure Only
```bash
docker-compose down
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Find process using port
   lsof -i :8080
   # Kill process
   kill -9 <PID>
   ```

2. **Database Connection Issues**
   ```bash
   # Check if databases are running
   docker-compose ps
   # Restart databases
   docker-compose restart postgres mysql
   ```

3. **Service Discovery Issues**
   - Ensure Eureka Server is running first
   - Check Eureka Dashboard: http://localhost:8761
   - Wait 30-60 seconds for services to register

4. **Memory Issues**
   ```bash
   # Increase Docker memory limit
   # Or run fewer services simultaneously
   ```

### Performance Tips

1. **Increase JVM Memory**
   ```bash
   export MAVEN_OPTS="-Xmx2g -Xms1g"
   ```

2. **Skip Tests During Build**
   ```bash
   mvn clean install -DskipTests
   ```

3. **Use Parallel Builds**
   ```bash
   mvn clean install -T 4  # Use 4 threads
   ```

## Development Workflow

1. **Make Changes** to your service
2. **Rebuild** the specific service:
   ```bash
   cd your-service
   mvn clean install -DskipTests
   ```
3. **Restart** the service:
   ```bash
   # Kill the service process
   kill $(cat your-service.pid)
   # Start it again
   mvn spring-boot:run &
   ```

## Next Steps

- Explore the API documentation
- Check out the monitoring dashboards
- Review the service logs
- Try the sample API calls
- Customize the configuration for your needs

## Support

If you encounter issues:
1. Check the service logs
2. Verify all prerequisites are installed
3. Ensure Docker services are healthy
4. Review the troubleshooting section above