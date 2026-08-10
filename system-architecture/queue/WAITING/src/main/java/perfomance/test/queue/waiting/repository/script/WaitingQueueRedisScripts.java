package perfomance.test.queue.waiting.repository.script;

import org.springframework.data.redis.core.script.DefaultRedisScript;

public final class WaitingQueueRedisScripts {

    public static final DefaultRedisScript<String> REGISTER = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 'EVENT_NOT_FOUND|0|-1|0|0|0'
            end

            local redisTime = redis.call('TIME')
            local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
            local userId = ARGV[1]
            local currentState = redis.call('HGET', KEYS[4], userId)
            local sequence = redis.call('HGET', KEYS[5], userId) or '0'

            if currentState == 'ACTIVE' then
                local expiry = redis.call('ZSCORE', KEYS[3], userId)
                if expiry and tonumber(expiry) > nowMillis then
                    redis.call('ZREM', KEYS[8], userId)
                    return 'ACTIVE|' .. sequence .. '|-1|' .. expiry .. '|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
                end
                redis.call('ZREM', KEYS[3], userId)
                redis.call('HDEL', KEYS[4], userId)
                redis.call('HDEL', KEYS[5], userId)
                redis.call('HDEL', KEYS[7], userId)
            end

            if redis.call('HGET', KEYS[1], 'state') ~= 'OPEN' then
                return 'EVENT_NOT_OPEN|0|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            end

            if currentState == 'WAITING' then
                redis.call('ZREM', KEYS[2], userId)
                redis.call('ZREM', KEYS[8], userId)
            end

            local maxWaitingUsers = tonumber(redis.call('HGET', KEYS[1], 'maxWaitingUsers'))
            if redis.call('ZCARD', KEYS[2]) >= maxWaitingUsers then
                return 'QUEUE_FULL|0|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            end

            sequence = redis.call('INCR', KEYS[6])
            redis.call('ZADD', KEYS[2], sequence, userId)
            redis.call('ZADD', KEYS[8], nowMillis, userId)
            redis.call('HSET', KEYS[4], userId, 'WAITING')
            redis.call('HSET', KEYS[5], userId, sequence)
            redis.call('HDEL', KEYS[7], userId)
            local rank = redis.call('ZRANK', KEYS[2], userId)
            return 'WAITING|' .. sequence .. '|' .. rank .. '|0|' .. redis.call('ZCARD', KEYS[3]) .. '|1'
            """, String.class);

    public static final DefaultRedisScript<String> STATUS = new DefaultRedisScript<>("""
            local userId = ARGV[1]
            local state = redis.call('HGET', KEYS[4], userId)
            local sequence = redis.call('HGET', KEYS[5], userId) or '0'

            if state == 'WAITING' then
                local rank = redis.call('ZRANK', KEYS[2], userId)
                if rank then
                    local redisTime = redis.call('TIME')
                    local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
                    redis.call('ZADD', KEYS[8], nowMillis, userId)
                    return 'WAITING|' .. sequence .. '|' .. rank .. '|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
                end
            end

            if state == 'ACTIVE' then
                local redisTime = redis.call('TIME')
                local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
                local expiry = redis.call('ZSCORE', KEYS[3], userId)
                if expiry and tonumber(expiry) > nowMillis then
                    return 'ACTIVE|' .. sequence .. '|-1|' .. expiry .. '|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
                end
                redis.call('ZREM', KEYS[3], userId)
                redis.call('HDEL', KEYS[4], userId)
                redis.call('HDEL', KEYS[5], userId)
                redis.call('HDEL', KEYS[7], userId)
                return 'EXPIRED|' .. sequence .. '|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            end

            return 'NOT_FOUND|0|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            """, String.class);

