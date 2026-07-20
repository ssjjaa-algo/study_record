package sys.arch.productdetailcache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import sys.arch.productdetailcache.domain.CacheInvalidationStatus;
import sys.arch.productdetailcache.domain.ProductCacheInvalidationEvent;
import sys.arch.productdetailcache.repository.ProductCacheInvalidationEventRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ProductCacheInvalidationService {

    private static final Logger log = LoggerFactory.getLogger(ProductCacheInvalidationService.class);
    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:detail:";
    private static final String PRODUCT_VERSION_KEY_PREFIX = "product:version:";

    private final ProductCacheInvalidationEventRepository eventRepository;
    private final StringRedisTemplate redisTemplate;
    private final Duration versionTtl;
    private final int maxRetryCount;

    public ProductCacheInvalidationService(
            ProductCacheInvalidationEventRepository eventRepository,
            StringRedisTemplate redisTemplate,
            @Value("${product.cache.version-ttl-seconds:3600}") long versionTtlSeconds,
            @Value("${product.cache.max-retry-count:20}") int maxRetryCount
    ) {
        this.eventRepository = eventRepository;
        this.redisTemplate = redisTemplate;
        this.versionTtl = Duration.ofSeconds(versionTtlSeconds);
        this.maxRetryCount = maxRetryCount;
    }

    @Scheduled(fixedDelayString = "${product.cache.invalidation-fixed-delay-ms:1000}")
    public void processPendingEvents() {
        eventRepository.findRetryableEvents(
                        CacheInvalidationStatus.PENDING,
                        LocalDateTime.now()
                )
                .forEach(event -> processEvent(event.getId()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEvent(Long eventId) {
        ProductCacheInvalidationEvent event = eventRepository.findById(eventId)
                .orElse(null);

        if (event == null || !event.isPending()) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(
                    productVersionKey(event.getProductId()),
                    String.valueOf(event.getProductVersion()),
                    versionTtl
            );
            redisTemplate.delete(productCacheKey(event.getProductId()));
            event.markProcessed();
        } catch (RuntimeException e) {
            markRetryOrFailed(event, e);
        }
    }

    @Transactional
    public void retryFailedEvent(Long eventId) {
        ProductCacheInvalidationEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        event.markPendingForRetry();
    }

    private void markRetryOrFailed(ProductCacheInvalidationEvent event, RuntimeException exception) {
        String errorMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();

        if (event.getRetryCount() + 1 >= maxRetryCount) {
            event.markFailed(errorMessage);
            log.error(
                    "Product cache invalidation moved to FAILED. eventId={}, productId={}, version={}, retryCount={}",
                    event.getId(),
                    event.getProductId(),
                    event.getProductVersion(),
                    event.getRetryCount(),
                    exception
            );
            return;
        }

        event.markRetry(errorMessage);
        log.warn(
                "Product cache invalidation failed. eventId={}, productId={}, version={}, retryCount={}",
                event.getId(),
                event.getProductId(),
                event.getProductVersion(),
                event.getRetryCount(),
                exception
        );
    }

    private String productCacheKey(Long productId) {
        return PRODUCT_CACHE_KEY_PREFIX + productId;
    }

    private String productVersionKey(Long productId) {
        return PRODUCT_VERSION_KEY_PREFIX + productId;
    }
}
