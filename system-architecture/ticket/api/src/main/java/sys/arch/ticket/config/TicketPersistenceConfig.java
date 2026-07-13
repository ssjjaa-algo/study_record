package sys.arch.ticket.config;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@AutoConfigurationPackage(basePackages = "sys.arch.ticket")
@EnableJpaRepositories(basePackages = "sys.arch.ticket.repository")
public class TicketPersistenceConfig {
}
