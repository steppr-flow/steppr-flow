package io.thalyazin.server;

import io.thalyazin.server.config.UiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Thalyazin Monitoring Server.
 *
 * This is a standalone Spring Boot application that provides centralized
 * monitoring for all Thalyazin-enabled microservices.
 *
 * Features:
 * - REST API for workflow monitoring (/api/workflows, /api/metrics, /api/dashboard)
 * - WebSocket for real-time updates
 * - MongoDB persistence for workflow execution history
 * - Kafka consumer for receiving workflow events from microservices
 * - Retry scheduler for automatic workflow retries
 *
 * Deploy this as a Docker container alongside Kafka and MongoDB.
 */
@SpringBootApplication(scanBasePackages = {
        "io.thalyazin.server",
        "io.thalyazin.monitor",
        "io.thalyazin.broker.kafka",
        "io.thalyazin.core"
})
@EnableConfigurationProperties(UiProperties.class)
@EnableScheduling
public class ThalyazinServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThalyazinServerApplication.class, args);
    }
}