package io.stepprflow.core.exception;

import io.stepprflow.core.model.WorkflowStatus;

/**
 * Exception thrown when a workflow operation is not allowed
 * due to its current state.
 */
public class WorkflowStateException extends WorkflowException {

    /** The execution ID. */
    private final String executionId;

    /** The current workflow status. */
    private final WorkflowStatus currentStatus;

    /** The operation that was attempted. */
    private final String operation;

    /**
     * Constructs a new workflow state exception.
     *
     * @param execId the execution ID
     * @param status the current workflow status
     * @param op the operation that was attempted
     */
    public WorkflowStateException(
            final String execId,
            final WorkflowStatus status,
            final String op) {
        super(String.format(
            "Workflow '%s' with status %s cannot be %s",
            execId, status, op
        ));
        this.executionId = execId;
        this.currentStatus = status;
        this.operation = op;
    }

    /**
     * Returns the execution ID.
     *
     * @return the execution ID
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Returns the current workflow status.
     *
     * @return the current status
     */
    public WorkflowStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Returns the operation that was attempted.
     *
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }
}
