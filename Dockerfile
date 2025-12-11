# =============================================================================
# Multi-stage build for Steppr Flow Dashboard with embedded UI
# This creates a single image containing both the backend (Spring Boot) and
# the frontend (Vue.js served by Spring Boot)
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build Vue.js frontend
# -----------------------------------------------------------------------------
FROM node:20-alpine AS ui-builder

WORKDIR /app/ui

# Copy package files for better caching
COPY steppr-flow-ui/package*.json ./

# Install dependencies
RUN npm ci --silent

# Copy UI source code
COPY steppr-flow-ui/ .

# Build the frontend
RUN npm run build

# -----------------------------------------------------------------------------
# Stage 2: Build Spring Boot backend
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder

WORKDIR /app

# Copy pom files first for better caching
COPY pom.xml .
COPY steppr-flow-core/pom.xml steppr-flow-core/
COPY steppr-flow-spring-kafka/pom.xml steppr-flow-spring-kafka/
COPY steppr-flow-spring-rabbitmq/pom.xml steppr-flow-spring-rabbitmq/
COPY steppr-flow-spring-monitor/pom.xml steppr-flow-spring-monitor/
COPY steppr-flow-ui/pom.xml steppr-flow-ui/
COPY steppr-flow-spring-boot-starter/pom.xml steppr-flow-spring-boot-starter/
COPY steppr-flow-dashboard/pom.xml steppr-flow-dashboard/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl steppr-flow-dashboard -am -q || true

# Copy source code
COPY steppr-flow-core/src steppr-flow-core/src
COPY steppr-flow-spring-kafka/src steppr-flow-spring-kafka/src
COPY steppr-flow-spring-rabbitmq/src steppr-flow-spring-rabbitmq/src
COPY steppr-flow-spring-monitor/src steppr-flow-spring-monitor/src
COPY steppr-flow-dashboard/src steppr-flow-dashboard/src

# Copy checkstyle config
COPY config/checkstyle/checkstyle.xml config/checkstyle/checkstyle.xml

# Copy built UI assets to static resources
COPY --from=ui-builder /app/ui/dist steppr-flow-dashboard/src/main/resources/static/

# Build the application with embedded UI
RUN mvn clean package -pl steppr-flow-dashboard -am -DskipTests -q

# -----------------------------------------------------------------------------
# Stage 3: Extract Spring Boot layers for optimized caching
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS layers

WORKDIR /app
COPY --from=backend-builder /app/steppr-flow-dashboard/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# -----------------------------------------------------------------------------
# Stage 4: Final runtime image
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Steppr Flow Team <contact@stepprflow.io>"
LABEL description="Steppr Flow Dashboard - Monitoring server with embedded UI"
LABEL org.opencontainers.image.source="https://github.com/steppr-flow/steppr-flow"
LABEL org.opencontainers.image.title="Steppr Flow Dashboard"
LABEL org.opencontainers.image.description="Multi-broker workflow orchestration monitoring dashboard"
LABEL org.opencontainers.image.vendor="Steppr Flow"

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1000 stepprflow && \
    adduser -u 1000 -G stepprflow -s /bin/sh -D stepprflow

# Copy layers in order of change frequency (less frequent first)
COPY --from=layers /app/dependencies/ ./
COPY --from=layers /app/spring-boot-loader/ ./
COPY --from=layers /app/snapshot-dependencies/ ./
COPY --from=layers /app/application/ ./

# Change ownership
RUN chown -R stepprflow:stepprflow /app

USER stepprflow

# Expose port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Default Spring profiles
ENV SPRING_PROFILES_ACTIVE="docker"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
