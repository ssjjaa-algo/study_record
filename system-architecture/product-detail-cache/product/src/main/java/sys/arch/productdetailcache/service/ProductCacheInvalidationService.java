package sys.arch.productdetailcache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import sys.arch.productdetailcache.api.dto.ProductResponse;
import sys.arch.productdetailcache.domain.CacheInvalidationStatus;
import sys.arch.productdetailcache.domain.ProductCacheInvalidationEvent;
import sys.arch.productdetailcache.repository.ProductCacheInvalidationEventRepository;
import sys.arch.productdetailcache.repository.ProductRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ProductCacheInvalidationService {

    private static final Logger log = LoggerFactory.getLogger(ProductCacheInvalidationService.class);
    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:detail:";
    private static final String PRODUCT_VERSION_KEY_PREFIX = "product:version:";

    private final ProductCacheInvalidationEventRepository eventRepository;
    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration cacheTtl;
    private final Duration versionTtl;
    private final int maxRetryCount;

    public ProductCacheInvalidationService(
            ProductCacheInvalidationEventRepository eventRepository,
            ProductRepository productRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${product.cache.ttl-seconds:600}") long cacheTtlSeconds,
            @Value("${product.cache.version-ttl-seconds:3600}") long versionTtlSeconds,
            @Value("${product.cache.max-retry-count:20}") int maxRetryCount
    ) {
        this.eventRepository = eventRepository;
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
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
            if (hasNewerCacheVersion(event)) {
                event.markProcessed();
                return;
            }

            ProductResponse product = productRepository.findById(event.getProductId())
                    .map(ProductResponse::from)
                    .orElseThrow(() -> new IllegalStateException("Product not found. productId=" + event.getProductId()));

            redisTemplate.opsForValue().set(
                    productVersionKey(product.id()),
                    String.valueOf(product.version()),
                    versionTtl
            );
            redisTemplate.opsForValue().set(
                    productCacheKey(product.id()),
                    objectMapper.writeValueAsString(product),
                    cacheTtl
            );
            event.markProcessed();
        } catch (RuntimeException | JsonProcessingException e) {
            markRetryOrFailed(event, e);
        }
    }

    @Transactional
    public void retryFailedEvent(Long eventId) {
        ProductCacheInvalidationEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        event.markPendingForRetry();
    }

    private boolean hasNewerCacheVersion(ProductCacheInvalidationEvent event) {
        String versionValue = redisTemplate.opsForValue().get(productVersionKey(event.getProductId()));
        return versionValue != null && Long.parseLong(versionValue) > event.getProductVersion();
    }

    private void markRetryOrFailed(ProductCacheInvalidationEvent event, Exception exception) {
        String errorMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();

        if (event.getRetryCount() + 1 >= maxRetryCount) {
            event.markFailed(errorMessage);
            log.error(
                    "Product cache refresh moved to FAILED. eventId={}, productId={}, version={}, retryCount={}",
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
                "Product cache refresh failed. eventId={}, productId={}, version={}, retryCount={}",
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
