# Thalyazin Broker Kafka

Apache Kafka implementation of the Thalyazin message broker.

## Overview

This module provides Kafka-based message transport for Thalyazin workflows, enabling distributed, high-throughput workflow execution.

## Installation

```xml
<dependency>
    <groupId>io.thalyazin</groupId>
    <artifactId>thalyazin-broker-kafka</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

```yaml
thalyazin:
  enabled: true
  broker: kafka
  kafka:
    bootstrap-servers: localhost:9092
    trusted-packages:
      - com.yourcompany.workflow
    consumer:
      group-id: thalyazin-workers
      auto-offset-reset: earliest
      concurrency: 3
    producer:
      acks: all
      retries: 3

spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `thalyazin.kafka.bootstrap-servers` | Kafka broker addresses | `localhost:9092` |
| `thalyazin.kafka.consumer.group-id` | Consumer group ID | `thalyazin` |
| `thalyazin.kafka.consumer.concurrency` | Number of consumer threads | `1` |
| `thalyazin.kafka.producer.acks` | Producer acknowledgment | `all` |
| `thalyazin.kafka.trusted-packages` | Packages for deserialization | `[]` |

## Features

- **Partitioned topics**: Workflows can be distributed across partitions
- **Consumer groups**: Multiple instances share the workload
- **Exactly-once semantics**: With proper Kafka configuration
- **Dead Letter Queue**: Failed messages sent to `*.DLT` topics

## Topic Naming

| Topic Pattern | Description |
|---------------|-------------|
| `{workflow-topic}` | Main workflow messages |
| `{workflow-topic}.DLT` | Dead Letter Topic for failed messages |

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
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
```
