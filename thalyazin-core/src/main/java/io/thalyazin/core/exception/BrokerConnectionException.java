package io.thalyazin.core.exception;

/**
 * Exception thrown when unable to connect to a message broker.
 */
public class BrokerConnectionException extends MessageBrokerException {

    private final String bootstrapServers;

    public BrokerConnectionException(String brokerType, String bootstrapServers, String message) {
        super(brokerType, String.format("Failed to connect to %s: %s", bootstrapServers, message));
        this.bootstrapServers = bootstrapServers;
    }

    public BrokerConnectionException(String brokerType, String bootstrapServers, String message, Throwable cause) {
        super(brokerType, String.format("Failed to connect to %s: %s", bootstrapServers, message), cause);
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Returns the bootstrap servers or connection URL.
     */
    public String getBootstrapServers() {
        return bootstrapServers;
    }
}