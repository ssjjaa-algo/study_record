package sys.arch.ticket.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "sys.arch.ticket")
public class TicketApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketApiApplication.class, args);
    }
}
