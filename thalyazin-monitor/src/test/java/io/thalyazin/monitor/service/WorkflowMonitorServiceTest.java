package io.thalyazin.monitor.service;

import io.thalyazin.core.model.WorkflowStatus;
import io.thalyazin.monitor.model.WorkflowExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for WorkflowMonitorService facade.
 * This service delegates to specialized services, so we mock those.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowMonitorService Tests")
class WorkflowMonitorServiceTest {

    @Mock
    private WorkflowQueryService queryService;

    @Mock
    private WorkflowCommandService commandService;

    @Mock
    private PayloadManagementService payloadService;

    @InjectMocks
    private WorkflowMonitorService monitorService;

    private WorkflowExecution testExecution;

    @BeforeEach
    void setUp() {
        testExecution = WorkflowExecution.builder()
                .executionId("exec-123")
                .correlationId("corr-456")
                .topic("test-topic")
                .status(WorkflowStatus.FAILED)
                .currentStep(2)
                .totalSteps(5)
                .payload(Map.of("data", "test"))
                .payloadType("java.util.Map")
                .createdAt(Instant.now().minusSeconds(3600))
                .build();
    }

    @Nested
    @DisplayName("getExecution() method")
    class GetExecutionTests {

        @Test
        @DisplayName("Should delegate to queryService and return execution when found")
        void shouldReturnExecutionWhenFound() {
            when(queryService.getExecution("exec-123")).thenReturn(Optional.of(testExecution));

            Optional<WorkflowExecution> result = monitorService.getExecution("exec-123");

            assertThat(result).isPresent();
            assertThat(result.get().getExecutionId()).isEqualTo("exec-123");
            verify(queryService).getExecution("exec-123");
        }

        @Test
        @DisplayName("Should delegate to queryService and return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(queryService.getExecution("unknown")).thenReturn(Optional.empty());

            Optional<WorkflowExecution> result = monitorService.getExecution("unknown");

            assertThat(result).isEmpty();
            verify(queryService).getExecution("unknown");
        }
    }

    @Nested
    @DisplayName("findExecutions() method")
    class FindExecutionsTests {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 20);
        }

        @Test
        @DisplayName("Should delegate to queryService with topic and status")
        void shouldDelegateWithTopicAndStatus() {
            Page<WorkflowExecution> expectedPage = new PageImpl<>(List.of(testExecution));
            List<WorkflowStatus> statuses = List.of(WorkflowStatus.FAILED);
            when(queryService.findExecutions("test-topic", statuses, pageable)).thenReturn(expectedPage);

            Page<WorkflowExecution> result = monitorService.findExecutions("test-topic", statuses, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(queryService).findExecutions("test-topic", statuses, pageable);
        }

        @Test
        @DisplayName("Should delegate to queryService with topic only")
        void shouldDelegateWithTopicOnly() {
            Page<WorkflowExecution> expectedPage = new PageImpl<>(List.of(testExecution));
            when(queryService.findExecutions("test-topic", null, pageable)).thenReturn(expectedPage);

            Page<WorkflowExecution> result = monitorService.findExecutions("test-topic", null, pageable);

            verify(queryService).findExecutions("test-topic", null, pageable);
        }

        @Test
        @DisplayName("Should delegate to queryService with status only")
        void shouldDelegateWithStatusOnly() {
            Page<WorkflowExecution> expectedPage = new PageImpl<>(List.of(testExecution));
            List<WorkflowStatus> statuses = List.of(WorkflowStatus.FAILED);
            when(queryService.findExecutions(null, statuses, pageable)).thenReturn(expectedPage);

            Page<WorkflowExecution> result = monitorService.findExecutions(null, statuses, pageable);

            verify(queryService).findExecutions(null, statuses, pageable);
        }

        @Test
        @DisplayName("Should delegate to queryService with no filter")
        void shouldDelegateWithNoFilter() {
            Page<WorkflowExecution> expectedPage = new PageImpl<>(List.of(testExecution));
            when(queryService.findExecutions(null, null, pageable)).thenReturn(expectedPage);

            Page<WorkflowExecution> result = monitorService.findExecutions(null, null, pageable);

            verify(queryService).findExecutions(null, null, pageable);
        }
    }

    @Nested
    @DisplayName("resume() method")
    class ResumeTests {

        @Test
        @DisplayName("Should delegate to commandService with default user")
        void shouldDelegateToCommandService() {
            monitorService.resume("exec-123", 2);

            verify(commandService).resume("exec-123", 2, "UI User");
        }

        @Test
        @DisplayName("Should delegate to commandService with null step")
        void shouldDelegateWithNullStep() {
            monitorService.resume("exec-123", null);

            verify(commandService).resume("exec-123", null, "UI User");
        }
    }

    @Nested
    @DisplayName("cancel() method")
    class CancelTests {

        @Test
        @DisplayName("Should delegate to commandService")
        void shouldDelegateToCommandService() {
            monitorService.cancel("exec-123");

            verify(commandService).cancel("exec-123");
        }
    }

    @Nested
    @DisplayName("getStatistics() method")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should delegate to queryService")
        void shouldDelegateToQueryService() {
            Map<String, Object> expectedStats = Map.of(
                    "pending", 5L,
                    "inProgress", 3L,
                    "completed", 100L,
                    "failed", 2L,
                    "total", 110L
            );
            when(queryService.getStatistics()).thenReturn(expectedStats);

            Map<String, Object> result = monitorService.getStatistics();

            assertThat(result).isEqualTo(expectedStats);
            verify(queryService).getStatistics();
        }
    }

    @Nested
    @DisplayName("getRecentExecutions() method")
    class GetRecentExecutionsTests {

        @Test
        @DisplayName("Should delegate to queryService")
        void shouldDelegateToQueryService() {
            List<WorkflowExecution> recentExecutions = List.of(testExecution);
            when(queryService.getRecentExecutions()).thenReturn(recentExecutions);

            List<WorkflowExecution> result = monitorService.getRecentExecutions();

            assertThat(result).hasSize(1);
            verify(queryService).getRecentExecutions();
        }

        @Test
        @DisplayName("Should return empty list when queryService returns empty")
        void shouldReturnEmptyList() {
            when(queryService.getRecentExecutions()).thenReturn(List.of());

            List<WorkflowExecution> result = monitorService.getRecentExecutions();

            assertThat(result).isEmpty();
            verify(queryService).getRecentExecutions();
        }
    }

    @Nested
    @DisplayName("updatePayloadField() method")
    class UpdatePayloadFieldTests {

        @Test
        @DisplayName("Should delegate to payloadService")
        void shouldDelegateToPayloadService() {
            when(payloadService.updatePayloadField("exec-123", "field.path", "newValue", "user", "reason"))
                    .thenReturn(testExecution);

            WorkflowExecution result = monitorService.updatePayloadField("exec-123", "field.path", "newValue", "user", "reason");

            assertThat(result).isEqualTo(testExecution);
            verify(payloadService).updatePayloadField("exec-123", "field.path", "newValue", "user", "reason");
        }
    }

    @Nested
    @DisplayName("restorePayload() method")
    class RestorePayloadTests {

        @Test
        @DisplayName("Should delegate to payloadService")
        void shouldDelegateToPayloadService() {
            when(payloadService.restorePayload("exec-123")).thenReturn(testExecution);

            WorkflowExecution result = monitorService.restorePayload("exec-123");

            assertThat(result).isEqualTo(testExecution);
            verify(payloadService).restorePayload("exec-123");
        }
    }
}
