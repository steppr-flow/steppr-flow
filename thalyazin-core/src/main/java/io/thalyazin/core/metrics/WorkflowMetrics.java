package io.thalyazin.core.metrics;

import io.micrometer.core.instrument.*;
import io.thalyazin.core.model.WorkflowStatus;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Micrometer-based metrics for workflow orchestration.
 *
 * <p>Provides the following metrics:
 * <ul>
 *   <li>thalyazin.workflow.started - Counter of started workflows (by topic)</li>
 *   <li>thalyazin.workflow.completed - Counter of completed workflows (by topic)</li>
 *   <li>thalyazin.workflow.failed - Counter of failed workflows (by topic)</li>
 *   <li>thalyazin.workflow.cancelled - Counter of cancelled workflows (by topic)</li>
 *   <li>thalyazin.workflow.active - Gauge of currently active workflows (by topic)</li>
 *   <li>thalyazin.workflow.duration - Timer of workflow execution duration (by topic, status)</li>
 *   <li>thalyazin.step.executed - Counter of executed steps (by topic, step)</li>
 *   <li>thalyazin.step.failed - Counter of failed steps (by topic, step)</li>
 *   <li>thalyazin.step.duration - Timer of step execution duration (by topic, step)</li>
 *   <li>thalyazin.step.timeout - Counter of timed out steps (by topic, step)</li>
 *   <li>thalyazin.retry.count - Counter of retry attempts (by topic)</li>
 *   <li>thalyazin.dlq.count - Counter of messages sent to DLQ (by topic)</li>
 * </ul>
 */
@Slf4j
public class WorkflowMetrics {

    private static final String PREFIX = "thalyazin";
    private static final String TAG_TOPIC = "topic";
    private static final String TAG_SERVICE = "service";
    private static final String TAG_STEP = "step";
    private static final String TAG_STATUS = "status";
    private static final String UNKNOWN_SERVICE = "unknown";

    private final MeterRegistry registry;

    // Gauges for active workflows per topic
    private final Map<String, AtomicLong> activeWorkflowsByTopic = new ConcurrentHashMap<>();

    // Cached counters for performance
    private final Map<String, Counter> startedCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> completedCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> failedCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> cancelledCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> retryCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> dlqCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> stepExecutedCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> stepFailedCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> stepTimeoutCounters = new ConcurrentHashMap<>();

    public WorkflowMetrics(MeterRegistry registry) {
        this.registry = registry;
        log.info("WorkflowMetrics initialized with registry: {}", registry.getClass().getSimpleName());
    }

    // ========== Workflow Lifecycle Metrics ==========

    /**
     * Record a workflow start.
     */
    public void recordWorkflowStarted(String topic) {
        recordWorkflowStarted(topic, null);
    }

    /**
     * Record a workflow start with service name.
     */
    public void recordWorkflowStarted(String topic, String serviceName) {
        String service = serviceName != null ? serviceName : UNKNOWN_SERVICE;
        getOrCreateCounter(startedCounters, PREFIX + ".workflow.started", TAG_TOPIC, topic, TAG_SERVICE, service).increment();
        getOrCreateActiveGauge(topic, service).incrementAndGet();
        log.debug("Recorded workflow started: topic={}, service={}", topic, service);
    }

    /**
     * Record a workflow completion.
     */
    public void recordWorkflowCompleted(String topic, Duration duration) {
        recordWorkflowCompleted(topic, null, duration);
    }

    /**
     * Record a workflow completion with service name.
     */
    public void recordWorkflowCompleted(String topic, String serviceName, Duration duration) {
        String service = serviceName != null ? serviceName : UNKNOWN_SERVICE;
        getOrCreateCounter(completedCounters, PREFIX + ".workflow.completed", TAG_TOPIC, topic, TAG_SERVICE, service).increment();
        getOrCreateActiveGauge(topic, service).decrementAndGet();
        recordWorkflowDuration(topic, service, WorkflowStatus.COMPLETED, duration);
        log.debug("Recorded workflow completed: topic={}, service={}, duration={}ms", topic, service, duration.toMillis());
    }

    /**
     * Record a workflow failure.
     */
    public void recordWorkflowFailed(String topic, Duration duration) {
        recordWorkflowFailed(topic, null, duration);
    }

    /**
     * Record a workflow failure with service name.
     */
    public void recordWorkflowFailed(String topic, String serviceName, Duration duration) {
        String service = serviceName != null ? serviceName : UNKNOWN_SERVICE;
        getOrCreateCounter(failedCounters, PREFIX + ".workflow.failed", TAG_TOPIC, topic, TAG_SERVICE, service).increment();
        getOrCreateActiveGauge(topic, service).decrementAndGet();
        recordWorkflowDuration(topic, service, WorkflowStatus.FAILED, duration);
        log.debug("Recorded workflow failed: topic={}, service={}, duration={}ms", topic, service, duration.toMillis());
    }

