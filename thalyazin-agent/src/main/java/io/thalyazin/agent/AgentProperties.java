package io.thalyazin.agent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Thalyazin Agent.
 */
@Data
@ConfigurationProperties(prefix = "thalyazin.agent")
public class AgentProperties {

    /**
     * URL of the Thalyazin monitoring server.
     * If set, the agent will register workflows at startup.
     */
    private String serverUrl;

    /**
     * Whether to enable auto-registration with the server.
     */
    private boolean autoRegister = true;

    /**
     * Heartbeat interval in seconds (0 to disable).
     */
    private int heartbeatIntervalSeconds = 30;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeoutMs = 5000;

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeoutMs = 10000;
}
