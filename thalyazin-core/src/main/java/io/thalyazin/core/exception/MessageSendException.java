package io.thalyazin.core.exception;

/**
 * Exception thrown when a message cannot be sent to the broker.
 */
public class MessageSendException extends MessageBrokerException {

    private final String topic;
    private final String executionId;

    public MessageSendException(String brokerType, String topic, String message) {
        super(brokerType, String.format("Failed to send to topic '%s': %s", topic, message));
        this.topic = topic;
        this.executionId = null;
    }

    public MessageSendException(String brokerType, String topic, String message, Throwable cause) {
        super(brokerType, String.format("Failed to send to topic '%s': %s", topic, message), cause);
        this.topic = topic;
        this.executionId = null;
    }

    public MessageSendException(String brokerType, String topic, String executionId, String message, Throwable cause) {
        super(brokerType, String.format("Failed to send to topic '%s' [%s]: %s", topic, executionId, message), cause);
        this.topic = topic;
        this.executionId = executionId;
    }

    /**
     * Returns the topic the message was being sent to.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Returns the execution ID if available.
     */
    public String getExecutionId() {
        return executionId;
    }
}