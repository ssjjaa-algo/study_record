package perfomance.test.queue.booking.dto.response;

import java.time.Instant;

import perfomance.test.queue.booking.domain.Booking;
import perfomance.test.queue.booking.domain.BookingStatus;

public record BookingResponse(
        String eventId,
        String userId,
        long sequence,
        BookingStatus status,
        Instant startedAt,
        Instant completedAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.eventId(),
                booking.userId(),
                booking.sequence(),
                booking.status(),
                booking.startedAt(),
                booking.completedAt()
        );
    }
}
