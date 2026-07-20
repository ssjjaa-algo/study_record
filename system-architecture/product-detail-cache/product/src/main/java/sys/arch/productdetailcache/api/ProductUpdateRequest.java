package sys.arch.productdetailcache.api;


public record ProductUpdateRequest(
        String name,
        int price,
        String description,
        boolean popular
) {
}
