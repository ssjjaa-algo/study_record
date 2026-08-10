package perfomance.test.queue.waiting.domain;

import java.time.Instant;

public record WaitingQueueStatus(
        String eventId,
        String userId,
        WaitingQueueState status,
        long sequence,
        Long position,
        Long peopleAhead,
        long activeCount,
        Instant expiresAt,
        int nextPollAfterSeconds,
        String admissionToken
) {
}
