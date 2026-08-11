package perfomance.test.queue.waiting.repository;

import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class WaitingQueueRegistryRepository {

    private final ReactiveSetOperations<String, String> sets;

    public WaitingQueueRegistryRepository(ReactiveStringRedisTemplate redisTemplate) {
        this.sets = redisTemplate.opsForSet();
    }

    public Flux<String> findOpenEventIds() {
        return sets.members(WaitingQueueRedisKeys.OPEN_EVENTS);
    }
}
