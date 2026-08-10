package perfomance.test.queue.booking.client;

import perfomance.test.queue.booking.domain.AdmissionTokenClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WaitingQueueClient {

    private static final Logger log = LoggerFactory.getLogger(WaitingQueueClient.class);

    private final RestClient restClient;

    public WaitingQueueClient(@Value("${waiting.base-url:http://localhost:8081}") String waitingBaseUrl) {
        this.restClient = RestClient.create(waitingBaseUrl);
    }

    @Async("releaseExecutor")
    public void releaseAsync(AdmissionTokenClaims claims) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/waiting-events/{eventId}/queue/release")
                            .queryParam("userId", claims.userId())
                            .queryParam("sequence", claims.sequence())
                            .queryParam("authority", "user")
                            .build(claims.eventId()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.error(
                    "Asynchronous active slot release failed. eventId={}, userId={}, sequence={}",
                    claims.eventId(),
                    claims.userId(),
                    claims.sequence(),
                    exception
            );
        }
    }
}
