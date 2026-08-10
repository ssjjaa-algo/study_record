package perfomance.test.queue.booking.domain;

import java.time.Instant;

public record AdmissionTokenClaims(
        String eventId,
        String userId,
        long sequence,
        Instant expiresAt
) {
}
