package io.stepprflow.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.stepprflow.core.metrics.WorkflowMetrics;
import io.stepprflow.core.metrics.WorkflowMetricsListener;
import io.stepprflow.monitor.config.OpenApiConfig;
import io.stepprflow.monitor.config.WebSocketConfig;
import io.stepprflow.monitor.controller.CircuitBreakerController;
import io.stepprflow.monitor.controller.GlobalExceptionHandler;
import io.stepprflow.monitor.controller.MetricsController;
import io.stepprflow.monitor.controller.WorkflowController;
import io.stepprflow.monitor.repository.WorkflowExecutionRepository;
import io.stepprflow.monitor.service.ExecutionPersistenceService;
import io.stepprflow.monitor.service.RetrySchedulerService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auto-configuration for StepprFlow Monitor module.
 */
@AutoConfiguration
@EnableConfigurationProperties(MonitorProperties.class)
@ConditionalOnProperty(prefix = "stepprflow.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableMongoRepositories(basePackageClasses = WorkflowExecutionRepository.class)
@EnableScheduling
@EnableAsync
@Import({
        OpenApiConfig.class,
        WebSocketConfig.class,
        GlobalExceptionHandler.class,
        WorkflowController.class,
        CircuitBreakerController.class,
        ExecutionPersistenceService.class,
        RetrySchedulerService.class
})
public class MonitorAutoConfiguration {

    /**
     * Creates WorkflowMetrics bean.
     */
    @Bean
    @ConditionalOnMissingBean(WorkflowMetrics.class)
    public WorkflowMetrics workflowMetrics(MeterRegistry meterRegistry) {
        return new WorkflowMetrics(meterRegistry);
    }

    /**
     * Creates WorkflowMetricsListener.
     */
    @Bean
    @ConditionalOnMissingBean(WorkflowMetricsListener.class)
    public WorkflowMetricsListener workflowMetricsListener(WorkflowMetrics workflowMetrics) {
        return new WorkflowMetricsListener(workflowMetrics);
    }

    /**
     * Creates MetricsController.
     */
    @Bean
    public MetricsController metricsController(WorkflowMetrics workflowMetrics) {
        return new MetricsController(workflowMetrics);
    }
}
