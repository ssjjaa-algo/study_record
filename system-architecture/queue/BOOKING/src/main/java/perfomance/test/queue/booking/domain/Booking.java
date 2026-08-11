package perfomance.test.queue.booking.domain;

import java.time.Instant;

public record Booking(
        String eventId,
        String userId,
        BookingStatus status,
        Instant startedAt,
        Instant completedAt
) {
}
