package sys.arch.productdetailcache.api.dto;


public record ProductUpdateRequest(
        String name,
        int price,
        String description,
        boolean popular
) {
}
