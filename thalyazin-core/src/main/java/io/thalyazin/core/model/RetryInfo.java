package io.thalyazin.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Retry information for workflow steps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryInfo {

    /**
     * Current attempt number (starts at 1).
     */
    @Builder.Default
    private int attempt = 1;

    /**
     * Maximum number of attempts.
     */
    private int maxAttempts;

    /**
     * Next retry scheduled time.
     */
    private Instant nextRetryAt;

    /**
     * Last error message.
     */
    private String lastError;

    /**
     * Create next retry info.
     */
    public RetryInfo nextAttempt(Instant nextRetryAt, String lastError) {
        return RetryInfo.builder()
                .attempt(this.attempt + 1)
                .maxAttempts(this.maxAttempts)
                .nextRetryAt(nextRetryAt)
                .lastError(lastError)
                .build();
    }

    /**
     * Check if retry is exhausted.
     */
    public boolean isExhausted() {
        return attempt >= maxAttempts;
    }
}