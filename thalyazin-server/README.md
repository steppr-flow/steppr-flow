# Thalyazin Server

Standalone monitoring server combining the monitor module and UI dashboard.

## Overview

A complete, ready-to-deploy monitoring solution that bundles:
- REST API for workflow management
- Vue.js dashboard UI
- WebSocket for real-time updates

## Quick Start

### With Docker Compose

```bash
docker-compose up -d
```

Access the dashboard at `http://localhost:8090/dashboard`

### Standalone JAR

```bash
# Build
mvn package -pl thalyazin-server -am

# Run
java -jar thalyazin-server/target/thalyazin-server-1.0.0-SNAPSHOT.jar
```

## Configuration

```yaml
server:
  port: 8090

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/thalyazin

thalyazin:
  enabled: true
  broker: kafka  # or rabbitmq

  kafka:
    bootstrap-servers: localhost:9092

  ui:
    enabled: true
    cors:
      allowed-origins:
        - http://localhost:5173  # Dev server
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | `8090` |
| `MONGODB_URI` | MongoDB connection | `mongodb://localhost:27017/thalyazin` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |
| `LOG_LEVEL` | Logging level | `INFO` |

## Endpoints

| Path | Description |
|------|-------------|
| `/dashboard` | Web UI |
| `/api/workflows` | REST API |
| `/api/metrics` | Workflow metrics |
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Prometheus metrics |

## Docker

```bash
# Build image
docker build -t thalyazin-server -f thalyazin-server/Dockerfile .

# Run with environment variables
docker run -p 8090:8090 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/thalyazin \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  thalyazin-server
```

## Architecture

```
┌──────────────────────────────────────┐
│          thalyazin-server            │
├──────────────────────────────────────┤
│  ┌────────────┐  ┌────────────────┐  │
│  │  REST API  │  │  Vue.js UI     │  │
│  │  /api/*    │  │  /dashboard    │  │
│  └────────────┘  └────────────────┘  │
├──────────────────────────────────────┤
│           thalyazin-monitor          │
├──────────────────────────────────────┤
│  MongoDB  │  Kafka/RabbitMQ          │
└──────────────────────────────────────┘
```
