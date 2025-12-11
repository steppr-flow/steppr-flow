package io.thalyazin.core.exception;

import java.time.Duration;

/**
 * Exception thrown when a workflow step exceeds its configured timeout.
 *
 * <p>This exception is thrown by the StepExecutor when a step takes longer
 * than the timeout specified via {@link io.thalyazin.core.annotation.Timeout}
 * annotation.
 *
 * <p>Step timeout exceptions are retryable by default, allowing the workflow
 * to retry the step if configured to do so.
 */
public class StepTimeoutException extends WorkflowException {

    private final String stepLabel;
    private final int stepId;
    private final Duration timeout;
    private final Duration elapsed;

    public StepTimeoutException(String stepLabel, int stepId, Duration timeout, Duration elapsed) {
        super(String.format(
            "Step '%s' (id=%d) timed out after %s (timeout: %s)",
            stepLabel, stepId, formatDuration(elapsed), formatDuration(timeout)
        ));
        this.stepLabel = stepLabel;
        this.stepId = stepId;
        this.timeout = timeout;
        this.elapsed = elapsed;
    }

    public StepTimeoutException(String stepLabel, int stepId, Duration timeout) {
        super(String.format(
            "Step '%s' (id=%d) timed out (timeout: %s)",
            stepLabel, stepId, formatDuration(timeout)
        ));
        this.stepLabel = stepLabel;
        this.stepId = stepId;
        this.timeout = timeout;
        this.elapsed = null;
    }

    public String getStepLabel() {
        return stepLabel;
    }

    public int getStepId() {
        return stepId;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Duration getElapsed() {
        return elapsed;
    }

    private static String formatDuration(Duration duration) {
        if (duration == null) {
            return "unknown";
        }
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        if (remainingSeconds == 0) {
            return minutes + "m";
        }
        return minutes + "m " + remainingSeconds + "s";
    }
}