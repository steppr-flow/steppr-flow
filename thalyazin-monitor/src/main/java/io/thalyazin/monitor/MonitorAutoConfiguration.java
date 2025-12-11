package io.thalyazin.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.thalyazin.core.metrics.WorkflowMetrics;
import io.thalyazin.core.metrics.WorkflowMetricsListener;
import io.thalyazin.monitor.config.WebSocketConfig;
import io.thalyazin.monitor.controller.CircuitBreakerController;
import io.thalyazin.monitor.controller.GlobalExceptionHandler;
import io.thalyazin.monitor.controller.MetricsController;
import io.thalyazin.monitor.controller.WorkflowController;
import io.thalyazin.monitor.repository.WorkflowExecutionRepository;
import io.thalyazin.monitor.service.ExecutionPersistenceService;
import io.thalyazin.monitor.service.RetrySchedulerService;
import io.thalyazin.monitor.service.WorkflowMonitorService;
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
 * Auto-configuration for Thalyazin Monitor module.
 */
@AutoConfiguration
@EnableConfigurationProperties(MonitorProperties.class)
@ConditionalOnProperty(prefix = "thalyazin.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableMongoRepositories(basePackageClasses = WorkflowExecutionRepository.class)
@EnableScheduling
@EnableAsync
@Import({
        WebSocketConfig.class,
        GlobalExceptionHandler.class,
        WorkflowController.class,
        CircuitBreakerController.class,
        ExecutionPersistenceService.class,
        WorkflowMonitorService.class,
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