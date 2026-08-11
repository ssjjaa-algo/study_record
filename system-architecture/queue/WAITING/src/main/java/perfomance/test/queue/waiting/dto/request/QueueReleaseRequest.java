package perfomance.test.queue.waiting.dto.request;

public record QueueReleaseRequest(
        String userId,
        String authority
) {
}
