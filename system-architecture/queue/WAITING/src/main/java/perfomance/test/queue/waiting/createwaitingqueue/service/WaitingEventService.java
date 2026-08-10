package perfomance.test.queue.waiting.createwaitingqueue.service;

import java.util.regex.Pattern;

import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingEvent;
import perfomance.test.queue.waiting.createwaitingqueue.repository.WaitingEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class WaitingEventService {

    private static final Pattern EVENT_ID = Pattern.compile("[A-Za-z0-9_-]{1,100}");

    private final WaitingEventRepository repository;
    private final WaitingQueuePolicyProvider policyProvider;

    public WaitingEventService(
            WaitingEventRepository repository,
            WaitingQueuePolicyProvider policyProvider
    ) {
        this.repository = repository;
        this.policyProvider = policyProvider;
    }

    public Mono<WaitingEvent> create(String eventId) {
        validateEventId(eventId);
        return repository.create(eventId, policyProvider.policyFor(eventId));
    }

    public Mono<WaitingEvent> find(String eventId) {
        validateEventId(eventId);
        return repository.find(eventId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Waiting event not found.")));
    }

    public Mono<WaitingEvent> drain(String eventId) {
        validateEventId(eventId);
        return repository.drain(eventId)
                .flatMap(result -> switch (result) {
                    case "NOT_FOUND" -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Waiting event not found."));
                    default -> find(eventId);
                });
    }

    public Mono<WaitingEvent> close(String eventId) {
        validateEventId(eventId);
        return find(eventId)
                .flatMap(event -> repository.close(eventId, event.policy().dataRetentionSeconds()))
                .flatMap(result -> switch (result) {
                    case "NOT_FOUND" -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Waiting event not found."));
                    case "EVENT_NOT_EMPTY" -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Waiting event still has users."));
                    default -> find(eventId);
                });
    }

    private void validateEventId(String eventId) {
        if (eventId == null || !EVENT_ID.matcher(eventId).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "eventId must contain only letters, numbers, '-' or '_', up to 100 characters."
            );
        }
    }
}
