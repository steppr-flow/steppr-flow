package io.thalyazin.core.tracing;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for workflow tracing.
 * Automatically configures WorkflowTracing when Micrometer Observation is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(ObservationRegistry.class)
public class TracingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WorkflowTracing workflowTracing(ObservationRegistry observationRegistry) {
        return new WorkflowTracing(observationRegistry);
    }
}