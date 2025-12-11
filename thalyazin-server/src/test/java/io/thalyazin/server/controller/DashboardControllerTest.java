package io.thalyazin.server.controller;

import io.thalyazin.core.model.StepDefinition;
import io.thalyazin.core.model.WorkflowDefinition;
import io.thalyazin.core.model.WorkflowStatus;
import io.thalyazin.core.service.WorkflowRegistry;
import io.thalyazin.monitor.model.WorkflowExecution;
import io.thalyazin.monitor.service.WorkflowMonitorService;
import io.thalyazin.server.config.UiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Mock
    private UiProperties properties;

    @Mock
    private WorkflowMonitorService monitorService;

    @Mock
    private WorkflowRegistry workflowRegistry;

    @InjectMocks
    private DashboardController controller;

    @Nested
    @DisplayName("GET /config")
    class GetConfigTests {

        @BeforeEach
        void setUpProperties() {
            when(properties.getTitle()).thenReturn("Test Dashboard");
            when(properties.getRefreshInterval()).thenReturn(10);
            when(properties.isDarkMode()).thenReturn(true);
            when(properties.getBasePath()).thenReturn("/test-dashboard");
        }

        @Test
        @DisplayName("Should return 200 with config")
        void shouldReturn200WithConfig() {
            ResponseEntity<Map<String, Object>> response = controller.getConfig();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Should return title from properties")
        void shouldReturnTitleFromProperties() {
            ResponseEntity<Map<String, Object>> response = controller.getConfig();

            assertThat(response.getBody()).containsEntry("title", "Test Dashboard");
        }

        @Test
        @DisplayName("Should return refreshInterval from properties")
        void shouldReturnRefreshIntervalFromProperties() {
            ResponseEntity<Map<String, Object>> response = controller.getConfig();

            assertThat(response.getBody()).containsEntry("refreshInterval", 10);
        }

        @Test
        @DisplayName("Should return darkMode from properties")
        void shouldReturnDarkModeFromProperties() {
            ResponseEntity<Map<String, Object>> response = controller.getConfig();

            assertThat(response.getBody()).containsEntry("darkMode", true);
        }

        @Test
        @DisplayName("Should return basePath from properties")
        void shouldReturnBasePathFromProperties() {
            ResponseEntity<Map<String, Object>> response = controller.getConfig();

            assertThat(response.getBody()).containsEntry("basePath", "/test-dashboard");
        }
    }

    @Nested
    @DisplayName("GET /overview")
    class GetOverviewTests {

        @BeforeEach
        void setUpMocks() {
            Map<String, Object> stats = Map.of(
                    "pending", 5L,
                    "inProgress", 3L,
                    "completed", 100L,
                    "failed", 2L,
                    "total", 110L
            );
            when(monitorService.getStatistics()).thenReturn(stats);
        }

        @Test
        @DisplayName("Should return 200 with overview data")
        void shouldReturn200WithOverviewData() {
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of());
            when(monitorService.getRecentExecutions()).thenReturn(List.of());

            ResponseEntity<Map<String, Object>> response = controller.getOverview();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Should include statistics")
        void shouldIncludeStatistics() {
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of());
            when(monitorService.getRecentExecutions()).thenReturn(List.of());

            ResponseEntity<Map<String, Object>> response = controller.getOverview();

            assertThat(response.getBody()).containsKey("stats");
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) response.getBody().get("stats");
            assertThat(stats).containsEntry("total", 110L);
        }

        @Test
        @DisplayName("Should include workflow definitions")
        void shouldIncludeWorkflowDefinitions() {
            WorkflowDefinition definition = WorkflowDefinition.builder()
                    .topic("order-workflow")
                    .description("Order processing workflow")
                    .steps(List.of(
                            StepDefinition.builder().id(1).label("Validate").build(),
                            StepDefinition.builder().id(2).label("Process").build()
                    ))
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(definition));
            when(monitorService.getRecentExecutions()).thenReturn(List.of());

            ResponseEntity<Map<String, Object>> response = controller.getOverview();

            assertThat(response.getBody()).containsKey("workflows");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody().get("workflows");
            assertThat(workflows).hasSize(1);
            assertThat(workflows.get(0)).containsEntry("topic", "order-workflow");
            assertThat(workflows.get(0)).containsEntry("description", "Order processing workflow");
            assertThat(workflows.get(0)).containsEntry("steps", 2);
        }

        @Test
        @DisplayName("Should handle null description in workflow")
        void shouldHandleNullDescriptionInWorkflow() {
            WorkflowDefinition definition = WorkflowDefinition.builder()
                    .topic("simple-workflow")
                    .description(null)
                    .steps(List.of())
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(definition));
            when(monitorService.getRecentExecutions()).thenReturn(List.of());

            ResponseEntity<Map<String, Object>> response = controller.getOverview();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody().get("workflows");
            assertThat(workflows.get(0)).containsEntry("description", "");
        }

        @Test
        @DisplayName("Should include recent executions")
        void shouldIncludeRecentExecutions() {
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of());

            List<WorkflowExecution> recentExecutions = List.of(
                    WorkflowExecution.builder()
                            .executionId("exec-123")
                            .topic("test-topic")
                            .status(WorkflowStatus.COMPLETED)
                            .createdAt(Instant.now())
                            .build()
            );
            when(monitorService.getRecentExecutions()).thenReturn(recentExecutions);

            ResponseEntity<Map<String, Object>> response = controller.getOverview();

            assertThat(response.getBody()).containsKey("recentExecutions");
            @SuppressWarnings("unchecked")
            List<WorkflowExecution> executions = (List<WorkflowExecution>) response.getBody().get("recentExecutions");
            assertThat(executions).hasSize(1);
            assertThat(executions.get(0).getExecutionId()).isEqualTo("exec-123");
        }
    }

    @Nested
    @DisplayName("GET /workflows")
    class GetWorkflowsTests {

        @Test
        @DisplayName("Should return 200 with workflow definitions")
        void shouldReturn200WithWorkflowDefinitions() {
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of());

            ResponseEntity<?> response = controller.getWorkflows(null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return empty list when no workflows")
        void shouldReturnEmptyListWhenNoWorkflows() {
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of());

            ResponseEntity<?> response = controller.getWorkflows(null, null, null);

            @SuppressWarnings("unchecked")
            List<Object> workflows = (List<Object>) response.getBody();
            assertThat(workflows).isEmpty();
        }

        @Test
        @DisplayName("Should return workflow details with steps")
        void shouldReturnWorkflowDetailsWithSteps() {
            StepDefinition step1 = StepDefinition.builder()
                    .id(1)
                    .label("Validate")
                    .description("Validate input")
                    .skippable(false)
                    .continueOnFailure(false)
                    .build();
            StepDefinition step2 = StepDefinition.builder()
                    .id(2)
                    .label("Process")
                    .description("Process data")
                    .skippable(true)
                    .continueOnFailure(true)
                    .build();

            WorkflowDefinition definition = WorkflowDefinition.builder()
                    .topic("order-workflow")
                    .description("Order processing")
                    .steps(List.of(step1, step2))
                    .partitions(3)
                    .replication((short) 2)
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(definition));

            ResponseEntity<?> response = controller.getWorkflows(null, null, null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody();
            assertThat(workflows).hasSize(1);

            Map<String, Object> workflow = workflows.get(0);
            assertThat(workflow).containsEntry("topic", "order-workflow");
            assertThat(workflow).containsEntry("description", "Order processing");
            assertThat(workflow).containsEntry("partitions", 3);
            assertThat(workflow).containsEntry("replication", (short) 2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> steps = (List<Map<String, Object>>) workflow.get("steps");
            assertThat(steps).hasSize(2);

            Map<String, Object> firstStep = steps.get(0);
            assertThat(firstStep).containsEntry("id", 1);
            assertThat(firstStep).containsEntry("label", "Validate");
            assertThat(firstStep).containsEntry("description", "Validate input");
            assertThat(firstStep).containsEntry("skippable", false);
            assertThat(firstStep).containsEntry("continueOnFailure", false);
        }

        @Test
        @DisplayName("Should handle null description in steps")
        void shouldHandleNullDescriptionInSteps() {
            StepDefinition step = StepDefinition.builder()
                    .id(1)
                    .label("Step")
                    .description(null)
                    .build();

            WorkflowDefinition definition = WorkflowDefinition.builder()
                    .topic("test-workflow")
                    .description("Test")
                    .steps(List.of(step))
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(definition));

            ResponseEntity<?> response = controller.getWorkflows(null, null, null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> steps = (List<Map<String, Object>>) workflows.get(0).get("steps");
            assertThat(steps.get(0)).containsEntry("description", "");
        }

        @Test
        @DisplayName("Should return multiple workflows")
        void shouldReturnMultipleWorkflows() {
            WorkflowDefinition def1 = WorkflowDefinition.builder()
                    .topic("workflow-1")
                    .description("First workflow")
                    .steps(List.of())
                    .build();
            WorkflowDefinition def2 = WorkflowDefinition.builder()
                    .topic("workflow-2")
                    .description("Second workflow")
                    .steps(List.of())
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(def1, def2));

            ResponseEntity<?> response = controller.getWorkflows(null, null, null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody();
            assertThat(workflows).hasSize(2);
        }

        @Test
        @DisplayName("Should filter workflows by topic")
        void shouldFilterWorkflowsByTopic() {
            WorkflowDefinition def1 = WorkflowDefinition.builder()
                    .topic("order-workflow")
                    .description("Order processing")
                    .steps(List.of())
                    .build();
            WorkflowDefinition def2 = WorkflowDefinition.builder()
                    .topic("payment-workflow")
                    .description("Payment processing")
                    .steps(List.of())
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(def1, def2));

            ResponseEntity<?> response = controller.getWorkflows("order", null, null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody();
            assertThat(workflows).hasSize(1);
            assertThat(workflows.get(0)).containsEntry("topic", "order-workflow");
        }

        @Test
        @DisplayName("Should filter workflows by status")
        void shouldFilterWorkflowsByStatus() {
            WorkflowDefinition def1 = WorkflowDefinition.builder()
                    .topic("active-workflow")
                    .description("Active workflow")
                    .steps(List.of())
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(def1));

            // Local workflows are always ACTIVE
            ResponseEntity<?> response = controller.getWorkflows(null, null, "ACTIVE");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody();
            assertThat(workflows).hasSize(1);

            // INACTIVE should return empty for local workflows
            ResponseEntity<?> responseInactive = controller.getWorkflows(null, null, "INACTIVE");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> inactiveWorkflows = (List<Map<String, Object>>) responseInactive.getBody();
            assertThat(inactiveWorkflows).isEmpty();
        }

        @Test
        @DisplayName("Should filter workflows by serviceName")
        void shouldFilterWorkflowsByServiceName() {
            WorkflowDefinition def1 = WorkflowDefinition.builder()
                    .topic("local-workflow")
                    .description("Local workflow")
                    .steps(List.of())
                    .build();
            when(workflowRegistry.getAllDefinitions()).thenReturn(List.of(def1));

            ResponseEntity<?> response = controller.getWorkflows(null, "local", null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> workflows = (List<Map<String, Object>>) response.getBody();
            assertThat(workflows).hasSize(1);

            // Non-matching service should return empty
            ResponseEntity<?> responseOther = controller.getWorkflows(null, "other-service", null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> otherWorkflows = (List<Map<String, Object>>) responseOther.getBody();
            assertThat(otherWorkflows).isEmpty();
        }
    }
}
