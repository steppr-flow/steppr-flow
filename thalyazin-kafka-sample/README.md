# Thalyazin Sample Application

A sample Spring Boot application demonstrating the Thalyazin workflow orchestration framework.

## Overview

This sample implements an order processing workflow with 5 steps:

1. **Validate Order** - Validates order data and business rules
2. **Reserve Inventory** - Reserves products in inventory
3. **Process Payment** - Charges customer payment method
4. **Create Shipment** - Creates shipping label
5. **Send Confirmation** - Sends order confirmation email

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose

## Quick Start

### 1. Start Kafka

```bash
cd thalyazin-sample
docker-compose up -d
```

Wait for Kafka to be ready (about 30 seconds).

Kafka UI is available at: http://localhost:8090

### 2. Build and Run

```bash
# From project root
mvn clean install -DskipTests

# Run the sample
cd thalyazin-sample
mvn spring-boot:run
```

### 3. Create an Order

```bash
curl -X POST http://localhost:8010/api/orders \
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

Response:
```json
{
  "orderId": "ORD-A1B2C3D4",
  "executionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PROCESSING",
  "message": "Order received and processing started"
}
```

### 4. Watch the Logs

You'll see the workflow executing step by step:

```
[Step 1] Validating order: ORD-A1B2C3D4
[Step 1] Order ORD-A1B2C3D4 validated successfully
[Step 2] Reserving inventory for order: ORD-A1B2C3D4
[Step 2] Reserved 2 units of product PROD-001
[Step 2] Reserved 1 units of product PROD-002
[Step 3] Processing payment for order: ORD-A1B2C3D4 (amount: 109.97)
[Step 3] Payment processed for order ORD-A1B2C3D4 (transaction: TXN-12345678)
[Step 4] Creating shipment for order: ORD-A1B2C3D4
[Step 4] Shipment created for order ORD-A1B2C3D4 (tracking: TRACK-ABCDEF1234)
[Step 5] Sending confirmation for order: ORD-A1B2C3D4
[Step 5] Confirmation sent for order ORD-A1B2C3D4
=== ORDER WORKFLOW COMPLETED ===
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create a new order |
| POST | `/api/orders/{executionId}/resume` | Resume a failed workflow |
| DELETE | `/api/orders/{executionId}` | Cancel an order |

## Testing Failures

The PaymentService has a 10% chance of failing to simulate real-world conditions. When a payment fails:

1. The workflow stops at step 3
2. `@OnFailure` callback is triggered
3. You can see the error in logs

To resume after fixing the issue:

```bash
curl -X POST "http://localhost:8080/api/orders/{executionId}/resume?fromStep=3"
```

## Testing Inventory Issues

Use `PROD-004` which is out of stock:

```bash
curl -X POST http://localhost:8010/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": "PROD-004",
        "productName": "Out of Stock Item",
        "quantity": 1,
        "price": 99.99
      }
    ],
    "payment": { "cardLast4": "4242", "cardType": "VISA" },
    "shipping": {
      "street": "123 Main St",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    }
  }'
```

## Cleanup

```bash
docker-compose down -v
```