package perfomance.test.queue.waiting.createwaitingqueue.repository;

import java.util.List;
import java.util.Map;

import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingEvent;
import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingEventState;
import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingQueuePolicy;
import perfomance.test.queue.waiting.repository.WaitingQueueRedisKeys;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class WaitingEventRepository {

    private static final DefaultRedisScript<String> CREATE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return redis.call('HGET', KEYS[1], 'state')
            end

            redis.call('HSET', KEYS[1],
                'state', 'OPEN',
                'maxWaitingUsers', ARGV[2],
                'maxActiveUsers', ARGV[3],
                'admissionRatePerSecond', ARGV[4],
                'waitingInactivityTimeoutSeconds', ARGV[5],
                'activeInactivityTimeoutSeconds', ARGV[6],
                'maxActiveDurationSeconds', ARGV[7],
                'dataRetentionSeconds', ARGV[8])
            redis.call('SADD', KEYS[2], ARGV[1])
            return 'OPEN'
            """, String.class);

    private static final DefaultRedisScript<String> DRAIN_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 'NOT_FOUND'
            end
            local state = redis.call('HGET', KEYS[1], 'state')
            if state == 'CLOSED' then
                return 'CLOSED'
            end
            redis.call('HSET', KEYS[1], 'state', 'DRAINING')
            redis.call('SADD', KEYS[2], ARGV[1])
            return 'DRAINING'
            """, String.class);

    private static final DefaultRedisScript<String> CLOSE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 'NOT_FOUND'
            end
            if redis.call('ZCARD', KEYS[3]) > 0 or redis.call('ZCARD', KEYS[4]) > 0 then
                return 'EVENT_NOT_EMPTY'
            end

            redis.call('HSET', KEYS[1], 'state', 'CLOSED')
            redis.call('SREM', KEYS[2], ARGV[1])
            local retention = tonumber(ARGV[2])
            for index = 1, #KEYS do
                if index ~= 2 then
                    redis.call('EXPIRE', KEYS[index], retention)
                end
            end
            return 'CLOSED'
            """, String.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ReactiveHashOperations<String, String, String> hashes;
    private final ReactiveZSetOperations<String, String> sortedSets;

    public WaitingEventRepository(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashes = redisTemplate.opsForHash();
        this.sortedSets = redisTemplate.opsForZSet();
    }

    public Mono<WaitingEvent> create(String eventId, WaitingQueuePolicy policy) {
        return redisTemplate.execute(
                        CREATE_SCRIPT,
                        List.of(WaitingQueueRedisKeys.meta(eventId), WaitingQueueRedisKeys.OPEN_EVENTS),
                        List.of(
                                eventId,
                                Integer.toString(policy.maxWaitingUsers()),
                                Integer.toString(policy.maxActiveUsers()),
                                Integer.toString(policy.admissionRatePerSecond()),
                                Long.toString(policy.waitingInactivityTimeoutSeconds()),
                                Long.toString(policy.activeInactivityTimeoutSeconds()),
                                Long.toString(policy.maxActiveDurationSeconds()),
                                Long.toString(policy.dataRetentionSeconds())
                        )
                )
                .next()
                .then(find(eventId));
    }

    public Mono<WaitingEvent> find(String eventId) {
        Mono<Map<String, String>> meta = hashes.entries(WaitingQueueRedisKeys.meta(eventId)).collectMap(Map.Entry::getKey, Map.Entry::getValue);
        Mono<Long> waitingCount = sortedSets.size(WaitingQueueRedisKeys.waiting(eventId)).defaultIfEmpty(0L);
        Mono<Long> activeCount = sortedSets.size(WaitingQueueRedisKeys.active(eventId)).defaultIfEmpty(0L);

        return Mono.zip(meta, waitingCount, activeCount)
                .flatMap(tuple -> {
                    Map<String, String> values = tuple.getT1();
                    if (values.isEmpty()) {
                        return Mono.empty();
                    }
                    WaitingQueuePolicy policy = new WaitingQueuePolicy(
                            Integer.parseInt(values.get("maxWaitingUsers")),
                            Integer.parseInt(values.get("maxActiveUsers")),
                            Integer.parseInt(values.get("admissionRatePerSecond")),
                            Long.parseLong(values.getOrDefault("waitingInactivityTimeoutSeconds", "300")),
                            Long.parseLong(values.getOrDefault("activeInactivityTimeoutSeconds", "300")),
                            Long.parseLong(values.get("maxActiveDurationSeconds")),
                            Long.parseLong(values.get("dataRetentionSeconds"))
                    );
                    return Mono.just(new WaitingEvent(
                            eventId,
                            WaitingEventState.valueOf(values.get("state")),
                            policy,
                            tuple.getT2(),
                            tuple.getT3()
                    ));
                });
    }

    public Mono<String> drain(String eventId) {
        return redisTemplate.execute(
                        DRAIN_SCRIPT,
                        List.of(WaitingQueueRedisKeys.meta(eventId), WaitingQueueRedisKeys.OPEN_EVENTS),
                        List.of(eventId)
                )
                .next();
    }

    public Mono<String> close(String eventId, long retentionSeconds) {
        return redisTemplate.execute(
                        CLOSE_SCRIPT,
                        List.of(
                                WaitingQueueRedisKeys.meta(eventId),
                                WaitingQueueRedisKeys.OPEN_EVENTS,
                                WaitingQueueRedisKeys.waiting(eventId),
                                WaitingQueueRedisKeys.active(eventId),
                                WaitingQueueRedisKeys.state(eventId),
                                WaitingQueueRedisKeys.userSequence(eventId),
                                WaitingQueueRedisKeys.activeStarted(eventId),
                                WaitingQueueRedisKeys.activeLastRequest(eventId),
                                WaitingQueueRedisKeys.waitingLastSeen(eventId),
                                WaitingQueueRedisKeys.sequence(eventId),
                                WaitingQueueRedisKeys.admissionBudget(eventId)
                        ),
                        List.of(eventId, Long.toString(retentionSeconds))
                )
                .next();
    }

}
