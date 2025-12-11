# Thalyazin RabbitMQ Sample

Sample application demonstrating Thalyazin with RabbitMQ.

## Overview

This sample shows how to build a complete order processing workflow using Thalyazin with RabbitMQ as the message broker.

## Running the Sample

### Prerequisites

- Java 21+
- Docker & Docker Compose (for RabbitMQ)

### 1. Start Infrastructure

```bash
# Start RabbitMQ
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3.13-management-alpine
```

### 2. Run the Application

```bash
cd thalyazin-rabbitmq-sample
mvn spring-boot:run
```

### 3. Test the Workflow

```bash
# Start a new order workflow
curl -X POST http://localhost:8011/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": "PROD-001",
        "productName": "Wireless Mouse",
        "quantity": 2,
        "price": 29.99
      },
      {
        "productId": "PROD-002",
        "productName": "USB Keyboard",
        "quantity": 1,
        "price": 49.99
      }
    ],
    "payment": {
      "cardLast4": "4242",
      "cardType": "VISA"
    },
    "shipping": {
      "street": "123 Main Street",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    }
  }'
```

## Project Structure

```
thalyazin-rabbitmq-sample/
├── src/main/java/
│   └── io/thalyazin/sample/
│       ├── SampleApplication.java
│       ├── workflow/
│       │   └── OrderWorkflow.java
│       ├── controller/
│       │   └── OrderController.java
│       └── model/
│           └── OrderPayload.java
└── src/main/resources/
    └── application.yml
```

## Configuration

```yaml
thalyazin:
  enabled: true
  broker: rabbitmq
  rabbitmq:
    exchange: thalyazin-sample

spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

## RabbitMQ Management

Access the management UI at `http://localhost:15672` (guest/guest) to:
- View queues and exchanges
- Monitor message rates
- Inspect dead letter queues

## Workflow Steps

1. **Validate Order** - Check order validity
2. **Reserve Inventory** - Reserve items
3. **Process Payment** - Charge customer
4. **Send Confirmation** - Notify customer