    /**
     * Record a workflow cancellation.
     */
    public void recordWorkflowCancelled(String topic) {
        recordWorkflowCancelled(topic, null);
    }

    /**
     * Record a workflow cancellation with service name.
     */
    public void recordWorkflowCancelled(String topic, String serviceName) {
        String service = serviceName != null ? serviceName : UNKNOWN_SERVICE;
        getOrCreateCounter(cancelledCounters, PREFIX + ".workflow.cancelled", TAG_TOPIC, topic, TAG_SERVICE, service).increment();
        getOrCreateActiveGauge(topic, service).decrementAndGet();
        log.debug("Recorded workflow cancelled: topic={}, service={}", topic, service);
    }

    // ========== Step Metrics ==========

    /**
     * Record a step execution.
     */
    public void recordStepExecuted(String topic, String stepLabel, Duration duration) {
        String key = topic + ":" + stepLabel;
        getOrCreateCounter(stepExecutedCounters, PREFIX + ".step.executed", TAG_TOPIC, topic, TAG_STEP, stepLabel).increment();
        recordStepDuration(topic, stepLabel, duration);
        log.debug("Recorded step executed: topic={}, step={}, duration={}ms", topic, stepLabel, duration.toMillis());
    }

    /**
     * Record a step failure.
     */
    public void recordStepFailed(String topic, String stepLabel) {
        String key = topic + ":" + stepLabel;
        getOrCreateCounter(stepFailedCounters, PREFIX + ".step.failed", TAG_TOPIC, topic, TAG_STEP, stepLabel).increment();
        log.debug("Recorded step failed: topic={}, step={}", topic, stepLabel);
    }

    /**
     * Record a step timeout.
     */
    public void recordStepTimeout(String topic, String stepLabel) {
        String key = topic + ":" + stepLabel;
        getOrCreateCounter(stepTimeoutCounters, PREFIX + ".step.timeout", TAG_TOPIC, topic, TAG_STEP, stepLabel).increment();
        log.debug("Recorded step timeout: topic={}, step={}", topic, stepLabel);
    }

    // ========== Retry & DLQ Metrics ==========

    /**
     * Record a retry attempt.
     */
    public void recordRetry(String topic, int attempt) {
        getOrCreateCounter(retryCounters, PREFIX + ".retry.count", TAG_TOPIC, topic).increment();
        log.debug("Recorded retry: topic={}, attempt={}", topic, attempt);
    }

    /**
     * Record a message sent to DLQ.
     */
    public void recordDlq(String topic) {
        getOrCreateCounter(dlqCounters, PREFIX + ".dlq.count", TAG_TOPIC, topic).increment();
        log.debug("Recorded DLQ: topic={}", topic);
    }

    // ========== Duration Recording ==========

