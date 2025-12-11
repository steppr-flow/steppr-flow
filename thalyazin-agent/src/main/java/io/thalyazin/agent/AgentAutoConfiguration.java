package io.thalyazin.agent;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auto-configuration for Thalyazin Agent.
 *
 * This is a lightweight module that provides workflow execution capabilities
 * without the monitoring endpoints. Use this in your microservices instead of
 * thalyazin-monitor to avoid exposing monitoring REST endpoints.
 *
 * The agent includes:
 * - Core workflow annotations and services (@Topic, @Step, WorkflowStarter, etc.)
 * - Kafka broker for message publishing and consuming
 * - Circuit breaker support
 * - Auto-registration with thalyazin-server (if configured)
 *
 * The agent does NOT include:
 * - REST endpoints for monitoring (/api/workflows, /api/metrics, etc.)
 * - MongoDB persistence
 * - WebSocket for real-time updates
 * - Dashboard UI
 *
 * For monitoring capabilities, deploy the thalyazin-server container separately.
 *
 * To enable auto-registration, set:
 *   thalyazin.agent.server-url=http://thalyazin-server:8090
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "thalyazin", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AgentProperties.class)
@EnableScheduling
@ComponentScan(basePackages = "io.thalyazin.agent")
public class AgentAutoConfiguration {
    // This configuration class serves as an entry point for the agent module.
    // The actual configurations are imported transitively from:
    // - thalyazin-core (ThalyazinAutoConfiguration)
    // - thalyazin-broker-kafka (KafkaBrokerAutoConfiguration)
}