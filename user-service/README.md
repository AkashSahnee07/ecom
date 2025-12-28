# User Service

## Overview

The User Service is a core microservice in the e-commerce platform responsible for user management, authentication, and authorization. It provides secure user registration, login, profile management, and JWT-based authentication for the entire system.

## Features

- **User Registration & Management**: Complete user lifecycle management
- **Authentication & Authorization**: JWT-based secure authentication
- **Role-Based Access Control**: Support for CUSTOMER, ADMIN, and SELLER roles
- **Password Security**: BCrypt password encryption
- **Event-Driven Architecture**: Kafka integration for user events
- **Service Discovery**: Eureka client integration
- **Distributed Tracing**: Sleuth and Zipkin integration
- **Health Monitoring**: Actuator endpoints for monitoring

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Tracing**: Spring Cloud Sleuth + Zipkin
- **Build Tool**: Maven
- **Java Version**: 17+

## Architecture

### Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Key Components

1. **UserController**: REST API endpoints for user operations
2. **AuthController**: Authentication endpoints (login, logout, refresh)
3. **UserService**: Business logic for user management
4. **AuthService**: JWT token management and authentication logic
5. **SecurityConfig**: Spring Security configuration
6. **JwtAuthenticationFilter**: JWT token validation filter

## API Endpoints

### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users/register` | Register new user | No |
| GET | `/users/profile` | Get current user profile | Yes |
| PUT | `/users/profile` | Update user profile | Yes |
| GET | `/users/{id}` | Get user by ID | Admin only |
| GET | `/users` | Get all users (paginated) | Admin only |
| PUT | `/users/{id}/status` | Activate/deactivate user | Admin only |
| DELETE | `/users/{id}` | Delete user | Admin only |

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | User login | No |
| POST | `/auth/logout` | User logout | Yes |
| POST | `/auth/refresh` | Refresh JWT token | Yes |
| POST | `/auth/forgot-password` | Request password reset | No |
| POST | `/auth/reset-password` | Reset password | No |

### Request/Response Examples

#### User Registration

**Request:**
```json
POST /users/register
{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER"
}
```

**Response:**
```json
{
    "id": 1,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER",
    "isActive": true,
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 10:30:00"
}
```

#### User Login

**Request:**
```json
POST /auth/login
{
    "username": "johndoe",
    "password": "securePassword123"
}
```

**Response:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
        "id": 1,
        "username": "johndoe",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "CUSTOMER"
    }
}
```

## Configuration

### Application Properties

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/user_db
    username: ${DB_USERNAME:ecommerce}
    password: ${DB_PASSWORD:ecommerce123}
  jpa:
    hibernate:
      ddl-auto: update
  kafka:
    bootstrap-servers: localhost:9092

app:
  jwt:
    secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
    expiration: 86400000  # 24 hours
    refresh-expiration: 604800000  # 7 days

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Environment Variables

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `DB_USERNAME` | Database username | `ecommerce` |
| `DB_PASSWORD` | Database password | `ecommerce123` |
| `JWT_SECRET` | JWT signing secret | `mySecretKey123456789012345678901234567890` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `EUREKA_SERVER_URL` | Eureka server URL | `http://localhost:8761/eureka/` |

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)

## Setup Instructions

### 1. Database Setup

```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE user_db;
CREATE USER ecommerce WITH PASSWORD 'ecommerce123';
GRANT ALL PRIVILEGES ON DATABASE user_db TO ecommerce;
```

### 2. Environment Setup

```bash
# Set environment variables
export DB_USERNAME=ecommerce
export DB_PASSWORD=ecommerce123
export JWT_SECRET=your-super-secret-jwt-key-here
```

### 3. Build and Run

```bash
# Navigate to user-service directory
cd user-service

# Build the application
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/user-service-1.0.0.jar
```

### 4. Docker Setup (Optional)

```bash
# Build Docker image
docker build -t user-service:1.0.0 .

# Run with Docker
docker run -p 8081:8081 \
  -e DB_USERNAME=ecommerce \
  -e DB_PASSWORD=ecommerce123 \
  -e JWT_SECRET=your-jwt-secret \
  user-service:1.0.0
```

## Testing

### Unit Tests

```bash
# Run unit tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

### Integration Tests

```bash
# Run integration tests
mvn verify -P integration-tests
```

### API Testing with cURL

```bash
# Register a new user
curl -X POST http://localhost:8081/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# Get user profile (with JWT token)
curl -X GET http://localhost:8081/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Kafka Events

The service publishes the following events to Kafka:

### User Events Topic: `user-events`

```json
{
  "eventType": "USER_REGISTERED",
  "userId": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "role": "CUSTOMER",
  "timestamp": 1642248600000
}
```

**Event Types:**
- `USER_REGISTERED`: New user registration
- `USER_UPDATED`: User profile updated
- `USER_ACTIVATED`: User account activated
- `USER_DEACTIVATED`: User account deactivated
- `USER_DELETED`: User account deleted

## Monitoring and Health Checks

### Health Endpoints

- **Health Check**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`

### Logging

Logs are configured with different levels:
- `DEBUG`: Detailed application flow
- `INFO`: General application information
- `WARN`: Warning messages
- `ERROR`: Error conditions

Log files are stored in `logs/user-service.log`

## Security Considerations

1. **Password Security**: Passwords are encrypted using BCrypt
2. **JWT Security**: Tokens are signed with HMAC SHA-256
3. **CORS**: Configured for cross-origin requests
4. **Input Validation**: All inputs are validated using Bean Validation
5. **SQL Injection**: Protected by JPA/Hibernate parameterized queries
6. **Rate Limiting**: Consider implementing rate limiting for production

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```bash
   # Check PostgreSQL status
   sudo systemctl status postgresql
   
   # Check database connectivity
   psql -h localhost -U ecommerce -d user_db
   ```

2. **Kafka Connection Issues**
   ```bash
   # Check Kafka status
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   ```

3. **Eureka Registration Issues**
   ```bash
   # Check Eureka dashboard
   curl http://localhost:8761/eureka/apps
   ```

4. **JWT Token Issues**
   - Ensure JWT secret is properly configured
   - Check token expiration settings
   - Verify token format and signature

### Debug Mode

```bash
# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.ecommerce.user=DEBUG"
```

## Performance Tuning

### Database Optimization

1. **Indexes**: Add indexes on frequently queried columns
   ```sql
   CREATE INDEX idx_users_username ON users(username);
   CREATE INDEX idx_users_email ON users(email);
   CREATE INDEX idx_users_role ON users(role);
   ```

2. **Connection Pooling**: Configure HikariCP settings
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
         connection-timeout: 30000
   ```

### JVM Tuning

```bash
# Production JVM settings
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar user-service.jar
```

## Contributing

1. Follow the existing code style and conventions
2. Write unit tests for new features
3. Update documentation for API changes
4. Ensure all tests pass before submitting
5. Use meaningful commit messages

## License

This project is part of the E-commerce Microservices Platform.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review application logs
3. Contact the development team

---

**Service Status**: ✅ Active  
**Last Updated**: January 2024  
**Version**: 1.0.0