package io.stepprflow.monitor.model;

import io.stepprflow.core.model.ErrorInfo;
import io.stepprflow.core.model.RetryInfo;
import io.stepprflow.core.model.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document for workflow execution.
 */
@Document(collection = "workflow_executions")
@CompoundIndex(name = "topic_status", def = "{'topic': 1, 'status': 1}")
@CompoundIndex(name = "status_createdAt", def = "{'status': 1, 'createdAt': -1}")
@CompoundIndex(name = "status_completedAt", def = "{'status': 1, 'completedAt': 1}")
@CompoundIndex(name = "status_nextRetry", def = "{'status': 1, 'retryInfo.nextRetryAt': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {

    @Id
    private String executionId;

    /**
     * Version field for optimistic locking.
     * Automatically managed by Spring Data MongoDB.
     */
    @Version
    private Long version;

    private String correlationId;

    @Indexed
    private String topic;

    @Indexed
    private WorkflowStatus status;

    private int currentStep;

    private int totalSteps;

    private Object payload;

    private String payloadType;

    private String securityContext;

    private Map<String, Object> metadata;

    private RetryInfo retryInfo;

    private ErrorInfo errorInfo;

    /**
     * History of step executions.
     */
    private List<StepExecution> stepHistory;

    /**
     * History of payload changes (pending changes before next resume).
     */
    private List<PayloadChange> payloadHistory;

    /**
     * History of execution attempts.
     */
    private List<ExecutionAttempt> executionAttempts;

    @Indexed
    private Instant createdAt;

    private Instant updatedAt;

    private Instant completedAt;

    /**
     * User who started the workflow.
     */
    private String initiatedBy;

    /**
     * Duration in milliseconds.
     */
    private Long durationMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepExecution {
        private int stepId;
        private String stepLabel;
        private WorkflowStatus status;
        private Instant startedAt;
        private Instant completedAt;
        private Long durationMs;
        private String errorMessage;
        private int attempt;
    }

    /**
     * Record of a payload field change.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayloadChange {
        private String fieldPath;
        private Object oldValue;
        private Object newValue;
        private Instant changedAt;
        private String changedBy;
        private String reason;
    }

    /**
     * Record of an execution attempt.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionAttempt {
        private int attemptNumber;
        private Instant startedAt;
        private Instant endedAt;
        private WorkflowStatus result;
        private int startStep;
        private int endStep;
        private String errorMessage;
        private String resumedBy;
        /**
         * Payload changes applied before this attempt.
         */
        private List<PayloadChange> payloadChanges;
    }
}
