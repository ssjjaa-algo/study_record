package sys.arch.productdetailcache.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_cache_invalidation_events")
public class ProductCacheInvalidationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private long productVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CacheInvalidationStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime nextRetryAt;

    @Column(length = 1000)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private LocalDateTime failedAt;

    protected ProductCacheInvalidationEvent() {
    }

    public ProductCacheInvalidationEvent(Long productId, long productVersion) {
        this.productId = productId;
        this.productVersion = productVersion;
        this.status = CacheInvalidationStatus.PENDING;
        this.retryCount = 0;
        this.nextRetryAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (nextRetryAt == null) {
            nextRetryAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (status == null) {
            status = CacheInvalidationStatus.PENDING;
        }
    }

    public void markProcessed() {
        this.status = CacheInvalidationStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.failedAt = null;
        this.lastError = null;
    }

    public void markRetry(String errorMessage) {
        this.retryCount++;
        this.status = CacheInvalidationStatus.PENDING;
        this.lastError = errorMessage;
        this.nextRetryAt = LocalDateTime.now().plusSeconds(nextRetryDelaySeconds());
    }

    public void markFailed(String errorMessage) {
        this.retryCount++;
        this.status = CacheInvalidationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.lastError = errorMessage;
    }

    public void markPendingForRetry() {
        this.status = CacheInvalidationStatus.PENDING;
        this.nextRetryAt = LocalDateTime.now();
        this.failedAt = null;
    }

    private long nextRetryDelaySeconds() {
        return Math.min(60L, (long) Math.pow(2, retryCount));
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public long getProductVersion() {
        return productVersion;
    }

    public boolean isPending() {
        return status == CacheInvalidationStatus.PENDING;
    }

    public CacheInvalidationStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
