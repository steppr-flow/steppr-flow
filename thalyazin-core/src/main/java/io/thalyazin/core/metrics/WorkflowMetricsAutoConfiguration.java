package io.thalyazin.core.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Thalyazin workflow metrics.
 * Configures WorkflowMetrics bean only when MeterRegistry is available.
 * Runs after metrics auto-configuration to ensure MeterRegistry exists.
 */
@AutoConfiguration(afterName = "org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration")
@ConditionalOnClass(MeterRegistry.class)
public class WorkflowMetricsAutoConfiguration {

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public WorkflowMetrics workflowMetrics(MeterRegistry meterRegistry) {
        return new WorkflowMetrics(meterRegistry);
    }

    @Bean
    @ConditionalOnBean(WorkflowMetrics.class)
    public WorkflowMetricsListener workflowMetricsListener(WorkflowMetrics workflowMetrics) {
        return new WorkflowMetricsListener(workflowMetrics);
    }
}