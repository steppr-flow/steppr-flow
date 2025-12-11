package io.thalyazin.broker.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.thalyazin.core.model.WorkflowMessage;
import io.thalyazin.core.model.WorkflowStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for RabbitMQMessageBroker using Testcontainers.
 */
@SpringBootTest(classes = RabbitMQMessageBrokerIntegrationTest.TestConfig.class,
        properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration")
@Testcontainers
class RabbitMQMessageBrokerIntegrationTest {

    private static final String TEST_EXCHANGE = "test-thalyazin-exchange";
    private static final String TEST_QUEUE = "test-workflow-queue";
    private static final String TEST_ROUTING_KEY = "test-workflow-topic";

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void rabbitmqProperties(DynamicPropertyRegistry registry) {
        registry.add("test.rabbitmq.host", rabbitmq::getHost);
        registry.add("test.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("test.rabbitmq.username", () -> "guest");
        registry.add("test.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private RabbitMQMessageBroker messageBroker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @BeforeEach
    void setUp() {
        // Declare exchange, queue and binding
        DirectExchange exchange = new DirectExchange(TEST_EXCHANGE);
        Queue queue = new Queue(TEST_QUEUE, true);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(TEST_ROUTING_KEY);

        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);

        // Purge the queue before each test
        rabbitAdmin.purgeQueue(TEST_QUEUE);
    }

    @AfterEach
    void tearDown() {
        rabbitAdmin.purgeQueue(TEST_QUEUE);
    }

    @Test
    void send_shouldDeliverMessageToRabbitMQ() {
        // Given
        WorkflowMessage message = createTestMessage();

        // When
        messageBroker.send(TEST_ROUTING_KEY, message);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Message receivedMessage = rabbitTemplate.receive(TEST_QUEUE, 1000);
            assertThat(receivedMessage).isNotNull();

            WorkflowMessage received = (WorkflowMessage) rabbitTemplate.getMessageConverter()
                    .fromMessage(receivedMessage);

            assertThat(received.getExecutionId()).isEqualTo(message.getExecutionId());
            assertThat(received.getTopic()).isEqualTo(message.getTopic());
            assertThat(received.getCurrentStep()).isEqualTo(message.getCurrentStep());
        });
    }

    @Test
    void sendAsync_shouldDeliverMessageAsynchronously() throws Exception {
        // Given
        WorkflowMessage message = createTestMessage();

        // When
        messageBroker.sendAsync(TEST_ROUTING_KEY, message).get(10, TimeUnit.SECONDS);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Message receivedMessage = rabbitTemplate.receive(TEST_QUEUE, 1000);
            assertThat(receivedMessage).isNotNull();

            WorkflowMessage received = (WorkflowMessage) rabbitTemplate.getMessageConverter()
                    .fromMessage(receivedMessage);

            assertThat(received.getExecutionId()).isEqualTo(message.getExecutionId());
        });
    }

    @Test
    void send_shouldIncludeWorkflowHeadersInMessage() {
        // Given
        WorkflowMessage message = createTestMessage();

        // When
        messageBroker.send(TEST_ROUTING_KEY, message);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Message receivedMessage = rabbitTemplate.receive(TEST_QUEUE, 1000);
            assertThat(receivedMessage).isNotNull();

            MessageProperties properties = receivedMessage.getMessageProperties();
            assertThat(properties.getMessageId()).isEqualTo(message.getExecutionId());
            assertThat(properties.getCorrelationId()).isEqualTo(message.getCorrelationId());
            assertThat((String) properties.getHeader("x-workflow-topic")).isEqualTo(message.getTopic());
            assertThat((Integer) properties.getHeader("x-workflow-step")).isEqualTo(message.getCurrentStep());
            assertThat((String) properties.getHeader("x-workflow-status")).isEqualTo(message.getStatus().name());
        });
    }

    @Test
    void getBrokerType_shouldReturnRabbitmq() {
        assertThat(messageBroker.getBrokerType()).isEqualTo("rabbitmq");
    }

    @Test
    void isAvailable_shouldReturnTrueWhenConnected() {
        assertThat(messageBroker.isAvailable()).isTrue();
    }

    private WorkflowMessage createTestMessage() {
        return WorkflowMessage.builder()
                .executionId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .topic(TEST_ROUTING_KEY)
                .currentStep(1)
                .totalSteps(3)
                .status(WorkflowStatus.IN_PROGRESS)
                .payload(Map.of("key", "value"))
                .build();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public ConnectionFactory connectionFactory(
                @Value("${test.rabbitmq.host}") String host,
                @Value("${test.rabbitmq.port}") int port,
                @Value("${test.rabbitmq.username}") String username,
                @Value("${test.rabbitmq.password}") String password) {
            CachingConnectionFactory factory = new CachingConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            return factory;
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }

        @Bean
        public MessageConverter messageConverter(ObjectMapper objectMapper) {
            return new Jackson2JsonMessageConverter(objectMapper);
        }

        @Bean
        public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                             MessageConverter messageConverter) {
            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            template.setMessageConverter(messageConverter);
            return template;
        }

        @Bean
        public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
            return new RabbitAdmin(connectionFactory);
        }

        @Bean
        public RabbitMQMessageBroker rabbitMQMessageBroker(RabbitTemplate rabbitTemplate,
                                                           MessageConverter messageConverter) {
            return new RabbitMQMessageBroker(rabbitTemplate, TEST_EXCHANGE, messageConverter);
        }
    }
}