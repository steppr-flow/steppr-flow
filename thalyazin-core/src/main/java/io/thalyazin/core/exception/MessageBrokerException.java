package io.thalyazin.core.exception;

/**
 * Exception thrown when a message broker operation fails.
 *
 * <p>This is the base exception for all broker-related errors, including
 * connection issues, send failures, and receive failures.
 */
public class MessageBrokerException extends WorkflowException {

    private final String brokerType;

    public MessageBrokerException(String message) {
        super(message);
        this.brokerType = null;
    }

    public MessageBrokerException(String message, Throwable cause) {
        super(message, cause);
        this.brokerType = null;
    }

    public MessageBrokerException(String brokerType, String message) {
        super(String.format("[%s] %s", brokerType, message));
        this.brokerType = brokerType;
    }

    public MessageBrokerException(String brokerType, String message, Throwable cause) {
        super(String.format("[%s] %s", brokerType, message), cause);
        this.brokerType = brokerType;
    }

    /**
     * Returns the broker type (e.g., "kafka", "rabbitmq") if known.
     */
    public String getBrokerType() {
        return brokerType;
    }
}