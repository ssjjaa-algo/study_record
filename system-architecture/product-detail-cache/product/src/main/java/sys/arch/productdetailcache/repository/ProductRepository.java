package sys.arch.productdetailcache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sys.arch.productdetailcache.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
