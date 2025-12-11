package io.thalyazin.core.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.thalyazin.core.model.StepDefinition;
import io.thalyazin.core.model.WorkflowMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Provides tracing capabilities for workflow execution.
 * Uses Micrometer Observation API for distributed tracing.
 */
@Component
@ConditionalOnBean(ObservationRegistry.class)
@RequiredArgsConstructor
@Slf4j
public class WorkflowTracing {

    private final ObservationRegistry observationRegistry;

    /**
     * Execute a step with tracing observation.
     *
     * @param message the workflow message
     * @param step the step definition
     * @param execution the step execution logic
     * @param <T> the return type
     * @return the result of the execution
     * @throws Exception if the execution fails
     */
    public <T> T traceStep(WorkflowMessage message, StepDefinition step, Supplier<T> execution) throws Exception {
        WorkflowTracingContext context = new WorkflowTracingContext(
                message.getExecutionId(),
                message.getCorrelationId(),
                message.getTopic(),
                step.getId(),
                step.getLabel(),
                message.getTotalSteps()
        );

        Observation observation = Observation.createNotStarted(
                WorkflowTracingObservationConvention.INSTANCE,
                () -> context,
                observationRegistry
        );

        try {
            return observation.observe(() -> {
                try {
                    T result = execution.get();
                    context.markSuccess();
                    return result;
                } catch (RuntimeException e) {
                    context.markFailed();
                    throw e;
                }
            });
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * Execute a step with tracing observation (void return).
     *
     * @param message the workflow message
     * @param step the step definition
     * @param execution the step execution logic
     * @throws Exception if the execution fails
     */
    public void traceStep(WorkflowMessage message, StepDefinition step, Runnable execution) throws Exception {
        traceStep(message, step, () -> {
            execution.run();
            return null;
        });
    }

    /**
     * Create a new observation for workflow start.
     */
    public Observation startWorkflowObservation(WorkflowMessage message) {
        WorkflowTracingContext context = new WorkflowTracingContext(
                message.getExecutionId(),
                message.getCorrelationId(),
                message.getTopic(),
                0,
                "workflow.start",
                message.getTotalSteps()
        );

        return Observation.createNotStarted(
                "thalyazin.workflow",
                () -> context,
                observationRegistry
        ).start();
    }
}