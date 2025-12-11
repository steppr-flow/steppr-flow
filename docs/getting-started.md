# Getting Started with Thalyazin

This guide will help you set up your first async workflow with Thalyazin.

## Prerequisites

- Java 21 or later
- Maven 3.8+
- Docker (for running Kafka or RabbitMQ)

## Step 1: Create a Spring Boot Project

Create a new Spring Boot project or add Thalyazin to an existing one.

## Step 2: Add Dependencies

Add the following to your `pom.xml`:

```xml
<properties>
    <thalyazin.version>1.0.0-SNAPSHOT</thalyazin.version>
</properties>

<dependencies>
    <!-- Thalyazin with Kafka -->
    <dependency>
        <groupId>io.thalyazin</groupId>
        <artifactId>thalyazin-broker-kafka</artifactId>
        <version>${thalyazin.version}</version>
    </dependency>

    <!-- Or Thalyazin with RabbitMQ -->
    <!--
    <dependency>
        <groupId>io.thalyazin</groupId>
        <artifactId>thalyazin-broker-rabbitmq</artifactId>
        <version>${thalyazin.version}</version>
    </dependency>
    -->
</dependencies>
```

## Step 3: Configure Your Application

Create `application.yml`:

```yaml
spring:
  application:
    name: my-thalyazin-app

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ${spring.application.name}-workers
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

thalyazin:
  broker:
    type: kafka
```

## Step 4: Create Your Payload

Define the data that will flow through your workflow:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayload {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private PaymentInfo paymentInfo;
    private ShippingAddress shippingAddress;
}
```

## Step 5: Create Your Workflow

Create a workflow class with steps:

```java
@Slf4j
@Service
@Topic("order-workflow")
public class OrderWorkflow implements Thalyazin {

    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    public OrderWorkflow(
            InventoryService inventoryService,
            PaymentService paymentService,
            ShippingService shippingService,
            NotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.notificationService = notificationService;
    }

    @Step(id = 1, label = "Validate Order")
    public void validateOrder(OrderPayload payload) {
        log.info("Validating order: {}", payload.getOrderId());

        if (payload.getItems() == null || payload.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        if (payload.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
    }

    @Step(id = 2, label = "Reserve Inventory")
    public void reserveInventory(OrderPayload payload) {
        log.info("Reserving inventory for order: {}", payload.getOrderId());

        for (OrderItem item : payload.getItems()) {
            boolean reserved = inventoryService.reserve(
                item.getProductId(),
                item.getQuantity()
            );

            if (!reserved) {
                throw new RuntimeException(
                    "Insufficient inventory for product: " + item.getProductId()
                );
            }
        }
    }

    @Step(id = 3, label = "Process Payment")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void processPayment(OrderPayload payload) {
        log.info("Processing payment for order: {}", payload.getOrderId());

        PaymentResult result = paymentService.charge(
            payload.getPaymentInfo(),
            payload.getTotalAmount()
        );

        if (!result.isSuccess()) {
            throw new RuntimeException("Payment failed: " + result.getErrorMessage());
        }
    }

    @Step(id = 4, label = "Create Shipment")
    public void createShipment(OrderPayload payload) {
        log.info("Creating shipment for order: {}", payload.getOrderId());

        shippingService.createShipment(
            payload.getOrderId(),
            payload.getItems(),
            payload.getShippingAddress()
        );
    }

    @Step(id = 5, label = "Send Confirmation")
    public void sendConfirmation(OrderPayload payload) {
        log.info("Sending confirmation for order: {}", payload.getOrderId());

        notificationService.sendOrderConfirmation(
            payload.getCustomerId(),
            payload.getOrderId()
        );
    }

    @OnSuccess
    public void onComplete(WorkflowMessage message) {
        log.info("Order workflow completed successfully: {}", message.getExecutionId());
    }

    @OnFailure
    public void onFailed(WorkflowMessage message) {
        log.error("Order workflow failed: {} - Error: {}",
            message.getExecutionId(),
            message.getErrorInfo() != null ? message.getErrorInfo().getMessage() : "Unknown"
        );

        // Compensate: release inventory, refund payment, etc.
    }
}
```

## Step 6: Start Workflows

Inject `WorkflowStarter` and start workflows:

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final WorkflowStarter workflowStarter;

    public OrderController(WorkflowStarter workflowStarter) {
        this.workflowStarter = workflowStarter;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // Build payload
        OrderPayload payload = OrderPayload.builder()
            .orderId(UUID.randomUUID().toString())
            .customerId(request.getCustomerId())
            .items(request.getItems())
            .totalAmount(calculateTotal(request.getItems()))
            .paymentInfo(request.getPaymentInfo())
            .shippingAddress(request.getShippingAddress())
            .build();

        // Start workflow
        String executionId = workflowStarter.start("order-workflow", payload);

        return ResponseEntity.accepted()
            .body(new OrderResponse(payload.getOrderId(), executionId, "PROCESSING"));
    }

    @PostMapping("/{executionId}/resume")
    public ResponseEntity<Void> resumeOrder(@PathVariable String executionId) {
        workflowStarter.resume(executionId, null);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{executionId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String executionId) {
        workflowStarter.cancel(executionId);
        return ResponseEntity.noContent().build();
    }
}
```

## Step 7: Run Kafka (Docker)

Start Kafka using Docker Compose:

```yaml
# docker-compose.yml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

```bash
docker-compose up -d
```

## Step 8: Test Your Workflow

Start your application and make a request:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust-123",
    "items": [
      {"productId": "prod-1", "quantity": 2, "price": 29.99}
    ],
    "paymentInfo": {
      "cardNumber": "4111111111111111",
      "expiryMonth": 12,
      "expiryYear": 2025
    },
    "shippingAddress": {
      "street": "123 Main St",
      "city": "Springfield",
      "zipCode": "12345"
    }
  }'
```

Response:
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "executionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "PROCESSING"
}
```

## Next Steps

- [Configure retry policies](./retry-configuration.md)
- [Add monitoring with thalyazin-monitor](./monitoring.md)
- [Use RabbitMQ instead of Kafka](./rabbitmq-setup.md)
- [Handle errors and compensation](./error-handling.md)