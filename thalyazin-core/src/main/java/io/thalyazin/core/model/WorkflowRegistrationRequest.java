package io.thalyazin.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for registering workflow definitions with the monitoring server.
 * This is a serializable version of WorkflowDefinition without runtime references.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRegistrationRequest {

    /**
     * Service name (application name).
     */
    private String serviceName;

    /**
     * Service instance ID (for multiple instances).
     */
    private String instanceId;

    /**
     * Service host/address.
     */
    private String host;

    /**
     * Service port.
     */
    private int port;

    /**
     * List of workflow definitions.
     */
    private List<WorkflowInfo> workflows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowInfo {
        private String topic;
        private String description;
        private List<StepInfo> steps;
        private int partitions;
        private short replication;
        private Long timeoutMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepInfo {
        private int id;
        private String label;
        private String description;
        private boolean skippable;
        private boolean continueOnFailure;
        private Long timeoutMs;
    }
}
