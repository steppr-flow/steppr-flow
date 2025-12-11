package io.thalyazin.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an async workflow handler.
 * The topic name is used as the Kafka topic for this workflow.
 *
 * @example
 * <pre>
 * {@code @Service}
 * {@code @Topic("order-processing")}
 * public class OrderWorkflow implements Thalyazin { }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Topic {

    /**
     * Kafka topic name for this workflow.
     * Should be unique across the application.
     */
    String value();

    /**
     * Optional description for documentation.
     */
    String description() default "";

    /**
     * Number of partitions for auto-created topic.
     */
    int partitions() default 1;

    /**
     * Replication factor for auto-created topic.
     */
    short replication() default 1;
}