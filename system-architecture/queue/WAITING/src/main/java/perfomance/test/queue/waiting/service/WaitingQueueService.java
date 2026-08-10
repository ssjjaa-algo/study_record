package perfomance.test.queue.waiting.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import perfomance.test.queue.waiting.domain.WaitingQueueRegistration;
import perfomance.test.queue.waiting.domain.WaitingQueueProgress;
import perfomance.test.queue.waiting.domain.WaitingQueueState;
import perfomance.test.queue.waiting.domain.WaitingQueueStatus;
import perfomance.test.queue.waiting.repository.QueueCommandResult;
import perfomance.test.queue.waiting.repository.WaitingQueueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class WaitingQueueService {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_-]{1,100}");

    private final WaitingQueueRepository repository;
    private final AdmissionService admissionService;
    private final AdmissionTokenService tokenService;
    private final Map<String, Mono<WaitingQueueProgress>> progressCache = new ConcurrentHashMap<>();

    public WaitingQueueService(
            WaitingQueueRepository repository,
            AdmissionService admissionService,
            AdmissionTokenService tokenService
    ) {
        this.repository = repository;
        this.admissionService = admissionService;
        this.tokenService = tokenService;
    }

    public Mono<WaitingQueueRegistration> register(String eventId, String userId, String authority) {
        validate(eventId, userId, authority);
        return repository.register(eventId, userId)
                .flatMap(this::requireQueueResult)
                .map(result -> new WaitingQueueRegistration(
                        toStatus(eventId, userId, result),
                        result.newlyRegistered()
                ));
    }

    public Mono<WaitingQueueStatus> findStatus(String eventId, String userId, String authority) {
        validate(eventId, userId, authority);
        return repository.findStatus(eventId, userId)
                .flatMap(this::requireQueueResult)
                .map(result -> toStatus(eventId, userId, result));
    }

    public Mono<WaitingQueueProgress> findProgress(String eventId, long sequence) {
        validateIdentifier("eventId", eventId);
        if (sequence <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "sequence must be positive."));
        }
        return progressCache.computeIfAbsent(
                eventId,
                key -> Mono.defer(() -> repository.findProgress(key)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Waiting event not found."
                                ))))
                        .cache(Duration.ofSeconds(3))
        ).map(progress -> {
            long position = Math.max(1, sequence - progress.lastAdmittedSequence());
            return new WaitingQueueProgress(
                    progress.eventId(),
                    progress.lastAdmittedSequence(),
                    progress.activeCount(),
                    nextPollAfterSeconds(WaitingQueueState.WAITING, position)
            );
        });
    }

    public Mono<WaitingQueueStatus> release(
            String eventId,
            String userId,
            long sequence,
            String authority
    ) {
        validate(eventId, userId, authority);
        if (sequence <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "sequence must be positive."));
        }
        return repository.release(eventId, userId, sequence)
                .flatMap(this::requireQueueResult)
                .flatMap(result -> {
                    if (!"COMPLETED".equals(result.code())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Only the matching active admission can be released."
                        ));
                    }
                    return admissionService.admitNow(eventId)
                            .thenReturn(toStatus(eventId, userId, result));
                });
    }

    private Mono<QueueCommandResult> requireQueueResult(QueueCommandResult result) {
        return switch (result.code()) {
            case "EVENT_NOT_FOUND", "NOT_FOUND" -> Mono.error(
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Waiting queue entry not found."));
            case "EVENT_NOT_OPEN" -> Mono.error(
                    new ResponseStatusException(HttpStatus.CONFLICT, "Waiting event is not open."));
            case "QUEUE_FULL" -> Mono.error(
                    new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Waiting queue is full."));
            case "ADMISSION_INVALID" -> Mono.error(
                    new ResponseStatusException(HttpStatus.CONFLICT, "Admission sequence is no longer valid."));
            default -> Mono.just(result);
        };
    }

    private WaitingQueueStatus toStatus(String eventId, String userId, QueueCommandResult result) {
        WaitingQueueState state = WaitingQueueState.valueOf(result.code());
        Long position = result.rank() < 0 ? null : result.rank() + 1;
        Long peopleAhead = result.rank() < 0 ? null : result.rank();
        Instant expiresAt = result.expiresAtMillis() == 0
                ? null
                : Instant.ofEpochMilli(result.expiresAtMillis());
        String admissionToken = state == WaitingQueueState.ACTIVE
                ? tokenService.issue(eventId, userId, result.sequence(), expiresAt)
                : null;
        return new WaitingQueueStatus(
                eventId,
                userId,
                state,
                result.sequence(),
                position,
                peopleAhead,
                result.activeCount(),
                expiresAt,
                nextPollAfterSeconds(state, position),
                admissionToken
        );
    }

    private int nextPollAfterSeconds(WaitingQueueState state, Long position) {
        if (state != WaitingQueueState.WAITING || position == null) {
            return 0;
        }
        if (position >= 1_000) {
            return 5;
        }
        if (position >= 100) {
            return 3;
        }
        return 1;
    }

    private void validate(String eventId, String userId, String authority) {
        if (!"user".equals(authority)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only user authority is allowed.");
        }
        validateIdentifier("eventId", eventId);
        validateIdentifier("userId", userId);
    }

    private void validateIdentifier(String name, String value) {
        if (value == null || !IDENTIFIER.matcher(value).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    name + " must contain only letters, numbers, '-' or '_', up to 100 characters."
            );
        }
    }
}
