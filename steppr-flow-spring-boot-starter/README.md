# Steppr Flow Spring Boot Starter

Spring Boot starter for easy integration of Steppr Flow into your applications.

## Overview

This starter provides auto-configuration for Steppr Flow, making it easy to add workflow orchestration capabilities to any Spring Boot application.

## Features

- Auto-configuration of all Steppr Flow components
- Automatic workflow registration
- Integration with Spring Boot Actuator for health checks
- Support for both Kafka and RabbitMQ brokers

## Installation

```xml
<dependency>
    <groupId>io.stepprflow</groupId>
    <artifactId>steppr-flow-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Then add one of the broker implementations:

**For Kafka:**
```xml
<dependency>
    <groupId>io.stepprflow</groupId>
    <artifactId>steppr-flow-spring-kafka</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**For RabbitMQ:**
```xml
<dependency>
    <groupId>io.stepprflow</groupId>
    <artifactId>steppr-flow-spring-rabbitmq</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

```yaml
steppr-flow:
  enabled: true
  retry:
    max-attempts: 3
    initial-delay: 1s
    max-delay: 5m
    multiplier: 2.0

# For Kafka
spring:
  kafka:
    bootstrap-servers: localhost:9092

# For RabbitMQ
spring:
  rabbitmq:
    host: localhost
    port: 5672
```

## Usage

Simply annotate your Spring Boot application class:

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

Then define your workflows using `@Topic` and `@Step`:

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

## What's Included

This starter bundles:
- `steppr-flow-core` - Core workflow engine
- `steppr-flow-spring-monitor` - Monitoring and REST API

## Architecture

```
┌──────────────────────────────────────────┐
│          Your Spring Boot App            │
├──────────────────────────────────────────┤
│  ┌────────────────────────────────────┐  │
│  │   steppr-flow-spring-boot-starter  │  │
│  ├────────────────────────────────────┤  │
│  │  steppr-flow-core                  │  │
│  │  steppr-flow-spring-monitor        │  │
│  └────────────────────────────────────┘  │
├──────────────────────────────────────────┤
│  steppr-flow-spring-kafka (or rabbitmq)  │
└──────────────────────────────────────────┘
```
