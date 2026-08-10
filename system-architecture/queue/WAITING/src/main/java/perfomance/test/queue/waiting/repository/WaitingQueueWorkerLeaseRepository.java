package perfomance.test.queue.waiting.repository;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class WaitingQueueWorkerLeaseRepository {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final String instanceId = UUID.randomUUID().toString();

    public WaitingQueueWorkerLeaseRepository(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> tryAcquire(String eventId, String workerName, long leaseMillis) {
        return redisTemplate.opsForValue()
                .setIfAbsent(
                        WaitingQueueRedisKeys.workerLease(eventId, workerName),
                        instanceId,
                        Duration.ofMillis(leaseMillis)
                )
                .map(Boolean.TRUE::equals)
                .defaultIfEmpty(false);
    }
}
