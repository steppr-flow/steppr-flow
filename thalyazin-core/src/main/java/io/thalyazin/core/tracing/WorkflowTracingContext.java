package io.thalyazin.core.tracing;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

/**
 * Observation context for workflow step execution.
 * Holds all the contextual information needed for tracing.
 */
@Getter
@Setter
public class WorkflowTracingContext extends Observation.Context {

    private String executionId;
    private String correlationId;
    private String topic;
    private int stepId;
    private String stepLabel;
    private int totalSteps;
    private String status = "IN_PROGRESS";

    public WorkflowTracingContext(String executionId, String correlationId, String topic,
                                   int stepId, String stepLabel, int totalSteps) {
        this.executionId = executionId;
        this.correlationId = correlationId;
        this.topic = topic;
        this.stepId = stepId;
        this.stepLabel = stepLabel;
        this.totalSteps = totalSteps;
    }

    public void markSuccess() {
        this.status = "SUCCESS";
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public void markTimeout() {
        this.status = "TIMEOUT";
    }
}