# Thalyazin Agent

Lightweight worker agent for executing Thalyazin workflows.

## Overview

The agent module provides a minimal runtime for workflow execution, designed to be deployed as standalone workers that process workflow messages.

## Use Cases

- **Horizontal scaling**: Deploy multiple agents to process workflows in parallel
- **Isolated workers**: Run specific workflow types on dedicated instances
- **Resource optimization**: Lightweight footprint for container deployments

## Installation

```xml
<dependency>
    <groupId>io.thalyazin</groupId>
    <artifactId>thalyazin-agent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

```yaml
thalyazin:
  agent:
    enabled: true
    topics:
      - order-processing
      - payment-workflow
    concurrency: 5
```

## Deployment

### Docker

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY target/my-agent.jar /app/agent.jar
CMD ["java", "-jar", "/app/agent.jar"]
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: thalyazin-agent
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: agent
          image: my-agent:latest
          env:
            - name: KAFKA_BOOTSTRAP_SERVERS
              value: kafka:9092
```

## Architecture

```
┌─────────────────────────────────────────┐
│              Thalyazin Agent            │
├─────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ StepExecutor│  │ WorkflowRegistry│   │
│  └─────────────┘  └─────────────────┘   │
├─────────────────────────────────────────┤
│         Message Broker (Kafka/RMQ)      │
└─────────────────────────────────────────┘
```
