package perfomance.test.queue.waiting.repository;

public record QueueCommandResult(
        String code,
        long sequence,
        long rank,
        long expiresAtMillis,
        long activeCount,
        boolean newlyRegistered
) {
}
