package sys.arch.ticket.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import sys.arch.ticket.api.TicketApiApplication;
import sys.arch.ticket.domain.IssueResult;
import sys.arch.ticket.domain.IssueStatus;
import sys.arch.ticket.domain.TicketEvent;
import sys.arch.ticket.repository.TicketEventRepository;
import sys.arch.ticket.repository.TicketIssueRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@SpringBootTest(classes = TicketApiApplication.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:ticket-api-concurrency-test;MODE=MYSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DbTicketIssueServiceConcurrencyTest {

    @Autowired
    private DbTicketIssueService issueService;

    @Autowired
    private TicketEventRepository ticketEventRepository;

    @Autowired
    private TicketIssueRepository ticketIssueRepository;

    @Test
    void doesNotOverIssueUnderConcurrentRequests() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            TicketEvent event = ticketEventRepository.save(TicketEvent.create("flash sale", 100));
            int requestCount = 500;
            int workerCount = 32;

            ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
            CountDownLatch start = new CountDownLatch(1);
            List<Callable<IssueStatus>> tasks = new ArrayList<>();

            for (int i = 0; i < requestCount; i++) {
                String userId = "user-" + i;
                tasks.add(() -> {
                    start.await();
                    return issueService.issue(event.getId(), userId).status();
                });
            }

            List<Future<IssueStatus>> futures = new ArrayList<>();
            for (Callable<IssueStatus> task : tasks) {
                futures.add(executorService.submit(task));
            }

            start.countDown();

            List<IssueStatus> results = new ArrayList<>();
            for (Future<IssueStatus> future : futures) {
                results.add(future.get());
            }
            executorService.shutdown();

            long successCount = results.stream()
                    .filter(status -> status == IssueStatus.SUCCESS)
                    .count();
            long soldOutCount = results.stream()
                    .filter(status -> status == IssueStatus.SOLD_OUT)
                    .count();

            TicketEvent refreshedEvent = ticketEventRepository.findById(event.getId()).orElseThrow();
            assertThat(successCount).isEqualTo(100);
            assertThat(soldOutCount).isEqualTo(400);
            assertThat(ticketIssueRepository.countByEventId(event.getId())).isEqualTo(100);
            assertThat(refreshedEvent.getRemainingQuantity()).isZero();
        });
    }

    @Test
    void duplicateUserDoesNotDecreaseStockAgain() {
        TicketEvent event = ticketEventRepository.save(TicketEvent.create("single user", 10));

        IssueResult first = issueService.issue(event.getId(), "user-1");
        IssueResult second = issueService.issue(event.getId(), "user-1");

        TicketEvent refreshedEvent = ticketEventRepository.findById(event.getId()).orElseThrow();
        assertThat(first.status()).isEqualTo(IssueStatus.SUCCESS);
        assertThat(second.status()).isEqualTo(IssueStatus.DUPLICATED);
        assertThat(ticketIssueRepository.countByEventId(event.getId())).isEqualTo(1);
        assertThat(refreshedEvent.getRemainingQuantity()).isEqualTo(9);
    }
}
