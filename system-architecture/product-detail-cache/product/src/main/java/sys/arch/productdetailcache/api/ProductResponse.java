package sys.arch.productdetailcache.api;

import sys.arch.productdetailcache.domain.Product;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        int price,
        String description,
        boolean popular,
        long version,
        LocalDateTime updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.isPopular(),
                product.getVersion(),
                product.getUpdatedAt()
        );
    }
}
