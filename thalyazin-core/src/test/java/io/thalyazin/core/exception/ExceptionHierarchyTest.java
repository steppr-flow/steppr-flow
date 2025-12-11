package io.thalyazin.core.exception;

import io.thalyazin.core.model.WorkflowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the exception hierarchy.
 */
@DisplayName("Exception Hierarchy Tests")
class ExceptionHierarchyTest {

    @Nested
    @DisplayName("WorkflowException (base)")
    class WorkflowExceptionTests {

        @Test
        @DisplayName("should be a RuntimeException")
        void shouldBeRuntimeException() {
            WorkflowException exception = new WorkflowException("test");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should preserve message")
        void shouldPreserveMessage() {
            WorkflowException exception = new WorkflowException("test message");
            assertThat(exception.getMessage()).isEqualTo("test message");
        }

        @Test
        @DisplayName("should preserve cause")
        void shouldPreserveCause() {
            Exception cause = new IllegalStateException("original");
            WorkflowException exception = new WorkflowException("wrapper", cause);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("WorkflowNotFoundException")
    class WorkflowNotFoundExceptionTests {

        @Test
        @DisplayName("should extend WorkflowException")
        void shouldExtendWorkflowException() {
            WorkflowNotFoundException exception = new WorkflowNotFoundException("exec-123");
            assertThat(exception).isInstanceOf(WorkflowException.class);
        }

        @Test
        @DisplayName("should contain execution ID in message")
        void shouldContainExecutionIdInMessage() {
            WorkflowNotFoundException exception = new WorkflowNotFoundException("exec-123");
            assertThat(exception.getMessage()).contains("exec-123");
            assertThat(exception.getExecutionId()).isEqualTo("exec-123");
        }
    }

    @Nested
    @DisplayName("WorkflowStateException")
    class WorkflowStateExceptionTests {

        @Test
        @DisplayName("should extend WorkflowException")
        void shouldExtendWorkflowException() {
            WorkflowStateException exception = new WorkflowStateException(
                    "exec-123", WorkflowStatus.COMPLETED, "resumed");
            assertThat(exception).isInstanceOf(WorkflowException.class);
        }

        @Test
        @DisplayName("should contain all context information")
        void shouldContainAllContextInformation() {
            WorkflowStateException exception = new WorkflowStateException(
                    "exec-123", WorkflowStatus.COMPLETED, "resumed");

            assertThat(exception.getExecutionId()).isEqualTo("exec-123");
            assertThat(exception.getCurrentStatus()).isEqualTo(WorkflowStatus.COMPLETED);
            assertThat(exception.getOperation()).isEqualTo("resumed");
            assertThat(exception.getMessage()).contains("COMPLETED");
            assertThat(exception.getMessage()).contains("resumed");
        }
    }

    @Nested
    @DisplayName("StepTimeoutException")
    class StepTimeoutExceptionTests {

        @Test
        @DisplayName("should extend WorkflowException")
        void shouldExtendWorkflowException() {
            StepTimeoutException exception = new StepTimeoutException(
                    "stepLabel", 1, Duration.ofSeconds(30));
            assertThat(exception).isInstanceOf(WorkflowException.class);
        }

        @Test
        @DisplayName("should contain timeout information")
        void shouldContainTimeoutInformation() {
            StepTimeoutException exception = new StepTimeoutException(
                    "processPayment", 2, Duration.ofSeconds(30), Duration.ofSeconds(45));

            assertThat(exception.getStepLabel()).isEqualTo("processPayment");
            assertThat(exception.getStepId()).isEqualTo(2);
            assertThat(exception.getTimeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(exception.getElapsed()).isEqualTo(Duration.ofSeconds(45));
            assertThat(exception.getMessage()).contains("processPayment");
            assertThat(exception.getMessage()).contains("timed out");
        }
    }

    @Nested
    @DisplayName("StepExecutionException")
    class StepExecutionExceptionTests {

        @Test
        @DisplayName("should extend WorkflowException")
        void shouldExtendWorkflowException() {
            StepExecutionException exception = new StepExecutionException(
                    "stepLabel", 1, "Error message");
            assertThat(exception).isInstanceOf(WorkflowException.class);
        }

        @Test
        @DisplayName("should contain step information")
        void shouldContainStepInformation() {
            StepExecutionException exception = new StepExecutionException(
                    "validateOrder", 3, "Validation failed", new IllegalArgumentException("bad input"));

            assertThat(exception.getStepLabel()).isEqualTo("validateOrder");
            assertThat(exception.getStepId()).isEqualTo(3);
            assertThat(exception.getMessage()).contains("validateOrder");
            assertThat(exception.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("MessageBrokerException")
    class MessageBrokerExceptionTests {

        @Test
        @DisplayName("should extend WorkflowException")
        void shouldExtendWorkflowException() {
            MessageBrokerException exception = new MessageBrokerException("Failed to send");
            assertThat(exception).isInstanceOf(WorkflowException.class);
        }

        @Test
        @DisplayName("should include broker type when provided")
        void shouldIncludeBrokerType() {
            MessageBrokerException exception = new MessageBrokerException(
                    "kafka", "Failed to connect");
            assertThat(exception.getBrokerType()).isEqualTo("kafka");
            assertThat(exception.getMessage()).contains("kafka");
        }
    }

    @Nested
    @DisplayName("BrokerConnectionException")
    class BrokerConnectionExceptionTests {

        @Test
        @DisplayName("should extend MessageBrokerException")
        void shouldExtendMessageBrokerException() {
            BrokerConnectionException exception = new BrokerConnectionException(
                    "kafka", "localhost:9092", "Connection refused");
            assertThat(exception).isInstanceOf(MessageBrokerException.class);
        }

        @Test
        @DisplayName("should contain connection details")
        void shouldContainConnectionDetails() {
            BrokerConnectionException exception = new BrokerConnectionException(
                    "kafka", "localhost:9092", "Connection refused");

            assertThat(exception.getBrokerType()).isEqualTo("kafka");
            assertThat(exception.getBootstrapServers()).isEqualTo("localhost:9092");
            assertThat(exception.getMessage()).contains("localhost:9092");
        }
    }

    @Nested
    @DisplayName("MessageSendException")
    class MessageSendExceptionTests {

        @Test
        @DisplayName("should extend MessageBrokerException")
        void shouldExtendMessageBrokerException() {
            MessageSendException exception = new MessageSendException(
                    "kafka", "order-topic", "Failed to send");
            assertThat(exception).isInstanceOf(MessageBrokerException.class);
        }

        @Test
        @DisplayName("should contain topic information")
        void shouldContainTopicInformation() {
            MessageSendException exception = new MessageSendException(
                    "rabbitmq", "payment-queue", "Queue full");

            assertThat(exception.getBrokerType()).isEqualTo("rabbitmq");
            assertThat(exception.getTopic()).isEqualTo("payment-queue");
            assertThat(exception.getMessage()).contains("payment-queue");
        }
    }

    @Nested
    @DisplayName("WorkflowDefinitionException")
    class WorkflowDefinitionExceptionTests {

        @Test
        @DisplayName("should extend WorkflowException")
        void shouldExtendWorkflowException() {
            WorkflowDefinitionException exception = new WorkflowDefinitionException(
                    "Invalid workflow definition");
            assertThat(exception).isInstanceOf(WorkflowException.class);
        }

        @Test
        @DisplayName("should include topic when provided")
        void shouldIncludeTopic() {
            WorkflowDefinitionException exception = new WorkflowDefinitionException(
                    "order-workflow", "No steps defined");

            assertThat(exception.getTopic()).isEqualTo("order-workflow");
            assertThat(exception.getMessage()).contains("order-workflow");
        }
    }

    @Nested
    @DisplayName("RetryExhaustedException")
    class RetryExhaustedExceptionTests {

        @Test
        @DisplayName("should extend WorkflowException")
        void shouldExtendWorkflowException() {
            RetryExhaustedException exception = new RetryExhaustedException(
                    "exec-123", 3, "All retries failed");
            assertThat(exception).isInstanceOf(WorkflowException.class);
        }

        @Test
        @DisplayName("should contain retry information")
        void shouldContainRetryInformation() {
            RetryExhaustedException exception = new RetryExhaustedException(
                    "exec-456", 5, "Database unavailable");

            assertThat(exception.getExecutionId()).isEqualTo("exec-456");
            assertThat(exception.getAttempts()).isEqualTo(5);
            assertThat(exception.getMessage()).contains("exec-456");
            assertThat(exception.getMessage()).contains("5");
        }
    }
}