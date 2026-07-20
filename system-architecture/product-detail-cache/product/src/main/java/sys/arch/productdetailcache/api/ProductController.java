package sys.arch.productdetailcache.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sys.arch.productdetailcache.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") long cacheWriteDelayMs
    ) {
        return productService.getProduct(productId, cacheWriteDelayMs);
    }

    @PostMapping("/seed")
    public long seedProducts() {
        return productService.seedProducts();
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductUpdateRequest request
    ) {
        return productService.updateProduct(productId, request);
    }
}
