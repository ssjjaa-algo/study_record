package perfomance.test.queue.waiting.repository;

public record QueueCommandResult(
        String code,
        long sequence,
        long rank,
        boolean newlyRegistered
) {
}
