package perfomance.test.queue.booking.dto.request;

public record BookingRequest(
        String authority,
        String admissionToken
) {
}
