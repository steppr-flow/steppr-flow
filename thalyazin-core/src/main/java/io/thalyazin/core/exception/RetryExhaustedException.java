package io.thalyazin.core.exception;

/**
 * Exception thrown when all retry attempts are exhausted.
 */
public class RetryExhaustedException extends WorkflowException {

    private final String executionId;
    private final int attempts;

    public RetryExhaustedException(String executionId, int attempts, String message) {
        super(String.format("Workflow '%s' retry exhausted after %d attempts: %s",
                executionId, attempts, message));
        this.executionId = executionId;
        this.attempts = attempts;
    }

    public RetryExhaustedException(String executionId, int attempts, String message, Throwable cause) {
        super(String.format("Workflow '%s' retry exhausted after %d attempts: %s",
                executionId, attempts, message), cause);
        this.executionId = executionId;
        this.attempts = attempts;
    }

    /**
     * @deprecated Use {@link #RetryExhaustedException(String, int, String)} instead.
     */
    @Deprecated
    public RetryExhaustedException(int attempts, String message) {
        super(String.format("Retry exhausted after %d attempts: %s", attempts, message));
        this.executionId = null;
        this.attempts = attempts;
    }

    public String getExecutionId() {
        return executionId;
    }

    public int getAttempts() {
        return attempts;
    }
}