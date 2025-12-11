package io.thalyazin.core.model;

import io.thalyazin.core.service.Thalyazin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

/**
 * Definition of a workflow.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition {

    /**
     * Topic name.
     */
    private String topic;

    /**
     * Description.
     */
    private String description;

    /**
     * Workflow handler instance.
     */
    private Thalyazin handler;

    /**
     * Handler class.
     */
    private Class<? extends Thalyazin> handlerClass;

    /**
     * Ordered list of step definitions.
     */
    private List<StepDefinition> steps;

    /**
     * Success callback method.
     */
    private Method onSuccessMethod;

    /**
     * Failure callback method.
     */
    private Method onFailureMethod;

    /**
     * Workflow timeout.
     */
    private Duration timeout;

    /**
     * Number of partitions.
     */
    private int partitions;

    /**
     * Replication factor.
     */
    private short replication;

    /**
     * Get step by ID.
     */
    public StepDefinition getStep(int stepId) {
        return steps.stream()
                .filter(s -> s.getId() == stepId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get total number of steps.
     */
    public int getTotalSteps() {
        return steps.size();
    }

    /**
     * Check if step ID is the last step.
     */
    public boolean isLastStep(int stepId) {
        return steps.stream()
                .mapToInt(StepDefinition::getId)
                .max()
                .orElse(0) == stepId;
    }
}