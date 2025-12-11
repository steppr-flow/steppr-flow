package io.thalyazin.monitor.service;

import io.thalyazin.core.broker.MessageBroker;
import io.thalyazin.core.model.WorkflowMessage;
import io.thalyazin.core.model.WorkflowStatus;
import io.thalyazin.monitor.model.WorkflowExecution;
import io.thalyazin.monitor.repository.WorkflowExecutionRepository;
import io.thalyazin.monitor.util.WorkflowMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for workflow command operations.
 * Handles state-changing operations like resume and cancel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowCommandService {

    private final WorkflowExecutionRepository repository;
    private final MessageBroker messageBroker;
    private final WorkflowMessageFactory messageFactory;

    /**
     * Resume a failed or paused workflow.
     */
    public void resume(String executionId, Integer fromStep, String resumedBy) {
        WorkflowExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        if (execution.getStatus() != WorkflowStatus.FAILED &&
            execution.getStatus() != WorkflowStatus.PAUSED &&
            execution.getStatus() != WorkflowStatus.RETRY_PENDING) {
            throw new IllegalStateException("Cannot resume execution with status: " + execution.getStatus());
        }

        int step = fromStep != null ? fromStep : execution.getCurrentStep();

        // Create a new execution attempt
        createExecutionAttempt(execution, step, resumedBy);

        WorkflowMessage message = messageFactory.createResumeMessage(execution, step);

        log.info("Resuming workflow {} from step {}", executionId, step);
        messageBroker.send(execution.getTopic(), message);
    }

    /**
     * Cancel a running workflow.
     */
    public void cancel(String executionId) {
        WorkflowExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        if (execution.getStatus() == WorkflowStatus.COMPLETED ||
            execution.getStatus() == WorkflowStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel execution with status: " + execution.getStatus());
        }

        execution.setStatus(WorkflowStatus.CANCELLED);
        execution.setUpdatedAt(Instant.now());
        execution.setCompletedAt(Instant.now());
        repository.save(execution);

        log.info("Cancelled workflow {}", executionId);
    }

    /**
     * Create a new execution attempt, moving pending payload changes to this attempt.
     */
    private void createExecutionAttempt(WorkflowExecution execution, int startStep, String resumedBy) {
        // Initialize attempts list if null
        if (execution.getExecutionAttempts() == null) {
            execution.setExecutionAttempts(new ArrayList<>());
        }

        // Determine attempt number
        int attemptNumber = execution.getExecutionAttempts().size() + 1;

        // Get pending payload changes and clear them
        List<WorkflowExecution.PayloadChange> pendingChanges = execution.getPayloadHistory();
        execution.setPayloadHistory(new ArrayList<>());

        // Create the attempt
        WorkflowExecution.ExecutionAttempt attempt = WorkflowExecution.ExecutionAttempt.builder()
                .attemptNumber(attemptNumber)
                .startedAt(Instant.now())
                .startStep(startStep)
                .resumedBy(attemptNumber > 1 ? resumedBy : null)
                .payloadChanges(pendingChanges != null && !pendingChanges.isEmpty() ? pendingChanges : null)
                .build();

        execution.getExecutionAttempts().add(attempt);
        execution.setUpdatedAt(Instant.now());
        repository.save(execution);

        log.info("Created execution attempt {} for workflow {}", attemptNumber, execution.getExecutionId());
    }
}
