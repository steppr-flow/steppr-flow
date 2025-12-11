package io.thalyazin.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a workflow step.
 * Steps are executed in order of their ID.
 *
 * @example
 * <pre>
 * {@code @Step(id = 1, label = "Validate order")}
 * public void validateOrder(OrderPayload payload) {
 *     // validation logic
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Step {

    /**
     * Step ID determining execution order.
     * Must be unique within a workflow and greater than 0.
     */
    int id();

    /**
     * Human-readable label for this step.
     */
    String label();

    /**
     * Optional description.
     */
    String description() default "";

    /**
     * Whether this step can be skipped on retry.
     */
    boolean skippable() default false;

    /**
     * Whether to continue to next step on failure.
     */
    boolean continueOnFailure() default false;
}