package perfomance.test.queue.waiting.createwaitingqueue.domain;

public record WaitingQueuePolicy(
        int maxWaitingUsers,
        int maxActiveUsers,
        int admissionRatePerSecond,
        long waitingInactivityTimeoutSeconds,
        long activeInactivityTimeoutSeconds,
        long maxActiveDurationSeconds,
        long dataRetentionSeconds
) {
}
