package sys.arch.ticket.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import sys.arch.ticket.domain.IssueResult;
import sys.arch.ticket.domain.IssueStatus;

import java.util.List;

@Service
public class RedisTicketIssueService {

    private static final String ISSUE_SCRIPT = """
            if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                return 'DUPLICATED'
            end

            if redis.call('EXISTS', KEYS[4]) == 1 then
                return 'SOLD_OUT'
            end

            local stock = tonumber(redis.call('GET', KEYS[2]))
            if stock == nil then
                return 'NOT_READY'
            end

            if stock <= 0 then
                redis.call('SET', KEYS[4], 'true', 'EX', ARGV[3])
                return 'SOLD_OUT'
            end

            local remaining = redis.call('DECR', KEYS[2])
            redis.call('SADD', KEYS[1], ARGV[1])
            redis.call(
                'XADD',
                KEYS[3],
                '*',
                'eventId',
                ARGV[2],
                'userId',
                ARGV[1]
            )

            if remaining <= 0 then
                redis.call('SET', KEYS[4], 'true', 'EX', ARGV[3])
            end

            return 'SUCCESS'
            """;

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<String> issueScript;
    private final String issueStreamKey;
    private final String soldOutFlagTtlSeconds;

    public RedisTicketIssueService(
            StringRedisTemplate redisTemplate,
            @Value("${ticket.issue.stream-key:ticket:issue:stream}") String issueStreamKey,
            @Value("${ticket.sold-out-flag-ttl-seconds:60}") int soldOutFlagTtlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.issueStreamKey = issueStreamKey;
        this.soldOutFlagTtlSeconds = String.valueOf(soldOutFlagTtlSeconds);
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(ISSUE_SCRIPT);
        script.setResultType(String.class);
        this.issueScript = script;
    }

    public IssueResult issue(Long eventId, String userId) {
        String result = redisTemplate.execute(
                issueScript,
                List.of(
                        TicketRedisKeys.issuedUsers(eventId),
                        TicketRedisKeys.stock(eventId),
                        issueStreamKey,
                        TicketRedisKeys.soldOut(eventId)
                ),
                userId,
                String.valueOf(eventId),
                soldOutFlagTtlSeconds
        );

        IssueStatus status = result == null ? IssueStatus.NOT_READY : IssueStatus.valueOf(result);
        return new IssueResult(status, eventId, userId);
    }
}
