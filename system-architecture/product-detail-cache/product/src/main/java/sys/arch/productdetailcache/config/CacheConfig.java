package sys.arch.productdetailcache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public ObjectMapper cacheObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }
}
