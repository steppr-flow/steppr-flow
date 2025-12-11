package io.thalyazin.core.metrics;

import io.thalyazin.core.event.WorkflowMessageEvent;
import io.thalyazin.core.model.WorkflowMessage;
import io.thalyazin.core.model.WorkflowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for workflow events and records metrics.
 * Works with WorkflowMetrics to update in-memory metrics counters.
 * Bean is created by WorkflowMetricsAutoConfiguration when MeterRegistry is available.
 */
@RequiredArgsConstructor
@Slf4j
public class WorkflowMetricsListener {

    private final WorkflowMetrics metrics;

    // Track workflow start times for duration calculation
    private final Map<String, Instant> workflowStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Instant> stepStartTimes = new ConcurrentHashMap<>();

    @EventListener
    public void onWorkflowMessage(WorkflowMessageEvent event) {
        WorkflowMessage message = event.getMessage();
        String topic = message.getTopic();
        String executionId = message.getExecutionId();
        WorkflowStatus status = message.getStatus();

        try {
            switch (status) {
                case PENDING -> handlePending(message);
                case IN_PROGRESS -> handleInProgress(message);
                case COMPLETED -> handleCompleted(message);
                case FAILED -> handleFailed(message);
                case CANCELLED -> handleCancelled(message);
                case RETRY_PENDING -> handleRetryPending(message);
                default -> log.debug("Ignoring status {} for metrics", status);
            }
        } catch (Exception e) {
            log.warn("Error recording metrics for workflow {} [{}]: {}", topic, executionId, e.getMessage());
        }
    }

    private void handlePending(WorkflowMessage message) {
        String executionId = message.getExecutionId();
        workflowStartTimes.put(executionId, Instant.now());
        metrics.recordWorkflowStarted(message.getTopic(), message.getServiceName());
    }

    private void handleInProgress(WorkflowMessage message) {
        String executionId = message.getExecutionId();
        String stepKey = executionId + ":" + message.getCurrentStep();
        stepStartTimes.put(stepKey, Instant.now());
    }

    private void handleCompleted(WorkflowMessage message) {
        String executionId = message.getExecutionId();
        Instant startTime = workflowStartTimes.remove(executionId);
        Duration duration = startTime != null
                ? Duration.between(startTime, Instant.now())
                : Duration.ZERO;
        metrics.recordWorkflowCompleted(message.getTopic(), message.getServiceName(), duration);
    }

    private void handleFailed(WorkflowMessage message) {
        String executionId = message.getExecutionId();
        Instant startTime = workflowStartTimes.remove(executionId);
        Duration duration = startTime != null
                ? Duration.between(startTime, Instant.now())
                : Duration.ZERO;
        metrics.recordWorkflowFailed(message.getTopic(), message.getServiceName(), duration);
        metrics.recordDlq(message.getTopic());
    }

    private void handleCancelled(WorkflowMessage message) {
        String executionId = message.getExecutionId();
        workflowStartTimes.remove(executionId);
        metrics.recordWorkflowCancelled(message.getTopic(), message.getServiceName());
    }

    private void handleRetryPending(WorkflowMessage message) {
        int attempt = message.getRetryInfo() != null ? message.getRetryInfo().getAttempt() : 1;
        metrics.recordRetry(message.getTopic(), attempt);
    }

    /**
     * Record step completion with duration.
     * Called directly from StepExecutor after successful step execution.
     */
    public void recordStepCompleted(String topic, String stepLabel, String executionId, int stepId) {
        String stepKey = executionId + ":" + stepId;
        Instant startTime = stepStartTimes.remove(stepKey);
        Duration duration = startTime != null
                ? Duration.between(startTime, Instant.now())
                : Duration.ZERO;
        metrics.recordStepExecuted(topic, stepLabel, duration);
    }

    /**
     * Record step failure.
     */
    public void recordStepFailed(String topic, String stepLabel, boolean isTimeout) {
        if (isTimeout) {
            metrics.recordStepTimeout(topic, stepLabel);
        }
        metrics.recordStepFailed(topic, stepLabel);
    }
}