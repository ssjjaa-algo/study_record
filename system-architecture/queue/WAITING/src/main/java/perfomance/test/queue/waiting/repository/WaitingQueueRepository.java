package perfomance.test.queue.waiting.repository;

import java.util.List;

import perfomance.test.queue.waiting.domain.WaitingQueueProgress;
import perfomance.test.queue.waiting.repository.script.WaitingQueueRedisScripts;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class WaitingQueueRepository {

    private final ReactiveStringRedisTemplate redisTemplate;

    public WaitingQueueRepository(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<QueueCommandResult> register(String eventId, String userId) {
        return executeResult(
                WaitingQueueRedisScripts.REGISTER,
                List.of(
                        WaitingQueueRedisKeys.meta(eventId),
                        WaitingQueueRedisKeys.waiting(eventId),
                        WaitingQueueRedisKeys.waitingLastSeen(eventId),
                        WaitingQueueRedisKeys.active(eventId)
                ),
                List.of(userId)
        );
    }

    public Mono<QueueCommandResult> findStatus(String eventId, String userId) {
        return executeResult(
                WaitingQueueRedisScripts.STATUS,
                List.of(
                        WaitingQueueRedisKeys.waiting(eventId),
                        WaitingQueueRedisKeys.waitingLastSeen(eventId),
                        WaitingQueueRedisKeys.active(eventId)
                ),
                List.of(userId)
        );
    }

    public Mono<WaitingQueueProgress> findProgress(String eventId) {
        Mono<List<String>> meta = redisTemplate.<String, String>opsForHash().multiGet(
                WaitingQueueRedisKeys.meta(eventId),
                List.of("state", "lastAdmittedSequence")
        );
        Mono<Long> activeCount = redisTemplate.opsForZSet()
                .size(WaitingQueueRedisKeys.active(eventId))
                .defaultIfEmpty(0L);

        return Mono.zip(meta, activeCount)
                .flatMap(tuple -> {
                    List<String> values = tuple.getT1();
                    if (values.isEmpty() || values.getFirst() == null) {
                        return Mono.empty();
                    }
                    String lastAdmittedSequence = values.get(1);
                    return Mono.just(new WaitingQueueProgress(
                            eventId,
                            lastAdmittedSequence == null ? 0L : Long.parseLong(lastAdmittedSequence),
                            tuple.getT2(),
                            0
                    ));
                });
    }

    public Mono<Long> admit(String eventId, int admissionBatchSize) {
        return redisTemplate.execute(
                WaitingQueueRedisScripts.ADMIT,
                List.of(
                        WaitingQueueRedisKeys.meta(eventId),
                        WaitingQueueRedisKeys.waiting(eventId),
                        WaitingQueueRedisKeys.waitingLastSeen(eventId),
                        WaitingQueueRedisKeys.active(eventId)
                ),
                List.of(Integer.toString(admissionBatchSize))
        )
                .next()
                .defaultIfEmpty(0L);
    }

    public Mono<QueueCommandResult> release(String eventId, String userId) {
        return executeResult(
                WaitingQueueRedisScripts.RELEASE,
                List.of(WaitingQueueRedisKeys.active(eventId)),
                List.of(userId)
        );
    }

    public Mono<Long> expireInactive(String eventId, int batchSize) {
        return redisTemplate.execute(
                        WaitingQueueRedisScripts.EXPIRE,
                        List.of(
                                WaitingQueueRedisKeys.meta(eventId),
                                WaitingQueueRedisKeys.waiting(eventId),
                                WaitingQueueRedisKeys.waitingLastSeen(eventId),
                                WaitingQueueRedisKeys.active(eventId)
                        ),
                        List.of(Integer.toString(batchSize))
                )
                .next()
                .defaultIfEmpty(0L);
    }

    private Mono<QueueCommandResult> executeResult(
            DefaultRedisScript<String> script,
            List<String> keys,
            List<String> arguments
    ) {
        return redisTemplate.execute(script, keys, arguments)
                .next()
                .switchIfEmpty(Mono.error(new IllegalStateException("Redis returned no queue result.")))
                .map(this::parseResult);
    }

    private QueueCommandResult parseResult(String raw) {
        String[] values = raw.split("\\|", 4);
        if (values.length != 4) {
            throw new IllegalStateException("Unexpected Redis queue result: " + raw);
        }
        return new QueueCommandResult(
                values[0],
                Long.parseLong(values[1]),
                Long.parseLong(values[2]),
                "1".equals(values[3])
        );
    }
}
