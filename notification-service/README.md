# Notification Service

## Overview

The Notification Service is a comprehensive microservice in the e-commerce platform responsible for managing all types of notifications across multiple channels. It provides email, SMS, and push notification capabilities with template management, delivery tracking, and event-driven processing. The service ensures reliable communication with customers throughout their shopping journey.

## Features

### Core Functionality
- **Multi-Channel Notifications**: Email, SMS, and Push notifications
- **Template Management**: Dynamic template processing with variables
- **Event-Driven Processing**: Kafka-based event consumption from other services
- **Delivery Tracking**: Complete notification delivery status monitoring
- **Notification History**: Comprehensive audit trail of all notifications
- **Scheduling**: Support for scheduled and delayed notifications

### Advanced Features
- **Priority Management**: Critical, High, Medium, Low priority levels
- **Duplicate Prevention**: Intelligent duplicate notification detection
- **Retry Logic**: Automatic retry for failed deliveries
- **Batch Processing**: Efficient bulk notification processing
- **User Preferences**: Respect user notification preferences
- **Analytics & Reporting**: Delivery statistics and performance metrics

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Messaging**: Apache Kafka
- **Email**: Spring Mail with SMTP
- **SMS**: Twilio API
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config
- **Build Tool**: Maven
- **Java Version**: 17+

## Database Schema

### Notifications Table
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id VARCHAR(255) NOT NULL,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(20),
    recipient_device_token VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(500),
    content TEXT NOT NULL,
    template_id VARCHAR(100),
    template_variables JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    correlation_id VARCHAR(255),
    source_service VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Notification Templates Table
```sql
CREATE TABLE notification_templates (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject_template VARCHAR(500),
    content_template TEXT NOT NULL,
    variables JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Key Components

### NotificationService
- **Location**: `com.ecommerce.notification.service.NotificationService`
- **Purpose**: Core business logic for notification management
- **Key Methods**:
  - `createNotification()`: Create and send notifications
  - `scheduleNotification()`: Schedule future notifications
  - `sendNotificationAsync()`: Asynchronous notification sending
  - `getNotificationHistory()`: Retrieve notification history

### EmailNotificationService
- **Location**: `com.ecommerce.notification.service.EmailNotificationService`
- **Purpose**: Handle email notification delivery
- **Features**: HTML/Plain text support, attachment handling

### SmsNotificationService
- **Location**: `com.ecommerce.notification.service.SmsNotificationService`
- **Purpose**: Handle SMS notification delivery via Twilio
- **Features**: International SMS support, delivery receipts

### PushNotificationService
- **Location**: `com.ecommerce.notification.service.PushNotificationService`
- **Purpose**: Handle push notifications via Firebase FCM
- **Features**: iOS/Android support, rich notifications

### NotificationEventListener
- **Location**: `com.ecommerce.notification.listener.NotificationEventListener`
- **Purpose**: Process Kafka events from other microservices
- **Events**: Order events, payment events, user events

## API Endpoints

### Notification Management
```http
POST   /api/notifications                    # Send notification
GET    /api/notifications/{id}               # Get notification by ID
GET    /api/notifications/user/{userId}      # Get user's notifications
PUT    /api/notifications/{id}/read          # Mark as read
DELETE /api/notifications/{id}               # Delete notification
```

### Bulk Operations
```http
POST   /api/notifications/bulk               # Send bulk notifications
PUT    /api/notifications/bulk/read          # Mark multiple as read
DELETE /api/notifications/bulk               # Delete multiple notifications
```

### Templates
```http
GET    /api/notifications/templates          # Get all templates
GET    /api/notifications/templates/{id}     # Get template by ID
POST   /api/notifications/templates          # Create template
PUT    /api/notifications/templates/{id}     # Update template
DELETE /api/notifications/templates/{id}     # Delete template
```

### Analytics
```http
GET    /api/notifications/analytics          # Get notification analytics
GET    /api/notifications/delivery-stats     # Get delivery statistics
```

## Configuration

### Application Properties
```yaml
server:
  port: 8087

spring:
  application:
    name: notification-service
  datasource:
    url: jdbc:postgresql://localhost:5432/notification_db
    username: postgres
    password: password
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service

notification:
  email:
    enabled: true
    from-address: ${EMAIL_FROM_ADDRESS}
  sms:
    enabled: true
    twilio:
      account-sid: ${TWILIO_ACCOUNT_SID}
      auth-token: ${TWILIO_AUTH_TOKEN}
      from-number: ${TWILIO_FROM_NUMBER}
  push:
    enabled: true
    firebase:
      project-id: ${FIREBASE_PROJECT_ID}
      credentials-path: ${FIREBASE_CREDENTIALS_PATH}
