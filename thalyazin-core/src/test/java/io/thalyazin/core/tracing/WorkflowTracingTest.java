package io.thalyazin.core.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import io.thalyazin.core.model.StepDefinition;
import io.thalyazin.core.model.WorkflowMessage;
import io.thalyazin.core.model.WorkflowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkflowTracing Tests")
class WorkflowTracingTest {

    private TestObservationRegistry observationRegistry;
    private WorkflowTracing workflowTracing;
    private WorkflowMessage testMessage;
    private StepDefinition testStep;

    @BeforeEach
    void setUp() {
        observationRegistry = TestObservationRegistry.create();
        workflowTracing = new WorkflowTracing(observationRegistry);

        testMessage = WorkflowMessage.builder()
                .executionId("exec-123")
                .correlationId("corr-456")
                .topic("order-workflow")
                .currentStep(1)
                .totalSteps(3)
                .status(WorkflowStatus.IN_PROGRESS)
                .createdAt(Instant.now())
                .build();

        testStep = StepDefinition.builder()
                .id(1)
                .label("validate-order")
                .build();
    }

    @Nested
    @DisplayName("traceStep() with Supplier")
    class TraceStepWithSupplier {

        @Test
        @DisplayName("Should create observation for successful step execution")
        void shouldCreateObservationForSuccessfulStep() throws Exception {
            String result = workflowTracing.traceStep(testMessage, testStep, () -> "success");

            assertThat(result).isEqualTo("success");

            TestObservationRegistryAssert.assertThat(observationRegistry)
                    .hasObservationWithNameEqualTo("thalyazin.workflow.step")
                    .that()
                    .hasLowCardinalityKeyValue("thalyazin.workflow.topic", "order-workflow")
                    .hasLowCardinalityKeyValue("thalyazin.workflow.step.id", "1")
                    .hasLowCardinalityKeyValue("thalyazin.workflow.step.label", "validate-order")
                    .hasLowCardinalityKeyValue("thalyazin.workflow.status", "SUCCESS")
                    .hasHighCardinalityKeyValue("thalyazin.workflow.execution.id", "exec-123")
                    .hasHighCardinalityKeyValue("thalyazin.workflow.correlation.id", "corr-456")
                    .hasBeenStopped();
        }

        @Test
        @DisplayName("Should mark observation as failed when step throws exception")
        void shouldMarkObservationAsFailedOnException() {
            RuntimeException expectedException = new RuntimeException("Step failed");

            assertThatThrownBy(() ->
                    workflowTracing.traceStep(testMessage, testStep, () -> {
                        throw expectedException;
                    })
            ).isInstanceOf(RuntimeException.class).hasMessage("Step failed");

            TestObservationRegistryAssert.assertThat(observationRegistry)
                    .hasObservationWithNameEqualTo("thalyazin.workflow.step")
                    .that()
                    .hasLowCardinalityKeyValue("thalyazin.workflow.status", "FAILED")
                    .hasError()
                    .hasBeenStopped();
        }

        @Test
        @DisplayName("Should include execution ID in high cardinality keys")
        void shouldIncludeExecutionIdInHighCardinalityKeys() throws Exception {
            workflowTracing.traceStep(testMessage, testStep, () -> null);

            TestObservationRegistryAssert.assertThat(observationRegistry)
                    .hasObservationWithNameEqualTo("thalyazin.workflow.step")
                    .that()
                    .hasHighCardinalityKeyValue("thalyazin.workflow.execution.id", "exec-123");
        }

        @Test
        @DisplayName("Should handle null correlation ID")
        void shouldHandleNullCorrelationId() throws Exception {
            WorkflowMessage messageWithoutCorrelation = WorkflowMessage.builder()
                    .executionId("exec-789")
                    .topic("test-workflow")
                    .currentStep(1)
                    .totalSteps(1)
                    .status(WorkflowStatus.IN_PROGRESS)
                    .createdAt(Instant.now())
                    .build();

            workflowTracing.traceStep(messageWithoutCorrelation, testStep, () -> null);

            TestObservationRegistryAssert.assertThat(observationRegistry)
                    .hasObservationWithNameEqualTo("thalyazin.workflow.step")
                    .that()
                    .hasHighCardinalityKeyValue("thalyazin.workflow.correlation.id", "");
        }
    }

    @Nested
    @DisplayName("traceStep() with Runnable")
    class TraceStepWithRunnable {

        @Test
        @DisplayName("Should create observation for void step execution")
        void shouldCreateObservationForVoidStep() throws Exception {
            boolean[] executed = {false};

            workflowTracing.traceStep(testMessage, testStep, () -> executed[0] = true);

            assertThat(executed[0]).isTrue();

            TestObservationRegistryAssert.assertThat(observationRegistry)
                    .hasObservationWithNameEqualTo("thalyazin.workflow.step")
                    .that()
                    .hasLowCardinalityKeyValue("thalyazin.workflow.status", "SUCCESS")
                    .hasBeenStopped();
        }

        @Test
        @DisplayName("Should propagate exception from runnable")
        void shouldPropagateExceptionFromRunnable() {
            assertThatThrownBy(() ->
                    workflowTracing.traceStep(testMessage, testStep, () -> {
                        throw new IllegalStateException("Runnable failed");
                    })
            ).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("startWorkflowObservation()")
    class StartWorkflowObservation {

        @Test
        @DisplayName("Should start workflow observation")
        void shouldStartWorkflowObservation() {
            Observation observation = workflowTracing.startWorkflowObservation(testMessage);

            assertThat(observation).isNotNull();

            observation.stop();

            TestObservationRegistryAssert.assertThat(observationRegistry)
                    .hasObservationWithNameEqualTo("thalyazin.workflow")
                    .that()
                    .hasBeenStarted()
                    .hasBeenStopped();
        }
    }

    @Nested
    @DisplayName("Contextual naming")
    class ContextualNaming {

        @Test
        @DisplayName("Should use topic and step label for contextual name")
        void shouldUseTopicAndStepLabelForContextualName() throws Exception {
            workflowTracing.traceStep(testMessage, testStep, () -> null);

            TestObservationRegistryAssert.assertThat(observationRegistry)
                    .hasObservationWithNameEqualTo("thalyazin.workflow.step")
                    .that()
                    .hasContextualNameEqualTo("order-workflow.validate-order");
        }
    }

    @Nested
    @DisplayName("With NOOP registry")
    class WithNoopRegistry {

        @Test
        @DisplayName("Should work with NOOP observation registry")
        void shouldWorkWithNoopRegistry() throws Exception {
            WorkflowTracing noopTracing = new WorkflowTracing(ObservationRegistry.NOOP);

            String result = noopTracing.traceStep(testMessage, testStep, () -> "result");

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("Should propagate exceptions with NOOP registry")
        void shouldPropagateExceptionsWithNoopRegistry() {
            WorkflowTracing noopTracing = new WorkflowTracing(ObservationRegistry.NOOP);

            assertThatThrownBy(() ->
                    noopTracing.traceStep(testMessage, testStep, () -> {
                        throw new RuntimeException("NOOP test");
                    })
            ).isInstanceOf(RuntimeException.class);
        }
    }
}