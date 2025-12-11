package io.thalyazin.core.tracing;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * Observation convention for workflow step execution.
 * Defines the naming and tagging conventions for tracing workflow steps.
 */
public class WorkflowTracingObservationConvention implements ObservationConvention<WorkflowTracingContext> {

    public static final WorkflowTracingObservationConvention INSTANCE = new WorkflowTracingObservationConvention();

    @Override
    public String getName() {
        return "thalyazin.workflow.step";
    }

    @Override
    public String getContextualName(WorkflowTracingContext context) {
        return context.getTopic() + "." + context.getStepLabel();
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(WorkflowTracingContext context) {
        return KeyValues.of(
                "thalyazin.workflow.topic", context.getTopic(),
                "thalyazin.workflow.step.id", String.valueOf(context.getStepId()),
                "thalyazin.workflow.step.label", context.getStepLabel(),
                "thalyazin.workflow.status", context.getStatus()
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(WorkflowTracingContext context) {
        return KeyValues.of(
                "thalyazin.workflow.execution.id", context.getExecutionId(),
                "thalyazin.workflow.correlation.id", context.getCorrelationId() != null ? context.getCorrelationId() : ""
        );
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof WorkflowTracingContext;
    }
}