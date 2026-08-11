package perfomance.test.queue.waiting.createwaitingqueue;

import java.util.Map;

import perfomance.test.queue.waiting.config.WaitingQueueProperties;
import perfomance.test.queue.waiting.repository.WaitingQueueRedisKeys;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/waiting-events/{eventId}")
public class CreateWaitingQueueController {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final WaitingQueueProperties properties;

    public CreateWaitingQueueController(
            ReactiveStringRedisTemplate redisTemplate,
            WaitingQueueProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @PostMapping
    public Mono<Void> create(@PathVariable String eventId) {
        WaitingQueueProperties.Defaults defaults = properties.defaults();
        Map<String, String> meta = Map.of(
                "state", "OPEN",
                "nextSequence", "0",
                "lastAdmittedSequence", "0",
                "maxWaitingUsers", Integer.toString(defaults.maxWaitingUsers()),
                "maxActiveUsers", Integer.toString(defaults.maxActiveUsers()),
                "waitingInactivityTimeoutSeconds", Long.toString(defaults.waitingInactivityTimeoutSeconds()),
                "activeInactivityTimeoutSeconds", Long.toString(defaults.activeInactivityTimeoutSeconds())
        );

        return redisTemplate.opsForHash()
                .putAll(WaitingQueueRedisKeys.meta(eventId), meta)
                .then(redisTemplate.opsForSet().add(WaitingQueueRedisKeys.OPEN_EVENTS, eventId))
                .then();
    }
}
