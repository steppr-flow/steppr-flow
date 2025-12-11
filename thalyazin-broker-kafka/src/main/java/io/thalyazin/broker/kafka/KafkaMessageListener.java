package io.thalyazin.broker.kafka;

import io.thalyazin.core.event.WorkflowMessageEvent;
import io.thalyazin.core.model.WorkflowMessage;
import io.thalyazin.core.model.WorkflowStatus;
import io.thalyazin.core.service.StepExecutor;
import io.thalyazin.core.service.WorkflowRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * Kafka listener for workflow messages.
 * Listens to registered workflow topics and delegates to StepExecutor.
 * This bean is created by KafkaBrokerAutoConfiguration.
 */
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageListener {

    private final StepExecutor stepExecutor;
    private final WorkflowRegistry registry;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Listen to all registered workflow topics.
     */
    @KafkaListener(
            topicPattern = "${thalyazin.kafka.topic-pattern:.*-workflow.*}",
            containerFactory = "workflowKafkaListenerContainerFactory",
            groupId = "${thalyazin.kafka.consumer.group-id:thalyazin-workflow-processor}"
    )
    public void onMessage(ConsumerRecord<String, WorkflowMessage> record, Acknowledgment ack) {
        WorkflowMessage message = record.value();

        if (message == null) {
            log.warn("Received null message on topic {}", record.topic());
            ack.acknowledge();
            return;
        }

        log.info("Received workflow message: topic={}, executionId={}, step={}, status={}",
                record.topic(), message.getExecutionId(), message.getCurrentStep(), message.getStatus());

        // Publish event for monitoring/persistence
        eventPublisher.publishEvent(new WorkflowMessageEvent(this, message));

        // Only process PENDING or IN_PROGRESS messages
        if (message.getStatus() == WorkflowStatus.PENDING ||
            message.getStatus() == WorkflowStatus.IN_PROGRESS) {
            try {
                stepExecutor.execute(message);
                ack.acknowledge();
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage(), e);
                // Don't acknowledge - message will be redelivered
            }
        } else {
            log.debug("Skipping message with status {}", message.getStatus());
            ack.acknowledge();
        }
    }
}