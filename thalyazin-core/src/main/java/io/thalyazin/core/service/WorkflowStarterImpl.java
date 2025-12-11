package io.thalyazin.core.service;

import io.thalyazin.core.broker.MessageBroker;
import io.thalyazin.core.exception.WorkflowException;
import io.thalyazin.core.model.WorkflowDefinition;
import io.thalyazin.core.model.WorkflowMessage;
import io.thalyazin.core.model.WorkflowStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of WorkflowStarter.
 */
@Service
@Slf4j
public class WorkflowStarterImpl implements WorkflowStarter {

    private final WorkflowRegistry registry;
    private final MessageBroker messageBroker;
    private final String serviceName;

    public WorkflowStarterImpl(
            WorkflowRegistry registry,
            MessageBroker messageBroker,
            @Value("${spring.application.name:unknown}") String serviceName) {
        this.registry = registry;
        this.messageBroker = messageBroker;
        this.serviceName = serviceName;
    }

    @Override
    public String start(String topic, Object payload) {
        return start(topic, payload, null);
    }

    @Override
    public String start(String topic, Object payload, Map<String, Object> metadata) {
        WorkflowDefinition definition = registry.getDefinition(topic);
        if (definition == null) {
            throw new WorkflowException("Unknown workflow topic: " + topic);
        }

        String executionId = UUID.randomUUID().toString();

        WorkflowMessage message = WorkflowMessage.builder()
                .executionId(executionId)
                .correlationId(UUID.randomUUID().toString())
                .topic(topic)
                .serviceName(serviceName)
                .currentStep(1)
                .totalSteps(definition.getTotalSteps())
                .status(WorkflowStatus.PENDING)
                .payload(payload)
                .payloadType(payload.getClass().getName())
                .metadata(metadata)
                .build();

        log.info("Starting workflow: topic={}, serviceName={}, executionId={}", topic, serviceName, executionId);
        messageBroker.send(topic, message);

        return executionId;
    }

    @Override
    public CompletableFuture<String> startAsync(String topic, Object payload) {
        return CompletableFuture.supplyAsync(() -> start(topic, payload));
    }

    @Override
    public WorkflowMessage startAndGetMessage(String topic, Object payload) {
        WorkflowDefinition definition = registry.getDefinition(topic);
        if (definition == null) {
            throw new WorkflowException("Unknown workflow topic: " + topic);
        }

        String executionId = UUID.randomUUID().toString();

        WorkflowMessage message = WorkflowMessage.builder()
                .executionId(executionId)
                .correlationId(UUID.randomUUID().toString())
                .topic(topic)
                .serviceName(serviceName)
                .currentStep(1)
                .totalSteps(definition.getTotalSteps())
                .status(WorkflowStatus.PENDING)
                .payload(payload)
                .payloadType(payload.getClass().getName())
                .build();

        log.info("Starting workflow: topic={}, executionId={}", topic, executionId);
        messageBroker.send(topic, message);

        return message;
    }

    @Override
    public void resume(String executionId, Integer stepId) {
        log.info("Resume workflow is not yet implemented");
        throw new UnsupportedOperationException("Resume is implemented in async-workflow-monitor");
    }

    @Override
    public void cancel(String executionId) {
        log.info("Cancel workflow is not yet implemented");
        throw new UnsupportedOperationException("Cancel is implemented in async-workflow-monitor");
    }
}