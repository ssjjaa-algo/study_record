package perfomance.test.queue.booking.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "releaseExecutor", destroyMethod = "close")
    public ExecutorService releaseExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
