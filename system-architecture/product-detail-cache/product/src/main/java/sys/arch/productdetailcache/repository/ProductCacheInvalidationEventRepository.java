package sys.arch.productdetailcache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sys.arch.productdetailcache.domain.CacheInvalidationStatus;
import sys.arch.productdetailcache.domain.ProductCacheInvalidationEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductCacheInvalidationEventRepository extends JpaRepository<ProductCacheInvalidationEvent, Long> {

    @Query("""
            select e
            from ProductCacheInvalidationEvent e
            where e.status = :status
              and e.nextRetryAt <= :now
            order by e.id asc
            """)
    List<ProductCacheInvalidationEvent> findRetryableEvents(
            CacheInvalidationStatus status,
            LocalDateTime now
    );
}
