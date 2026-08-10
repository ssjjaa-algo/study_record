package perfomance.test.queue.waiting.domain;

public record WaitingQueueProgress(
        String eventId,
        long lastAdmittedSequence,
        long activeCount,
        int nextPollAfterSeconds
) {
}