    public static final DefaultRedisScript<Long> ADMIT = new DefaultRedisScript<>("""
            local eventState = redis.call('HGET', KEYS[1], 'state')
            if eventState ~= 'OPEN' and eventState ~= 'DRAINING' then
                return 0
            end

            local redisTime = redis.call('TIME')
            local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)

            local waitingTimeoutSeconds = tonumber(redis.call('HGET', KEYS[1], 'waitingInactivityTimeoutSeconds') or '300')
            local staleWaiting = redis.call(
                'ZRANGEBYSCORE', KEYS[8], '-inf', nowMillis - waitingTimeoutSeconds * 1000,
                'LIMIT', 0, tonumber(ARGV[1])
            )
            for _, userId in ipairs(staleWaiting) do
                if redis.call('HGET', KEYS[4], userId) == 'WAITING' then
                    redis.call('ZREM', KEYS[2], userId)
                    redis.call('HDEL', KEYS[4], userId)
                    redis.call('HDEL', KEYS[5], userId)
                end
                redis.call('ZREM', KEYS[8], userId)
            end

            local expired = redis.call('ZRANGEBYSCORE', KEYS[3], '-inf', nowMillis, 'LIMIT', 0, tonumber(ARGV[1]))
            for _, userId in ipairs(expired) do
                redis.call('ZREM', KEYS[3], userId)
                redis.call('HDEL', KEYS[4], userId)
                redis.call('HDEL', KEYS[5], userId)
                redis.call('HDEL', KEYS[6], userId)
            end

            local rate = tonumber(redis.call('HGET', KEYS[1], 'admissionRatePerSecond'))
            local maxActive = tonumber(redis.call('HGET', KEYS[1], 'maxActiveUsers'))
            local tokens = tonumber(redis.call('HGET', KEYS[7], 'tokens') or rate)
            local lastRefill = tonumber(redis.call('HGET', KEYS[7], 'lastRefillMillis') or nowMillis)
            tokens = math.min(rate, tokens + math.max(0, nowMillis - lastRefill) * rate / 1000)

            local available = math.min(
                math.floor(tokens),
                maxActive - redis.call('ZCARD', KEYS[3]),
                tonumber(ARGV[2])
            )
            if available <= 0 then
                redis.call('HSET', KEYS[7], 'tokens', tokens, 'lastRefillMillis', nowMillis)
                redis.call('EXPIRE', KEYS[7], 2)
                return 0
            end

            local popped = redis.call('ZPOPMIN', KEYS[2], available)
            local admittedCount = math.floor(#popped / 2)
            local maxActiveDurationSeconds = tonumber(redis.call('HGET', KEYS[1], 'maxActiveDurationSeconds'))
            local activeInactivityTimeoutSeconds = tonumber(
                redis.call('HGET', KEYS[1], 'activeInactivityTimeoutSeconds') or maxActiveDurationSeconds
            )
            local expiresAt = nowMillis + math.min(maxActiveDurationSeconds, activeInactivityTimeoutSeconds) * 1000

            local admittedUsers = {}
            local activeEntries = {}
            local stateEntries = {}
            local activeStartedEntries = {}

            for index = 1, #popped, 2 do
                local userId = popped[index]
                table.insert(admittedUsers, userId)
                table.insert(activeEntries, expiresAt)
                table.insert(activeEntries, userId)
                table.insert(stateEntries, userId)
                table.insert(stateEntries, 'ACTIVE')
                table.insert(activeStartedEntries, userId)
                table.insert(activeStartedEntries, nowMillis)
            end

            if admittedCount > 0 then
                redis.call('ZREM', KEYS[8], unpack(admittedUsers))
                redis.call('ZADD', KEYS[3], unpack(activeEntries))
                redis.call('HSET', KEYS[4], unpack(stateEntries))
                redis.call('HSET', KEYS[6], unpack(activeStartedEntries))
                redis.call('HSET', KEYS[1], 'lastAdmittedSequence', popped[#popped])
            end

            redis.call('HSET', KEYS[7], 'tokens', tokens - admittedCount, 'lastRefillMillis', nowMillis)
            redis.call('EXPIRE', KEYS[7], 2)
            return admittedCount
            """, Long.class);

    public static final DefaultRedisScript<String> RELEASE = new DefaultRedisScript<>("""
            local userId = ARGV[1]
            local expectedSequence = ARGV[2]
            local currentState = redis.call('HGET', KEYS[4], userId)
            local currentSequence = redis.call('HGET', KEYS[5], userId)

            if not currentState or not currentSequence then
                return 'NOT_FOUND|0|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            end
            if tostring(currentSequence) ~= tostring(expectedSequence) then
                return 'ADMISSION_INVALID|' .. currentSequence .. '|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            end
            if currentState ~= 'ACTIVE' then
                return 'ADMISSION_INVALID|' .. currentSequence .. '|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            end

            redis.call('ZREM', KEYS[3], userId)
            redis.call('HDEL', KEYS[4], userId)
            redis.call('HDEL', KEYS[5], userId)
            redis.call('HDEL', KEYS[7], userId)
            redis.call('ZREM', KEYS[8], userId)
            return 'COMPLETED|' .. currentSequence .. '|-1|0|' .. redis.call('ZCARD', KEYS[3]) .. '|0'
            """, String.class);

    public static final DefaultRedisScript<Long> EXPIRE = new DefaultRedisScript<>("""
            local redisTime = redis.call('TIME')
            local nowMillis = tonumber(redisTime[1]) * 1000 + math.floor(tonumber(redisTime[2]) / 1000)
            local expired = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', nowMillis, 'LIMIT', 0, tonumber(ARGV[1]))
            for _, userId in ipairs(expired) do
                redis.call('ZREM', KEYS[1], userId)
                redis.call('HDEL', KEYS[2], userId)
                redis.call('HDEL', KEYS[3], userId)
                redis.call('HDEL', KEYS[4], userId)
            end
            return #expired
            """, Long.class);

    private WaitingQueueRedisScripts() {
    }
}
