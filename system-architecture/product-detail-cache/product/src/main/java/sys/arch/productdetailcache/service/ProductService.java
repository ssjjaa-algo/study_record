package sys.arch.productdetailcache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import sys.arch.productdetailcache.api.dto.ProductResponse;
import sys.arch.productdetailcache.api.dto.ProductUpdateRequest;
import sys.arch.productdetailcache.domain.ProductCacheInvalidationEvent;
import sys.arch.productdetailcache.domain.Product;
import sys.arch.productdetailcache.repository.ProductCacheInvalidationEventRepository;
import sys.arch.productdetailcache.repository.ProductRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:detail:";
    private static final String PRODUCT_VERSION_KEY_PREFIX = "product:version:";

    private final ProductRepository productRepository;
    private final ProductCacheInvalidationEventRepository eventRepository;
    private final ProductCacheInvalidationService productCacheInvalidationService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration cacheTtl;

    public ProductService(
            ProductRepository productRepository,
            ProductCacheInvalidationEventRepository eventRepository,
            ProductCacheInvalidationService productCacheInvalidationService,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${product.cache.ttl-seconds:600}") long cacheTtlSeconds
    ) {
        this.productRepository = productRepository;
        this.eventRepository = eventRepository;
        this.productCacheInvalidationService = productCacheInvalidationService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        return getProduct(productId, 0);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId, long cacheWriteDelayMs) {
        ProductResponse cachedProduct = getCachedProduct(productId);
        if (cachedProduct != null) {
            return cachedProduct;
        }

        return productRepository.findById(productId)
                .map(ProductResponse::from)
                .map(product -> {
                    delayBeforeCacheWrite(cacheWriteDelayMs);
                    cacheProduct(product);
                    return product;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        product.update(request.name(), request.price(), request.description(), request.popular());
        ProductResponse response = ProductResponse.from(product);
        ProductCacheInvalidationEvent event = eventRepository.save(
                new ProductCacheInvalidationEvent(response.id(), response.version())
        );

        processInvalidationAfterCommit(event.getId());

        return response;
    }

    @Transactional
    public long seedProducts() {
        eventRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();

        List<Product> products = new ArrayList<>(10_000);
        for (long id = 1; id <= 10_000; id++) {
            products.add(new Product(
                    id,
                    "product-" + id,
                    (int) id * 100,
                    "test product " + id,
                    id <= 100
            ));
        }

        productRepository.saveAll(products);
        clearProductCache();
        return products.size();
    }

    private ProductResponse getCachedProduct(Long productId) {
        String cacheKey = productCacheKey(productId);

        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue == null) {
                return null;
            }
            return objectMapper.readValue(cachedValue, ProductResponse.class);
        } catch (JsonProcessingException e) {
            redisTemplate.delete(cacheKey);
            log.warn("Invalid product cache value. key={}", cacheKey, e);
            return null;
        } catch (RuntimeException e) {
            log.warn("Product cache read failed. key={}", cacheKey, e);
            return null;
        }
    }

    private void cacheProduct(ProductResponse product) {
        String cacheKey = productCacheKey(product.id());

        try {
            if (isStaleProduct(product)) {
                log.info("Skip stale product cache write. productId={}, version={}", product.id(), product.version());
                return;
            }

            String cacheValue = objectMapper.writeValueAsString(product);
            redisTemplate.opsForValue().set(cacheKey, cacheValue, cacheTtl);
        } catch (JsonProcessingException e) {
            log.warn("Product cache serialization failed. key={}", cacheKey, e);
        } catch (RuntimeException e) {
            log.warn("Product cache write failed. key={}", cacheKey, e);
        }
    }

    private void delayBeforeCacheWrite(long delayMs) {
        if (delayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isStaleProduct(ProductResponse product) {
        String versionValue = redisTemplate.opsForValue().get(productVersionKey(product.id()));
        if (versionValue == null) {
            return false;
        }

        long currentVersion = Long.parseLong(versionValue);
        return product.version() < currentVersion;
    }

    private void clearProductCache() {
        try {
            redisTemplate.delete(redisTemplate.keys(PRODUCT_CACHE_KEY_PREFIX + "*"));
            redisTemplate.delete(redisTemplate.keys(PRODUCT_VERSION_KEY_PREFIX + "*"));
        } catch (RuntimeException e) {
            log.warn("Product cache clear failed.", e);
        }
    }

    private void processInvalidationAfterCommit(Long eventId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                productCacheInvalidationService.processEvent(eventId);
            }
        });
    }

    private String productCacheKey(Long productId) {
        return PRODUCT_CACHE_KEY_PREFIX + productId;
    }

    private String productVersionKey(Long productId) {
        return PRODUCT_VERSION_KEY_PREFIX + productId;
    }
}
