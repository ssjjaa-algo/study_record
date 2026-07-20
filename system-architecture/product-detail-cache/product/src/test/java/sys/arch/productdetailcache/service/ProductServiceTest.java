package sys.arch.productdetailcache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sys.arch.productdetailcache.domain.Product;
import sys.arch.productdetailcache.repository.ProductCacheInvalidationEventRepository;
import sys.arch.productdetailcache.repository.ProductRepository;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductCacheInvalidationEventRepository eventRepository;

    @Mock
    ProductCacheInvalidationService invalidationService;

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Test
    void staleProductIsNotWrittenToCache() {
        ProductService productService = new ProductService(
                productRepository,
                eventRepository,
                invalidationService,
                redisTemplate,
                new ObjectMapper().registerModule(new JavaTimeModule()),
                600
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("product:detail:1")).thenReturn(null);
        when(valueOperations.get("product:version:1")).thenReturn("2");
        when(productRepository.findById(1L)).thenReturn(Optional.of(
                new Product(1L, "product-1", 1000, "old product", true)
        ));

        assertThat(productService.getProduct(1L).version()).isEqualTo(1);

        verify(valueOperations, never()).set(eq("product:detail:1"), anyString(), any(Duration.class));
    }
}
