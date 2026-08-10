package perfomance.test.queue.waiting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "waiting-queue")
public record WaitingQueueProperties(
        Defaults defaults,
        Worker worker
) {
    public record Defaults(
            int maxWaitingUsers,
            int maxActiveUsers,
            int admissionRatePerSecond,
            long waitingInactivityTimeoutSeconds,
            long activeInactivityTimeoutSeconds,
            long maxActiveDurationSeconds,
            long dataRetentionSeconds
    ) {
    }

    public record Worker(
            long admissionIntervalMs,
            long admissionLeaseMs,
            int admissionBatchSize,
            long expirationIntervalMs,
            long expirationLeaseMs,
            int expirationBatchSize
    ) {
    }

}
