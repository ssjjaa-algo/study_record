package sys.arch.ticket.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTicketStockInitializer {

    private final StringRedisTemplate redisTemplate;

    public RedisTicketStockInitializer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void initialize(Long eventId, int totalQuantity) {
        redisTemplate.opsForValue().set(TicketRedisKeys.stock(eventId), String.valueOf(totalQuantity));
        redisTemplate.delete(TicketRedisKeys.issuedUsers(eventId));
        redisTemplate.delete(TicketRedisKeys.soldOut(eventId));
    }
}
