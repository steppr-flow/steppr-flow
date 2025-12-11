package io.thalyazin.monitor.integration;

import io.thalyazin.core.broker.MessageBroker;
import io.thalyazin.core.metrics.WorkflowMetrics;
import io.thalyazin.monitor.MonitorProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.mockito.Mockito.mock;

/**
 * Test application for integration tests.
 * Required because thalyazin-monitor is a library without a main application class.
 */
@SpringBootApplication(scanBasePackages = {"io.thalyazin.monitor"})
@EnableConfigurationProperties(MonitorProperties.class)
@EnableScheduling
@EnableAsync
public class TestApplication {

    @Bean
    @Primary
    public MessageBroker messageBroker() {
        return mock(MessageBroker.class);
    }

    @Bean
    @Primary
    public WorkflowMetrics workflowMetrics() {
        return mock(WorkflowMetrics.class);
    }
}