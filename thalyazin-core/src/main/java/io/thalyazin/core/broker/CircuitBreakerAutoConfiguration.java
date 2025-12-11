package io.thalyazin.core.broker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.thalyazin.core.ThalyazinProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;

/**
 * Auto-configuration for Circuit Breaker integration with MessageBroker.
 * <p>
 * This configuration wraps any existing MessageBroker with a ResilientMessageBroker
 * that provides circuit breaker protection.
 * </p>
 */
@Configuration
@ConditionalOnClass(CircuitBreakerRegistry.class)
@Slf4j
public class CircuitBreakerAutoConfiguration {

    /**
     * Default circuit breaker names to pre-register at startup.
     */
    private static final List<String> DEFAULT_CIRCUIT_BREAKERS = List.of(
            "broker-kafka",
            "broker-rabbitmq",
            "workflow-execution"
    );

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(ThalyazinProperties properties) {
        log.info("Creating default CircuitBreakerRegistry");

        ThalyazinProperties.CircuitBreaker cbProps = properties.getCircuitBreaker();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(cbProps.getFailureRateThreshold())
                .slowCallRateThreshold(cbProps.getSlowCallRateThreshold())
                .slowCallDurationThreshold(cbProps.getSlowCallDurationThreshold())
                .slidingWindowSize(cbProps.getSlidingWindowSize())
                .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(cbProps.getPermittedNumberOfCallsInHalfOpenState())
                .waitDurationInOpenState(cbProps.getWaitDurationInOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(cbProps.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        // Pre-register default circuit breakers
        for (String name : DEFAULT_CIRCUIT_BREAKERS) {
            CircuitBreaker cb = registry.circuitBreaker(name);
            log.info("Pre-registered circuit breaker: {} (state: {})", name, cb.getState());
        }

        return registry;
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public TaggedCircuitBreakerMetrics circuitBreakerMetrics(
            CircuitBreakerRegistry registry,
            MeterRegistry meterRegistry) {
        log.info("Registering circuit breaker metrics with Micrometer");
        TaggedCircuitBreakerMetrics metrics = TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    /**
     * Configuration for wrapping the primary broker with circuit breaker.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "thalyazin.circuit-breaker", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class ResilientBrokerConfiguration {

        @Bean
        @Primary
        @ConditionalOnBean(MessageBroker.class)
        public ResilientMessageBroker resilientMessageBroker(
                MessageBroker delegate,
                ThalyazinProperties properties,
                CircuitBreakerRegistry registry) {
            log.info("Wrapping MessageBroker '{}' with circuit breaker protection",
                    delegate.getBrokerType());
            return new ResilientMessageBroker(delegate, properties.getCircuitBreaker(), registry);
        }
    }
}