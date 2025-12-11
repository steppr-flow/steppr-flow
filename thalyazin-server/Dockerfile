# Multi-stage build for Thalyazin Server
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom files first for better caching
COPY pom.xml .
COPY thalyazin-core/pom.xml thalyazin-core/
COPY thalyazin-broker-kafka/pom.xml thalyazin-broker-kafka/
COPY thalyazin-broker-rabbitmq/pom.xml thalyazin-broker-rabbitmq/
COPY thalyazin-monitor/pom.xml thalyazin-monitor/
COPY thalyazin-ui/pom.xml thalyazin-ui/
COPY thalyazin-agent/pom.xml thalyazin-agent/
COPY thalyazin-server/pom.xml thalyazin-server/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl thalyazin-server -am -q || true

# Copy source code
COPY thalyazin-core/src thalyazin-core/src
COPY thalyazin-broker-kafka/src thalyazin-broker-kafka/src
COPY thalyazin-monitor/src thalyazin-monitor/src
COPY thalyazin-ui/src thalyazin-ui/src
COPY thalyazin-server/src thalyazin-server/src

# Build the application
RUN mvn clean package -pl thalyazin-server -am -DskipTests -q

# Extract layers for optimized Docker caching
FROM eclipse-temurin:21-jdk-alpine AS layers
WORKDIR /app
COPY --from=builder /app/thalyazin-server/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Final runtime image
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Thalyazin Team"
LABEL description="Thalyazin Monitoring Server"
LABEL version="1.0.0-SNAPSHOT"

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1000 thalyazin && \
    adduser -u 1000 -G thalyazin -s /bin/sh -D thalyazin

# Copy layers in order of change frequency
COPY --from=layers /app/dependencies/ ./
COPY --from=layers /app/spring-boot-loader/ ./
COPY --from=layers /app/snapshot-dependencies/ ./
COPY --from=layers /app/application/ ./

# Change ownership
RUN chown -R thalyazin:thalyazin /app

USER thalyazin

# Expose port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]