```

### Environment Variables
- `EMAIL_USERNAME`: SMTP username
- `EMAIL_PASSWORD`: SMTP password
- `EMAIL_FROM_ADDRESS`: Default sender email
- `TWILIO_ACCOUNT_SID`: Twilio account SID
- `TWILIO_AUTH_TOKEN`: Twilio auth token
- `TWILIO_FROM_NUMBER`: Twilio phone number
- `FIREBASE_PROJECT_ID`: Firebase project ID
- `FIREBASE_CREDENTIALS_PATH`: Path to Firebase credentials JSON

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+
- SMTP Server (Gmail, SendGrid, etc.)
- Twilio Account (for SMS)
- Firebase Project (for Push notifications)
- Eureka Server (for service discovery)

## Setup Instructions

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE notification_db;

-- Create user (optional)
CREATE USER notification_user WITH PASSWORD 'notification_pass';
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification_user;
```

### 2. Email Configuration

#### Gmail SMTP Setup
1. Enable 2-factor authentication on Gmail
2. Generate App Password for the application
3. Use App Password as EMAIL_PASSWORD

#### SendGrid Setup
1. Create SendGrid account
2. Generate API key
3. Configure SMTP settings

### 3. SMS Configuration (Twilio)
1. Create Twilio account at https://twilio.com
2. Get Account SID and Auth Token
3. Purchase phone number for SMS sending
4. Set up webhook for delivery receipts

### 4. Push Notification Setup (Firebase)
1. Create Firebase project at https://console.firebase.google.com
2. Enable Cloud Messaging
3. Download service account credentials JSON
4. Configure FCM settings

### 5. Environment Configuration
```bash
# Set environment variables
export EMAIL_USERNAME=your-email@gmail.com
export EMAIL_PASSWORD=your-app-password
export EMAIL_FROM_ADDRESS=noreply@yourcompany.com
export TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxx
export TWILIO_AUTH_TOKEN=your-auth-token
export TWILIO_FROM_NUMBER=+1234567890
export FIREBASE_PROJECT_ID=your-project-id
export FIREBASE_CREDENTIALS_PATH=/path/to/firebase-credentials.json
```

### 6. Build and Run
```bash
# Navigate to notification-service directory
cd notification-service

# Build the application
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

### 7. Verify Installation
```bash
# Check health endpoint
curl http://localhost:8087/actuator/health

# Test email notification
curl -X POST http://localhost:8087/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipientId": "user123",
    "recipientEmail": "test@example.com",
    "type": "ORDER_CONFIRMATION",
    "channel": "EMAIL",
    "subject": "Order Confirmed",
    "content": "Your order has been confirmed!"
  }'
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=NotificationServiceTest
```

### Integration Tests
```bash
# Run integration tests
mvn test -Dtest=NotificationServiceApplicationTests
```

### Manual Testing

#### Email Testing
```bash
# Test email delivery
curl -X POST http://localhost:8087/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipientEmail": "test@example.com",
    "type": "TEST",
    "channel": "EMAIL",
    "subject": "Test Email",
    "content": "This is a test email."
  }'
```

#### SMS Testing
```bash
# Test SMS delivery
curl -X POST http://localhost:8087/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipientPhone": "+1234567890",
    "type": "TEST",
    "channel": "SMS",
    "content": "This is a test SMS."
  }'
```

## Kafka Events

### Consumed Events
- **order.created**: Order confirmation notifications
- **order.shipped**: Shipping notifications
- **order.delivered**: Delivery notifications
- **payment.completed**: Payment confirmation
- **payment.failed**: Payment failure alerts
- **user.registered**: Welcome notifications

### Published Events
- **notification.sent**: When notification is successfully sent
- **notification.failed**: When notification delivery fails
- **notification.delivered**: When delivery is confirmed

### Event Processing
```java
@KafkaListener(topics = "order-events")
public void handleOrderEvent(OrderEvent event) {
    switch (event.getEventType()) {
        case "ORDER_CREATED":
            sendOrderConfirmation(event);
            break;
        case "ORDER_SHIPPED":
            sendShippingNotification(event);
            break;
    }
}
```

## Notification Templates

### Template Variables
Templates support dynamic variables using `{{variable}}` syntax:

```html
<!-- Order Confirmation Template -->
<h1>Order Confirmed</h1>
<p>Dear {{customerName}},</p>
<p>Your order {{orderNumber}} has been confirmed.</p>
<p>Total Amount: {{totalAmount}}</p>
<p>Estimated Delivery: {{deliveryDate}}</p>
```

### Template Management
```bash
# Create template
curl -X POST http://localhost:8087/api/notifications/templates \
  -H "Content-Type: application/json" \
  -d '{
    "id": "order-confirmation",
    "name": "Order Confirmation",
    "type": "ORDER_CONFIRMATION",
    "channel": "EMAIL",
    "subjectTemplate": "Order {{orderNumber}} Confirmed",
    "contentTemplate": "Dear {{customerName}}, your order has been confirmed."
  }'
