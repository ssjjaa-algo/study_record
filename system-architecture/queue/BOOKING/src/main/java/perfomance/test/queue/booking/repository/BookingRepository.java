package perfomance.test.queue.booking.repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import perfomance.test.queue.booking.domain.AdmissionTokenClaims;
import perfomance.test.queue.booking.domain.Booking;
import perfomance.test.queue.booking.domain.BookingStatus;
import org.springframework.stereotype.Repository;

@Repository
public class BookingRepository {

    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public BookingStart start(AdmissionTokenClaims claims) {
        AtomicBoolean newlyStarted = new AtomicBoolean(false);
        Booking booking = bookings.computeIfAbsent(key(claims.eventId(), claims.userId()), ignored -> {
            newlyStarted.set(true);
            return new Booking(
                    claims.eventId(),
                    claims.userId(),
                    BookingStatus.PROCESSING,
                    Instant.now(),
                    null
            );
        });
        return new BookingStart(booking, newlyStarted.get());
    }

    public Booking complete(AdmissionTokenClaims claims) {
        return updateStatus(claims, BookingStatus.COMPLETED);
    }

    public Booking fail(AdmissionTokenClaims claims) {
        return updateStatus(claims, BookingStatus.FAILED);
    }

    public Optional<Booking> find(String eventId, String userId) {
        return Optional.ofNullable(bookings.get(key(eventId, userId)));
    }

    private Booking updateStatus(AdmissionTokenClaims claims, BookingStatus status) {
        return bookings.compute(key(claims.eventId(), claims.userId()), (ignored, current) -> {
            if (current == null) {
                throw new IllegalStateException("Booking does not exist.");
            }
            return new Booking(
                    current.eventId(),
                    current.userId(),
                    status,
                    current.startedAt(),
                    Instant.now()
            );
        });
    }

    private String key(String eventId, String userId) {
        return eventId + ":" + userId;
    }

    public record BookingStart(Booking booking, boolean newlyStarted) {
    }
}
