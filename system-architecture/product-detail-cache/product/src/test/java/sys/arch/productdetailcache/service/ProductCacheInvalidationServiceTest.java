package sys.arch.productdetailcache.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sys.arch.productdetailcache.domain.CacheInvalidationStatus;
import sys.arch.productdetailcache.domain.ProductCacheInvalidationEvent;
import sys.arch.productdetailcache.repository.ProductCacheInvalidationEventRepository;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCacheInvalidationServiceTest {

    @Mock
    ProductCacheInvalidationEventRepository eventRepository;

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Test
    void eventBecomesFailedWhenRedisKeepsFailing() {
        ProductCacheInvalidationService service = new ProductCacheInvalidationService(
                eventRepository,
                redisTemplate,
                3600,
                1
        );
        ProductCacheInvalidationEvent event = new ProductCacheInvalidationEvent(1L, 2L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("redis down"))
                .when(valueOperations).set("product:version:1", "2", Duration.ofSeconds(3600));

        service.processEvent(1L);

        assertThat(event.getStatus()).isEqualTo(CacheInvalidationStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
    }
}
