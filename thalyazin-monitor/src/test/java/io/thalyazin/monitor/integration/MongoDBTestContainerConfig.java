package io.thalyazin.monitor.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * Shared MongoDB Testcontainer for integration tests.
 * Use with @Testcontainers annotation on test classes.
 */
public abstract class MongoDBTestContainerConfig {

    @Container
    protected static final MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "thalyazin-test");
        // Disable components that might interfere with tests
        registry.add("thalyazin.monitor.retry-scheduler.enabled", () -> false);
        registry.add("thalyazin.monitor.web-socket.enabled", () -> false);
    }
}
