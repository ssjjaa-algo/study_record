package perfomance.test.queue.booking.service;

import java.time.Duration;

import perfomance.test.queue.booking.client.WaitingQueueClient;
import perfomance.test.queue.booking.config.BookingProperties;
import perfomance.test.queue.booking.domain.AdmissionTokenClaims;
import perfomance.test.queue.booking.domain.Booking;
import perfomance.test.queue.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository repository;
    private final BookingTokenService tokenService;
    private final WaitingQueueClient waitingQueueClient;
    private final Duration processingDuration;

    public BookingService(
            BookingRepository repository,
            BookingTokenService tokenService,
            WaitingQueueClient waitingQueueClient,
            BookingProperties properties
    ) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.waitingQueueClient = waitingQueueClient;
        this.processingDuration = Duration.ofSeconds(properties.processingDurationSeconds());
    }

    public Booking start(String authority, String admissionToken) {
        validateAuthority(authority);
        AdmissionTokenClaims claims = tokenService.decode(admissionToken);
        Booking existing = repository.find(claims.eventId(), claims.userId(), claims.sequence()).orElse(null);
        if (existing != null) {
            return existing;
        }

        BookingRepository.BookingStart start = repository.start(claims);
        if (!start.newlyStarted()) {
            return start.booking();
        }

        try {
            log.info(
                    "Booking processing started. eventId={}, userId={}, sequence={}, virtualThread={}",
                    claims.eventId(),
                    claims.userId(),
                    claims.sequence(),
                    Thread.currentThread().isVirtual()
            );
            Thread.sleep(processingDuration);
            Booking completed = repository.complete(claims);
            waitingQueueClient.releaseAsync(claims);
            return completed;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            repository.fail(claims);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Booking processing was interrupted.");
        }
    }

    public Booking find(String eventId, String userId, long sequence, String authority) {
        validateAuthority(authority);
        return repository.find(eventId, userId, sequence)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
    }

    private void validateAuthority(String authority) {
        if (!"user".equals(authority)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only user authority is allowed.");
        }
    }
}