```

## Monitoring

### Health Checks
- **Endpoint**: `/actuator/health`
- **Database**: Connection and query health
- **Email**: SMTP server connectivity
- **SMS**: Twilio API connectivity
- **Push**: Firebase FCM connectivity
- **Kafka**: Consumer and producer health

### Metrics
- **Delivery Rates**: Success/failure rates by channel
- **Response Times**: Notification processing latency
- **Queue Depth**: Pending notification count
- **Error Rates**: Failed delivery statistics

### Custom Metrics
```java
@Component
public class NotificationMetrics {
    private final Counter emailsSent = Counter.builder("notifications.emails.sent").register(meterRegistry);
    private final Counter smsSent = Counter.builder("notifications.sms.sent").register(meterRegistry);
    private final Timer processingTime = Timer.builder("notifications.processing.time").register(meterRegistry);
}
```

## Performance Optimization

### Async Processing
- **Email Sending**: Asynchronous email delivery
- **SMS Sending**: Async SMS processing
- **Push Notifications**: Batch push notification sending
- **Event Processing**: Async Kafka event handling

### Batch Operations
```java
@Async
public CompletableFuture<Void> sendBulkNotifications(List<Notification> notifications) {
    // Process notifications in batches
    notifications.parallelStream()
        .forEach(this::sendNotification);
    return CompletableFuture.completedFuture(null);
}
```

### Caching Strategy
- **Templates**: Cache frequently used templates
- **User Preferences**: Cache notification preferences
- **Delivery Status**: Cache recent delivery statuses

## Error Handling

### Retry Logic
```java
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public void sendEmailWithRetry(Notification notification) {
    // Email sending logic with automatic retry
}
```

### Dead Letter Queue
- Failed notifications moved to DLQ after max retries
- Manual processing of failed notifications
- Error analysis and resolution

## Security

### Data Protection
- **Encryption**: Sensitive data encrypted at rest
- **PII Handling**: Proper handling of personal information
- **Access Control**: Role-based access to notification data

### API Security
- **Authentication**: JWT token validation
- **Rate Limiting**: Prevent notification spam
- **Input Validation**: Validate all notification requests

## Troubleshooting

### Common Issues

1. **Email Delivery Failed**
   ```bash
   # Check SMTP configuration
   telnet smtp.gmail.com 587
   
   # Verify credentials
   curl -u "username:password" --url "smtps://smtp.gmail.com:465"
   ```

2. **SMS Delivery Failed**
   ```bash
   # Test Twilio API
   curl -X POST https://api.twilio.com/2010-04-01/Accounts/$TWILIO_ACCOUNT_SID/Messages.json \
     --data-urlencode "From=$TWILIO_FROM_NUMBER" \
     --data-urlencode "Body=Test message" \
     --data-urlencode "To=+1234567890" \
     -u $TWILIO_ACCOUNT_SID:$TWILIO_AUTH_TOKEN
   ```

3. **Push Notification Failed**
   ```bash
   # Verify Firebase credentials
   firebase projects:list
   
   # Test FCM connectivity
   curl -X POST https://fcm.googleapis.com/fcm/send \
     -H "Authorization: key=$FCM_SERVER_KEY" \
     -H "Content-Type: application/json"
   ```

### Performance Issues
- **Slow Email Delivery**: Check SMTP server performance
- **High Memory Usage**: Monitor notification queue size
- **Database Locks**: Optimize notification queries

## Contributing

1. **Testing**: Test all notification channels thoroughly
2. **Templates**: Follow template naming conventions
3. **Error Handling**: Implement proper error handling
4. **Documentation**: Update API documentation

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For notification service issues:
- **Email Issues**: Check SMTP server logs
- **SMS Issues**: Check Twilio console
- **Push Issues**: Check Firebase console
- **Performance**: Monitor application metrics