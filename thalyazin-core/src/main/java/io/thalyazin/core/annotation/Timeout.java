package io.thalyazin.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Sets timeout for a step or entire workflow.
 * Can be applied at class level (workflow) or method level (step).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Timeout {

    /**
     * Timeout duration.
     */
    long value();

    /**
     * Time unit (default: SECONDS).
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}