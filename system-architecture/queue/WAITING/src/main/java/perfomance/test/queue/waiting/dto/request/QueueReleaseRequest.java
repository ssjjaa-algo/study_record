package perfomance.test.queue.waiting.dto.request;

public record QueueReleaseRequest(
        String userId,
        long sequence,
        String authority
) {
}
