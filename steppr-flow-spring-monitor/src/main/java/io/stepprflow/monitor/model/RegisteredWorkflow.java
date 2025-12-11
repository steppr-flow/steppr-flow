package io.stepprflow.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * MongoDB document for registered workflow definitions.
 * Workflows are registered by services using stepprflow-agent at startup.
 */
@Document(collection = "registered_workflows")
@CompoundIndex(name = "topic_serviceName_idx", def = "{'topic': 1, 'serviceName': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredWorkflow {

    @Id
    private String id;

    /**
     * Workflow topic.
     */
    @Indexed
    private String topic;

    /**
     * Service name that provides this workflow.
     * Together with topic, forms a unique composite key.
     */
    @Indexed
    private String serviceName;

    /**
     * Workflow description.
     */
    private String description;

    /**
     * List of steps in this workflow.
     */
    private List<StepInfo> steps;

    /**
     * Number of Kafka partitions.
     */
    private int partitions;

    /**
     * Kafka replication factor.
     */
    private short replication;

    /**
     * Workflow timeout in milliseconds.
     */
    private Long timeoutMs;

    /**
     * Services that provide this workflow.
     */
    private Set<ServiceInstance> registeredBy;

    /**
     * Workflow status (ACTIVE when at least one instance is alive, INACTIVE otherwise).
     */
    @Builder.Default
    private Status status = Status.ACTIVE;

    /**
     * First registration time.
     */
    private Instant createdAt;

    /**
     * Last update time.
     */
    @Indexed
    private Instant updatedAt;

    /**
     * Step information (serializable).
     */
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

    /**
     * Service instance information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInstance {
        private String serviceName;
        private String instanceId;
        private String host;
        private int port;
        private Instant lastHeartbeat;
    }

    /**
     * Workflow registration status.
     */
    public enum Status {
        /**
         * At least one service instance is actively providing this workflow.
         */
        ACTIVE,

        /**
         * No active service instances are providing this workflow.
         * The workflow definition is preserved for reference.
         */
        INACTIVE
    }
}
