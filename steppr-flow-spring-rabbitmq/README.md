# Steppr Flow Broker RabbitMQ

RabbitMQ implementation of the Steppr Flow message broker.

## Overview

This module provides RabbitMQ-based message transport for Steppr Flow workflows, ideal for traditional message queuing scenarios.

## Installation

```xml
<dependency>
    <groupId>io.stepprflow</groupId>
    <artifactId>steppr-flow-spring-rabbitmq</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

```yaml
stepprflow:
  enabled: true
  broker: rabbitmq
  rabbitmq:
    exchange: stepprflow-exchange
    exchange-type: topic
    durable: true
    auto-delete: false
    concurrency: 3
    prefetch: 10

spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `stepprflow.rabbitmq.exchange` | Exchange name | `stepprflow-exchange` |
| `stepprflow.rabbitmq.exchange-type` | Exchange type | `topic` |
| `stepprflow.rabbitmq.durable` | Durable queues | `true` |
| `stepprflow.rabbitmq.concurrency` | Consumer concurrency | `1` |
| `stepprflow.rabbitmq.prefetch` | Prefetch count | `10` |

## Features

- **Topic exchange**: Flexible routing patterns
- **Durable queues**: Messages survive broker restart
- **Dead Letter Exchange**: Failed messages routed to DLX
- **Manual acknowledgment**: Reliable message processing

## Queue Naming

| Queue Pattern | Description |
|---------------|-------------|
| `stepprflow.{workflow-topic}` | Main workflow queue |
| `stepprflow.{workflow-topic}.dlq` | Dead Letter Queue |

## Usage

Steppr Flow auto-configures automatically with Spring Boot. No additional annotations required:

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

Then define your workflows using `@Topic` and `@Step` annotations:

```java
@Component
@Topic("order-workflow")
public class OrderWorkflow {

    @Step(id = 1, label = "Validate")
    public void validate(OrderPayload payload) {
        // Validation logic
    }

    @Step(id = 2, label = "Process")
    public void process(OrderPayload payload) {
        // Processing logic
    }
}
```

## Docker Compose

```yaml
services:
  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
```

## Management UI

Access RabbitMQ Management UI at `http://localhost:15672` (guest/guest).
