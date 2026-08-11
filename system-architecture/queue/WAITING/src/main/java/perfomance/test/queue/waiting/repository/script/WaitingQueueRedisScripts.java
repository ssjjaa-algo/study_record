package perfomance.test.queue.waiting.repository.script;

import org.springframework.data.redis.core.script.DefaultRedisScript;

public final class WaitingQueueRedisScripts {

    public static final DefaultRedisScript<String> REGISTER = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 'EVENT_NOT_FOUND|0|-1|0'
            end

            local redisTime = redis.call('TIME')
            local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
            local userId = ARGV[1]

            if redis.call('ZSCORE', KEYS[4], userId) then
                redis.call('ZADD', KEYS[4], 'XX', nowMillis, userId)
                return 'ACTIVE|0|-1|0'
            end

            if redis.call('HGET', KEYS[1], 'state') ~= 'OPEN' then
                return 'EVENT_NOT_OPEN|0|-1|0'
            end

            if redis.call('ZSCORE', KEYS[2], userId) then
                redis.call('ZREM', KEYS[2], userId)
                redis.call('ZREM', KEYS[3], userId)
            end

            local maxWaitingUsers = tonumber(redis.call('HGET', KEYS[1], 'maxWaitingUsers'))
            if redis.call('ZCARD', KEYS[2]) >= maxWaitingUsers then
                return 'QUEUE_FULL|0|-1|0'
            end

            local sequence = redis.call('HINCRBY', KEYS[1], 'nextSequence', 1)
            redis.call('ZADD', KEYS[2], sequence, userId)
            redis.call('ZADD', KEYS[3], nowMillis, userId)
            local rank = redis.call('ZRANK', KEYS[2], userId)
            return 'WAITING|' .. sequence .. '|' .. rank .. '|1'
            """, String.class);

    public static final DefaultRedisScript<String> STATUS = new DefaultRedisScript<>("""
            local redisTime = redis.call('TIME')
            local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
            local userId = ARGV[1]

            if redis.call('ZSCORE', KEYS[3], userId) then
                redis.call('ZADD', KEYS[3], 'XX', nowMillis, userId)
                return 'ACTIVE|0|-1|0'
            end

            local sequence = redis.call('ZSCORE', KEYS[1], userId)
            if sequence then
                local rank = redis.call('ZRANK', KEYS[1], userId)
                redis.call('ZADD', KEYS[2], nowMillis, userId)
                return 'WAITING|' .. sequence .. '|' .. rank .. '|0'
            end

            return 'NOT_FOUND|0|-1|0'
            """, String.class);

    public static final DefaultRedisScript<Long> ADMIT = new DefaultRedisScript<>("""
            local eventState = redis.call('HGET', KEYS[1], 'state')
            if eventState ~= 'OPEN' and eventState ~= 'DRAINING' then
                return 0
            end

            local maxActiveUsers = tonumber(redis.call('HGET', KEYS[1], 'maxActiveUsers'))
            local available = math.min(
                maxActiveUsers - redis.call('ZCARD', KEYS[4]),
                tonumber(ARGV[1])
            )
            if available <= 0 then
                return 0
            end

            local popped = redis.call('ZPOPMIN', KEYS[2], available)
            if #popped == 0 then
                return 0
            end

            local redisTime = redis.call('TIME')
            local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
            local admittedUsers = {}
            local activeEntries = {}

            for index = 1, #popped, 2 do
                local userId = popped[index]
                table.insert(admittedUsers, userId)
                table.insert(activeEntries, nowMillis)
                table.insert(activeEntries, userId)
            end

            redis.call('ZREM', KEYS[3], unpack(admittedUsers))
            redis.call('ZADD', KEYS[4], unpack(activeEntries))
            redis.call('HSET', KEYS[1], 'lastAdmittedSequence', popped[#popped])
            return math.floor(#popped / 2)
            """, Long.class);

    public static final DefaultRedisScript<String> RELEASE = new DefaultRedisScript<>("""
            local userId = ARGV[1]
            if redis.call('ZREM', KEYS[1], userId) == 0 then
                return 'NOT_FOUND|0|-1|0'
            end
            return 'COMPLETED|0|-1|0'
            """, String.class);

    public static final DefaultRedisScript<Long> EXPIRE = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 0
            end

            local redisTime = redis.call('TIME')
            local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
            local batchSize = tonumber(ARGV[1])
            local waitingTimeoutSeconds = tonumber(redis.call('HGET', KEYS[1], 'waitingInactivityTimeoutSeconds'))
            local activeTimeoutSeconds = tonumber(redis.call('HGET', KEYS[1], 'activeInactivityTimeoutSeconds'))

            local staleWaiting = redis.call(
                'ZRANGEBYSCORE', KEYS[3], '-inf', nowMillis - waitingTimeoutSeconds * 1000,
                'LIMIT', 0, batchSize
            )
            if #staleWaiting > 0 then
                redis.call('ZREM', KEYS[2], unpack(staleWaiting))
                redis.call('ZREM', KEYS[3], unpack(staleWaiting))
            end

            local staleActive = redis.call(
                'ZRANGEBYSCORE', KEYS[4], '-inf', nowMillis - activeTimeoutSeconds * 1000,
                'LIMIT', 0, batchSize
            )
            if #staleActive > 0 then
                redis.call('ZREM', KEYS[4], unpack(staleActive))
            end

            return #staleWaiting + #staleActive
            """, Long.class);

    private WaitingQueueRedisScripts() {
    }
}
