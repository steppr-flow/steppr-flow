package io.thalyazin.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "thalyazin.monitor")
@Data
public class MonitorProperties {

    /**
     * Enable monitoring module.
     */
    private boolean enabled = true;

    /**
     * MongoDB collection name for executions.
     */
    private String collectionName = "workflow_executions";

    /**
     * WebSocket configuration.
     */
    private WebSocket webSocket = new WebSocket();

    /**
     * Retention configuration.
     */
    private Retention retention = new Retention();

    /**
     * Retry scheduler configuration.
     */
    private RetryScheduler retryScheduler = new RetryScheduler();

    /**
     * Registry configuration for workflow registration from agents.
     */
    private Registry registry = new Registry();

    @Data
    public static class WebSocket {
        private boolean enabled = true;
        private String endpoint = "/ws/workflow";
        private String topicPrefix = "/topic/workflow";
    }

    @Data
    public static class Retention {
        /**
         * How long to keep completed executions.
         */
        private Duration completedTtl = Duration.ofDays(7);

        /**
         * How long to keep failed executions.
         */
        private Duration failedTtl = Duration.ofDays(30);
    }

    @Data
    public static class RetryScheduler {
        /**
         * Enable automatic retry processing.
         */
        private boolean enabled = true;

        /**
         * Interval for checking pending retries.
         */
        private Duration checkInterval = Duration.ofSeconds(30);
    }

    @Data
    public static class Registry {
        /**
         * Timeout after which an instance without heartbeat is considered stale.
         * Stale instances are automatically removed during cleanup.
         */
        private Duration instanceTimeout = Duration.ofMinutes(5);

        /**
         * Interval for running the stale instance cleanup job.
         */
        private Duration cleanupInterval = Duration.ofMinutes(1);
    }
}