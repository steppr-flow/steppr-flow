package io.thalyazin.core.event;

import io.thalyazin.core.model.WorkflowMessage;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a workflow message is received.
 * This allows other modules (like thalyazin-monitor) to react to workflow
 * message processing without tight coupling.
 */
public class WorkflowMessageEvent extends ApplicationEvent {

    private final WorkflowMessage message;

    public WorkflowMessageEvent(Object source, WorkflowMessage message) {
        super(source);
        this.message = message;
    }

    public WorkflowMessage getMessage() {
        return message;
    }
}