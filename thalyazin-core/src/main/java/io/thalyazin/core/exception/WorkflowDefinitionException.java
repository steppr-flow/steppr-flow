package io.thalyazin.core.exception;

/**
 * Exception thrown when a workflow definition is invalid or cannot be found.
 */
public class WorkflowDefinitionException extends WorkflowException {

    private final String topic;

    public WorkflowDefinitionException(String message) {
        super(message);
        this.topic = null;
    }

    public WorkflowDefinitionException(String topic, String message) {
        super(String.format("Workflow '%s': %s", topic, message));
        this.topic = topic;
    }

    public WorkflowDefinitionException(String topic, String message, Throwable cause) {
        super(String.format("Workflow '%s': %s", topic, message), cause);
        this.topic = topic;
    }

    /**
     * Returns the workflow topic if known.
     */
    public String getTopic() {
        return topic;
    }
}