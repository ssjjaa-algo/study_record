package perfomance.test.queue.waiting.service;

import java.time.Duration;

import perfomance.test.queue.waiting.config.WaitingQueueProperties;
import perfomance.test.queue.waiting.repository.WaitingQueueRepository;
import perfomance.test.queue.waiting.repository.WaitingQueueRegistryRepository;
import perfomance.test.queue.waiting.repository.WaitingQueueWorkerLeaseRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ExpirationService {

    private static final Logger log = LoggerFactory.getLogger(ExpirationService.class);

    private final WaitingQueueRegistryRepository registryRepository;
    private final WaitingQueueRepository queueRepository;
    private final WaitingQueueWorkerLeaseRepository leaseRepository;
    private final AdmissionService admissionService;
    private final WaitingQueueProperties properties;
    private Disposable worker;

    public ExpirationService(
            WaitingQueueRegistryRepository registryRepository,
            WaitingQueueRepository queueRepository,
            WaitingQueueWorkerLeaseRepository leaseRepository,
            AdmissionService admissionService,
            WaitingQueueProperties properties
    ) {
        this.registryRepository = registryRepository;
        this.queueRepository = queueRepository;
        this.leaseRepository = leaseRepository;
        this.admissionService = admissionService;
        this.properties = properties;
    }

    @PostConstruct
    void start() {
        worker = Flux.interval(
                        Duration.ofMillis(properties.worker().expirationIntervalMs()),
                        Duration.ofMillis(properties.worker().expirationIntervalMs())
                )
                .concatMap(ignored -> registryRepository.findOpenEventIds()
                        .flatMap(this::expireEvent)
                        .then()
                        .onErrorResume(exception -> {
                            log.error("Active expiration worker failed.", exception);
                            return Mono.empty();
                        }))
                .subscribe();
    }

    private Mono<Void> expireEvent(String eventId) {
        return leaseRepository.tryAcquire(eventId, "expiration", properties.worker().expirationLeaseMs())
                .filter(Boolean::booleanValue)
                .flatMap(ignored -> queueRepository.expireActive(eventId, properties.worker().expirationBatchSize()))
                .flatMap(expiredCount -> {
                    if (expiredCount == 0) {
                        return Mono.empty();
                    }
                    return admissionService.admitNow(eventId).then();
                })
                .onErrorResume(exception -> {
                    log.error("Active expiration failed. eventId={}", eventId, exception);
                    return Mono.empty();
                });
    }

    @PreDestroy
    void stop() {
        if (worker != null) {
            worker.dispose();
        }
    }
}
