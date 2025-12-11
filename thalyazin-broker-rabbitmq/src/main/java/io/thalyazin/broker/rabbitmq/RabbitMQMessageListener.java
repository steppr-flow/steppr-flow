package io.thalyazin.broker.rabbitmq;

import com.rabbitmq.client.Channel;
import io.thalyazin.core.event.WorkflowMessageEvent;
import io.thalyazin.core.model.WorkflowMessage;
import io.thalyazin.core.model.WorkflowStatus;
import io.thalyazin.core.service.StepExecutor;
import io.thalyazin.core.service.WorkflowRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.ApplicationEventPublisher;

/**
 * RabbitMQ listener for workflow messages.
 * Listens to workflow queues and delegates to StepExecutor.
 * This bean is created by RabbitMQBrokerAutoConfiguration.
 */
@RequiredArgsConstructor
@Slf4j
public class RabbitMQMessageListener {

    private final StepExecutor stepExecutor;
    private final WorkflowRegistry registry;
    private final MessageConverter messageConverter;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Listen to workflow queue.
     * Queues are dynamically created based on registered workflows.
     */
    @RabbitListener(
            queues = "#{@rabbitMQQueueInitializer.workflowQueueNames}",
            containerFactory = "workflowRabbitListenerContainerFactory",
            ackMode = "MANUAL"
    )
    public void onMessage(Message message, Channel channel) {
        try {
            WorkflowMessage workflowMessage = (WorkflowMessage) messageConverter.fromMessage(message);
            String queueName = message.getMessageProperties().getConsumerQueue();

            if (workflowMessage == null) {
                log.warn("Received null message on queue {}", queueName);
                acknowledgeMessage(channel, message);
                return;
            }

            log.info("Received workflow message: queue={}, executionId={}, step={}, status={}",
                    queueName, workflowMessage.getExecutionId(),
                    workflowMessage.getCurrentStep(), workflowMessage.getStatus());

            // Publish event for monitoring/persistence
            eventPublisher.publishEvent(new WorkflowMessageEvent(this, workflowMessage));

            // Only process PENDING or IN_PROGRESS messages
            if (workflowMessage.getStatus() == WorkflowStatus.PENDING ||
                workflowMessage.getStatus() == WorkflowStatus.IN_PROGRESS) {
                try {
                    stepExecutor.execute(workflowMessage);
                    acknowledgeMessage(channel, message);
                } catch (Exception e) {
                    log.error("Error processing message: {}", e.getMessage(), e);
                    // Reject and requeue the message
                    rejectMessage(channel, message, true);
                }
            } else {
                log.debug("Skipping message with status {}", workflowMessage.getStatus());
                acknowledgeMessage(channel, message);
            }
        } catch (Exception e) {
            log.error("Error deserializing message: {}", e.getMessage(), e);
            // Reject without requeue for invalid messages
            rejectMessage(channel, message, false);
        }
    }

    private void acknowledgeMessage(Channel channel, Message message) {
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Failed to acknowledge message", e);
        }
    }

    private void rejectMessage(Channel channel, Message message, boolean requeue) {
        try {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), requeue);
        } catch (Exception e) {
            log.error("Failed to reject message", e);
        }
    }
}