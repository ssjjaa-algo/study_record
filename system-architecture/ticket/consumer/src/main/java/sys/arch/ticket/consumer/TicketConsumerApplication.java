package sys.arch.ticket.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "sys.arch.ticket.consumer",
        "sys.arch.ticket.config",
        "sys.arch.ticket.redis",
        "sys.arch.ticket.repository",
        "sys.arch.ticket.service"
})
public class TicketConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketConsumerApplication.class, args);
    }
}
