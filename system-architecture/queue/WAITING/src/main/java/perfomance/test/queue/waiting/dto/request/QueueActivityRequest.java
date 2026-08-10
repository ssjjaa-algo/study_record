package perfomance.test.queue.waiting.dto.request;

public record QueueActivityRequest(
        String userId,
        long sequence,
        String authority
) {
}
