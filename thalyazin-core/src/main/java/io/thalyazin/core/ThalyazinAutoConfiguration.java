package io.thalyazin.core;

import io.thalyazin.core.service.StepExecutor;
import io.thalyazin.core.service.WorkflowRegistry;
import io.thalyazin.core.service.WorkflowStarterImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Thalyazin core components.
 * This configures the core workflow infrastructure.
 * Broker-specific configurations (Kafka, RabbitMQ) are in separate modules.
 */
@AutoConfiguration
@EnableConfigurationProperties(ThalyazinProperties.class)
@ConditionalOnProperty(prefix = "thalyazin", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
        WorkflowRegistry.class,
        StepExecutor.class,
        WorkflowStarterImpl.class
})
@ComponentScan(basePackages = "io.thalyazin.core")
public class ThalyazinAutoConfiguration {
}
