package perfomance.test.queue.waiting.domain;

public record WaitingQueueRegistration(
        WaitingQueueStatus queue,
        boolean newlyRegistered
) {
}
