package perfomance.test.queue.booking.controller;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = {"http://localhost:8081", "http://127.0.0.1:8081"})
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private static final Duration PROCESSING_TIME = Duration.ofSeconds(5);

    private final RestClient waitingClient;

    public BookingController(@Value("${waiting.base-url:http://localhost:8081}") String waitingBaseUrl) {
        this.waitingClient = RestClient.create(waitingBaseUrl);
    }

    @PostMapping
    public Map<String, String> book(
            @RequestParam String authority,
            @RequestParam String admissionToken
    ) {
        if (!"user".equals(authority)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String[] admission = decode(admissionToken);
        try {
            Thread.sleep(PROCESSING_TIME);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        releaseAsync(admission[0], admission[1]);
        return Map.of("status", "COMPLETED");
    }

    private String[] decode(String admissionToken) {
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(admissionToken),
                    StandardCharsets.UTF_8
            );
            String[] admission = decoded.split("\\|", 2);
            if (admission.length != 2 || admission[0].isBlank() || admission[1].isBlank()) {
                throw new IllegalArgumentException();
            }
            return admission;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void releaseAsync(String eventId, String userId) {
        Thread.startVirtualThread(() -> {
            try {
                waitingClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/v1/waiting-events/{eventId}/queue/release")
                                .queryParam("userId", userId)
                                .queryParam("authority", "user")
                                .build(eventId))
                        .retrieve()
                        .toBodilessEntity();
            } catch (RuntimeException exception) {
                log.error("Active slot release failed. eventId={}, userId={}", eventId, userId, exception);
            }
        });
    }
}