    private void recordWorkflowDuration(String topic, String serviceName, WorkflowStatus status, Duration duration) {
        Timer.builder(PREFIX + ".workflow.duration")
                .tag(TAG_TOPIC, topic)
                .tag(TAG_SERVICE, serviceName)
                .tag(TAG_STATUS, status.name())
                .description("Workflow execution duration")
                .register(registry)
                .record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void recordStepDuration(String topic, String stepLabel, Duration duration) {
        Timer.builder(PREFIX + ".step.duration")
                .tag(TAG_TOPIC, topic)
                .tag(TAG_STEP, stepLabel)
                .description("Step execution duration")
                .register(registry)
                .record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    // ========== Helper Methods ==========

    private Counter getOrCreateCounter(Map<String, Counter> cache, String name, String... tags) {
        String key = name + ":" + String.join(":", tags);
        return cache.computeIfAbsent(key, k ->
                Counter.builder(name)
                        .tags(tags)
                        .register(registry)
        );
    }

    private AtomicLong getOrCreateActiveGauge(String topic, String serviceName) {
        String key = topic + ":" + serviceName;
        return activeWorkflowsByTopic.computeIfAbsent(key, k -> {
            AtomicLong gauge = new AtomicLong(0);
            Gauge.builder(PREFIX + ".workflow.active", gauge, AtomicLong::get)
                    .tag(TAG_TOPIC, topic)
                    .tag(TAG_SERVICE, serviceName)
                    .description("Number of active workflows")
                    .register(registry);
            return gauge;
        });
    }

    // ========== Metrics Summary (for API) ==========

    /**
     * Record for workflow identification by topic and service.
     */
    public record WorkflowKey(String topic, String serviceName) {}

    /**
     * Get a summary of all metrics for a specific topic (backward compatibility).
     */
    public MetricsSummary getSummary(String topic) {
        return getSummary(topic, UNKNOWN_SERVICE);
    }

    /**
     * Get a summary of all metrics for a specific topic and serviceName.
     */
    public MetricsSummary getSummary(String topic, String serviceName) {
        String key = topic + ":" + serviceName;
        return MetricsSummary.builder()
                .topic(topic)
                .serviceName(serviceName)
                .workflowsStarted(getCounterValue(startedCounters, PREFIX + ".workflow.started", TAG_TOPIC, topic, TAG_SERVICE, serviceName))
                .workflowsCompleted(getCounterValue(completedCounters, PREFIX + ".workflow.completed", TAG_TOPIC, topic, TAG_SERVICE, serviceName))
                .workflowsFailed(getCounterValue(failedCounters, PREFIX + ".workflow.failed", TAG_TOPIC, topic, TAG_SERVICE, serviceName))
                .workflowsCancelled(getCounterValue(cancelledCounters, PREFIX + ".workflow.cancelled", TAG_TOPIC, topic, TAG_SERVICE, serviceName))
                .workflowsActive(activeWorkflowsByTopic.getOrDefault(key, new AtomicLong(0)).get())
                .retryCount(getCounterValue(retryCounters, PREFIX + ".retry.count", TAG_TOPIC, topic))
                .dlqCount(getCounterValue(dlqCounters, PREFIX + ".dlq.count", TAG_TOPIC, topic))
                .avgWorkflowDurationMs(getTimerMeanWithService(topic, serviceName))
                .build();
    }

    /**
     * Get all active workflow keys (topic:serviceName pairs) that have recorded metrics.
     */
    public Set<WorkflowKey> getActiveWorkflowKeys() {
        Set<WorkflowKey> keys = new HashSet<>();
        String prefix = PREFIX + ".workflow.started:";
        for (String key : startedCounters.keySet()) {
            if (key.startsWith(prefix)) {
                // Key format: thalyazin.workflow.started:topic:{topic}:service:{service}
                String remainder = key.substring(prefix.length());
                String[] parts = remainder.split(":");
                if (parts.length >= 4 && "topic".equals(parts[0]) && "service".equals(parts[2])) {
                    keys.add(new WorkflowKey(parts[1], parts[3]));
                }
            }
        }
        return keys;
    }

    /**
     * Get a global summary across all topics.
     */
    public MetricsSummary getGlobalSummary() {
        long started = sumCounterValues(startedCounters);
        long completed = sumCounterValues(completedCounters);
        long failed = sumCounterValues(failedCounters);
        long cancelled = sumCounterValues(cancelledCounters);
        long active = activeWorkflowsByTopic.values().stream().mapToLong(AtomicLong::get).sum();
        long retries = sumCounterValues(retryCounters);
        long dlq = sumCounterValues(dlqCounters);

        return MetricsSummary.builder()
                .topic("_global")
                .workflowsStarted(started)
                .workflowsCompleted(completed)
                .workflowsFailed(failed)
                .workflowsCancelled(cancelled)
                .workflowsActive(active)
                .retryCount(retries)
                .dlqCount(dlq)
                .successRate(started > 0 ? (double) completed / started * 100 : 0)
                .build();
    }

    private long getCounterValue(Map<String, Counter> cache, String name, String... tags) {
        String key = name + ":" + String.join(":", tags);
        Counter counter = cache.get(key);
        return counter != null ? (long) counter.count() : 0L;
    }

    private long sumCounterValues(Map<String, Counter> cache) {
        return cache.values().stream()
                .mapToLong(c -> (long) c.count())
                .sum();
    }

    private double getTimerMean(String name, String... tags) {
        Timer timer = registry.find(name).tags(tags).timer();
        return timer != null ? timer.mean(TimeUnit.MILLISECONDS) : 0.0;
    }

    private double getTimerMeanWithService(String topic, String serviceName) {
        Timer timer = registry.find(PREFIX + ".workflow.duration")
                .tag(TAG_TOPIC, topic)
                .tag(TAG_SERVICE, serviceName)
                .timer();
        return timer != null ? timer.mean(TimeUnit.MILLISECONDS) : 0.0;
    }

    /**
     * Get all topics that have recorded metrics.
     * Extracts topic names from the startedCounters cache keys.
     */
    public Set<String> getActiveTopics() {
        Set<String> topics = new HashSet<>();
        String prefix = PREFIX + ".workflow.started:topic:";
        for (String key : startedCounters.keySet()) {
            if (key.startsWith(prefix)) {
                topics.add(key.substring(prefix.length()));
            }
        }
        return topics;
    }
}