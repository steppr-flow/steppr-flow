package io.thalyazin.core.exception;

/**
 * Exception thrown when a workflow execution cannot be found.
 */
public class WorkflowNotFoundException extends WorkflowException {

    private final String executionId;

    public WorkflowNotFoundException(String executionId) {
        super("Workflow execution not found: " + executionId);
        this.executionId = executionId;
    }

    public WorkflowNotFoundException(String executionId, String message) {
        super(message);
        this.executionId = executionId;
    }

    public String getExecutionId() {
        return executionId;
    }
}