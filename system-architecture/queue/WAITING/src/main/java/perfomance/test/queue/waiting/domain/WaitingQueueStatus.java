package perfomance.test.queue.waiting.domain;

public record WaitingQueueStatus(
        WaitingQueueState status,
        long sequence,
        Long position,
        int nextPollAfterSeconds,
        String admissionToken
) {
}
