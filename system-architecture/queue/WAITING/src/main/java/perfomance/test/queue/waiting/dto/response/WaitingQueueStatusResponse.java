package perfomance.test.queue.waiting.dto.response;

import java.time.Instant;

import perfomance.test.queue.waiting.domain.WaitingQueueState;
import perfomance.test.queue.waiting.domain.WaitingQueueStatus;

public record WaitingQueueStatusResponse(
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
    public static WaitingQueueStatusResponse from(WaitingQueueStatus status) {
        return new WaitingQueueStatusResponse(
                status.eventId(),
                status.userId(),
                status.status(),
                status.sequence(),
                status.position(),
                status.peopleAhead(),
                status.activeCount(),
                status.expiresAt(),
                status.nextPollAfterSeconds(),
                status.admissionToken()
        );
    }
}
