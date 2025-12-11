# Thalyazin Broker RabbitMQ

RabbitMQ implementation of the Thalyazin message broker.

## Overview

This module provides RabbitMQ-based message transport for Thalyazin workflows, ideal for traditional message queuing scenarios.

## Installation

```xml
<dependency>
    <groupId>io.thalyazin</groupId>
    <artifactId>thalyazin-broker-rabbitmq</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

```yaml
thalyazin:
  enabled: true
  broker: rabbitmq
  rabbitmq:
    exchange: thalyazin-exchange
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
| `thalyazin.rabbitmq.exchange` | Exchange name | `thalyazin-exchange` |
| `thalyazin.rabbitmq.exchange-type` | Exchange type | `topic` |
| `thalyazin.rabbitmq.durable` | Durable queues | `true` |
| `thalyazin.rabbitmq.concurrency` | Consumer concurrency | `1` |
| `thalyazin.rabbitmq.prefetch` | Prefetch count | `10` |

## Features

- **Topic exchange**: Flexible routing patterns
- **Durable queues**: Messages survive broker restart
- **Dead Letter Exchange**: Failed messages routed to DLX
- **Manual acknowledgment**: Reliable message processing

## Queue Naming

| Queue Pattern | Description |
|---------------|-------------|
| `thalyazin.{workflow-topic}` | Main workflow queue |
| `thalyazin.{workflow-topic}.dlq` | Dead Letter Queue |

## Usage

```java
@SpringBootApplication
@EnableThalyazin
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
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
