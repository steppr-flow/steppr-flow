package io.thalyazin.core.exception;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.Getter;

/**
 * Exception thrown when a circuit breaker is open and rejects calls.
 * <p>
 * This exception indicates that the broker is experiencing failures
 * and the circuit breaker has opened to prevent cascading failures.
 * </p>
 */
@Getter
public class CircuitBreakerOpenException extends MessageBrokerException {

    private final String circuitBreakerName;
    private final CircuitBreaker.State state;

    public CircuitBreakerOpenException(String circuitBreakerName, CircuitBreaker.State state) {
        super(String.format("Circuit breaker '%s' is %s - calls are not permitted",
                circuitBreakerName, state));
        this.circuitBreakerName = circuitBreakerName;
        this.state = state;
    }

    public CircuitBreakerOpenException(String circuitBreakerName, CircuitBreaker.State state, Throwable cause) {
        super(String.format("Circuit breaker '%s' is %s - calls are not permitted",
                circuitBreakerName, state), cause);
        this.circuitBreakerName = circuitBreakerName;
        this.state = state;
    }
}