package perfomance.test.queue.booking.domain;

import java.time.Instant;

public record Booking(
        String eventId,
        String userId,
        long sequence,
        BookingStatus status,
        Instant startedAt,
        Instant completedAt
) {
}
