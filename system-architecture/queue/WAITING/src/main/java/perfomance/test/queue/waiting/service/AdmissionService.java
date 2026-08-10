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
public class AdmissionService {

    private static final Logger log = LoggerFactory.getLogger(AdmissionService.class);

    private final WaitingQueueRegistryRepository registryRepository;
    private final WaitingQueueRepository queueRepository;
    private final WaitingQueueWorkerLeaseRepository leaseRepository;
    private final WaitingQueueProperties properties;
    private Disposable worker;

    public AdmissionService(
            WaitingQueueRegistryRepository registryRepository,
            WaitingQueueRepository queueRepository,
            WaitingQueueWorkerLeaseRepository leaseRepository,
            WaitingQueueProperties properties
    ) {
        this.registryRepository = registryRepository;
        this.queueRepository = queueRepository;
        this.leaseRepository = leaseRepository;
        this.properties = properties;
    }

    @PostConstruct
    void start() {
        worker = Flux.interval(Duration.ZERO, Duration.ofMillis(properties.worker().admissionIntervalMs()))
                .onBackpressureDrop()
                .concatMap(ignored -> registryRepository.findOpenEventIds()
                        .flatMap(eventId -> admitNow(eventId)
                                .onErrorResume(exception -> {
                                    log.error("Admission failed. eventId={}", eventId, exception);
                                    return Mono.empty();
                                }))
                        .then()
                        .onErrorResume(exception -> {
                            log.error("Failed to load open waiting events.", exception);
                            return Mono.empty();
                        }))
                .subscribe();
    }

    public Mono<Long> admitNow(String eventId) {
        return leaseRepository.tryAcquire(eventId, "admission", properties.worker().admissionLeaseMs())
                .flatMap(acquired -> acquired
                        ? queueRepository.admit(
                                eventId,
                                properties.worker().expirationBatchSize(),
                                properties.worker().admissionBatchSize()
                        )
                        : Mono.just(0L));
    }

    @PreDestroy
    void stop() {
        if (worker != null) {
            worker.dispose();
        }
    }
}
