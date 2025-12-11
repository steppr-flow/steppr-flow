package io.thalyazin.core.exception;

/**
 * Exception thrown when a step execution fails.
 */
public class StepExecutionException extends WorkflowException {

    private final int stepId;
    private final String stepLabel;

    public StepExecutionException(String stepLabel, int stepId, String message) {
        super(String.format("Step '%s' (id=%d) failed: %s", stepLabel, stepId, message));
        this.stepId = stepId;
        this.stepLabel = stepLabel;
    }

    public StepExecutionException(String stepLabel, int stepId, String message, Throwable cause) {
        super(String.format("Step '%s' (id=%d) failed: %s", stepLabel, stepId, message), cause);
        this.stepId = stepId;
        this.stepLabel = stepLabel;
    }

    /**
     * @deprecated Use {@link #StepExecutionException(String, int, String)} instead.
     */
    @Deprecated
    public StepExecutionException(int stepId, String stepLabel, String message) {
        this(stepLabel, stepId, message);
    }

    /**
     * @deprecated Use {@link #StepExecutionException(String, int, String, Throwable)} instead.
     */
    @Deprecated
    public StepExecutionException(int stepId, String stepLabel, String message, Throwable cause) {
        this(stepLabel, stepId, message, cause);
    }

    public int getStepId() {
        return stepId;
    }

    public String getStepLabel() {
        return stepLabel;
    }
}