package perfomance.test.queue.waiting;

import perfomance.test.queue.waiting.config.WaitingQueueProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WaitingQueueProperties.class)
public class WaitingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaitingApplication.class, args);
    }
}
