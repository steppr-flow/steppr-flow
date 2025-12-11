package io.thalyazin.core.exception;

import io.thalyazin.core.model.WorkflowStatus;

/**
 * Exception thrown when a workflow operation is not allowed due to its current state.
 */
public class WorkflowStateException extends WorkflowException {

    private final String executionId;
    private final WorkflowStatus currentStatus;
    private final String operation;

    public WorkflowStateException(String executionId, WorkflowStatus currentStatus, String operation) {
        super(String.format(
            "Workflow '%s' with status %s cannot be %s",
            executionId, currentStatus, operation
        ));
        this.executionId = executionId;
        this.currentStatus = currentStatus;
        this.operation = operation;
    }

    public String getExecutionId() {
        return executionId;
    }

    public WorkflowStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getOperation() {
        return operation;
    }
}