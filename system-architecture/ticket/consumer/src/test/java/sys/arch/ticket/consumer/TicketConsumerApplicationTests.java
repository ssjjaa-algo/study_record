package sys.arch.ticket.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "ticket.consumer.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:ticket-consumer-test;MODE=MYSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TicketConsumerApplicationTests {

    @Test
    void contextLoads() {
    }
}
