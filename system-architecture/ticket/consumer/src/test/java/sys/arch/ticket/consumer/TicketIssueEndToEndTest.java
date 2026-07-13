package sys.arch.ticket.consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import sys.arch.ticket.redis.TicketRedisKeys;
import sys.arch.ticket.repository.TicketEventRepository;
import sys.arch.ticket.repository.TicketIssueRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(
        classes = TicketIssueEndToEndTest.E2eApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:ticket-e2e-test;MODE=MYSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.main.web-application-type=servlet",
        "spring.main.banner-mode=off",
        "logging.level.org.springframework=WARN",
        "logging.level.org.hibernate=WARN",
        "logging.level.com.zaxxer.hikari=WARN",
        "ticket.consumer.enabled=true",
        "ticket.consumer.batch-size=100",
        "ticket.consumer.poll-delay-ms=20"
})
class TicketIssueEndToEndTest {

    private static final String STREAM_KEY = "ticket:e2e:issue:stream:" + UUID.randomUUID();
    private static final String FAILURE_STREAM_KEY = STREAM_KEY + ":failed";
    private static final String GROUP_NAME = "ticket-e2e-consumers-" + UUID.randomUUID();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
    private static final Pattern STATUS_PATTERN = Pattern.compile("\"status\"\\s*:\\s*\"([A-Z_]+)\"");

    @LocalServerPort
    private int port;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TicketEventRepository ticketEventRepository;

    @Autowired
    private TicketIssueRepository ticketIssueRepository;

    private Long eventId;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> 6379);
        registry.add("ticket.issue.stream-key", () -> STREAM_KEY);
        registry.add("ticket.issue.failure-stream-key", () -> FAILURE_STREAM_KEY);
        registry.add("ticket.consumer.group-name", () -> GROUP_NAME);
        registry.add("ticket.consumer.name", () -> "ticket-e2e-consumer");
    }

    @AfterEach
    void cleanup() {
        if (!isRedisAvailable()) {
            return;
        }
        if (eventId != null) {
            redisTemplate.delete(List.of(
                    TicketRedisKeys.stock(eventId),
                    TicketRedisKeys.issuedUsers(eventId),
                    TicketRedisKeys.soldOut(eventId)
            ));
        }
        redisTemplate.delete(STREAM_KEY);
        redisTemplate.delete(FAILURE_STREAM_KEY);
    }

    @Test
    void issueRequestsFlowThroughRedisStreamAndConsumerToDb() throws Exception {
        assumeTrue(isRedisAvailable(), "Local Redis must be running on localhost:6379 for E2E test.");

        eventId = createEvent(5);

        List<HttpResponse<String>> responses = issueConcurrently(eventId, 12);

        long acceptedCount = responses.stream()
                .filter(response -> response.statusCode() == 202)
                .count();
        long soldOutCount = responses.stream()
                .filter(response -> response.statusCode() == 410)
                .count();

        assertThat(acceptedCount).isEqualTo(5);
        assertThat(soldOutCount).isEqualTo(7);
        printIssueResponses(responses, acceptedCount, soldOutCount);

        awaitUntil(() ->
                ticketIssueRepository.countByEventId(eventId) == 5
                        && ticketEventRepository.findById(eventId)
                        .orElseThrow()
                        .getRemainingQuantity() == 0
        );

        long issuedCount = ticketIssueRepository.countByEventId(eventId);
        int remainingQuantity = ticketEventRepository.findById(eventId)
                .orElseThrow()
                .getRemainingQuantity();
        printDbResult(issuedCount, remainingQuantity);
    }

    private Long createEvent(int totalQuantity) throws IOException, InterruptedException {
        HttpResponse<String> response = post(
                "/api/ticket-events",
                """
                        {
                          "name": "e2e flash sale",
                          "totalQuantity": %d
                        }
                        """.formatted(totalQuantity)
        );

        assertThat(response.statusCode()).isEqualTo(201);
        return extractId(response.body());
    }

    private List<HttpResponse<String>> issueConcurrently(Long eventId, int requestCount) {
        List<CompletableFuture<HttpResponse<String>>> futures = IntStream.range(0, requestCount)
                .mapToObj(index -> HTTP_CLIENT.sendAsync(
                        postRequest(
                                "/api/ticket-events/" + eventId + "/issues",
                                """
                                        {
                                          "userId": "e2e-user-%d"
                                        }
                                        """.formatted(index)
                        ),
                        HttpResponse.BodyHandlers.ofString()
                ))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(postRequest(path, body), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest postRequest(String path, String body) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(5))
                .build();
    }

    private void awaitUntil(BooleanSupplier condition) throws InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(5));
        while (Instant.now().isBefore(deadline)) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(50);
        }
        assertThat(condition.getAsBoolean()).isTrue();
    }

    private static boolean isRedisAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 6379), 300);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private Long extractId(String responseBody) {
        Matcher matcher = ID_PATTERN.matcher(responseBody);
        assertThat(matcher.find()).isTrue();
        return Long.valueOf(matcher.group(1));
    }

    private void printIssueResponses(
            List<HttpResponse<String>> responses,
            long acceptedCount,
            long soldOutCount
    ) {
        System.out.println();
        System.out.println("=== Ticket E2E issue responses ===");
        System.out.println("eventId=" + eventId + ", requests=12, stock=5");
        System.out.println("----------------------------------");
        System.out.println("userId       | HTTP | domainStatus");
        System.out.println("----------------------------------");

        for (int i = 0; i < responses.size(); i++) {
            HttpResponse<String> response = responses.get(i);
            System.out.printf(
                    "e2e-user-%-2d | %-4d | %s%n",
                    i,
                    response.statusCode(),
                    extractStatus(response.body())
            );
        }

        System.out.println("----------------------------------");
        System.out.println("accepted=" + acceptedCount + ", soldOut=" + soldOutCount);
        System.out.println();
    }

    private void printDbResult(long issuedCount, int remainingQuantity) {
        System.out.println("=== Ticket E2E DB result ===");
        System.out.println("ticket_issue rows=" + issuedCount);
        System.out.println("remainingQuantity=" + remainingQuantity);
        System.out.println("============================");
        System.out.println();
    }

    private String extractStatus(String responseBody) {
        Matcher matcher = STATUS_PATTERN.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "UNKNOWN";
    }

    @EnableScheduling
    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "sys.arch.ticket.api.controller",
            "sys.arch.ticket.config",
            "sys.arch.ticket.consumer",
            "sys.arch.ticket.redis",
            "sys.arch.ticket.repository",
            "sys.arch.ticket.service"
    })
    static class E2eApplication {
    }
}
