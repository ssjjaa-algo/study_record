package sys.arch.productdetailcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ProductDetailCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductDetailCacheApplication.class, args);
    }
}
