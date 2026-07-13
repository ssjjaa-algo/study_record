package sys.arch.ticket.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import sys.arch.ticket.domain.IssueResult;
import sys.arch.ticket.service.TicketIssuePersistenceService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketIssueStreamConsumerTest {

    private static final String STREAM_KEY = "ticket:test:issue:stream";
    private static final String FAILURE_STREAM_KEY = "ticket:test:issue:stream:failed";
    private static final String GROUP_NAME = "ticket-test-consumers";
    private static final String CONSUMER_NAME = "ticket-test-consumer";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private TicketIssuePersistenceService persistenceService;

    @Mock
    private StreamOperations<String, String, String> streamOperations;

    private TicketIssueStreamConsumer consumer;

    @BeforeEach
    void setUp() {
        when(redisTemplate.<String, String>opsForStream()).thenReturn(streamOperations);
        consumer = new TicketIssueStreamConsumer(
                redisTemplate,
                persistenceService,
                STREAM_KEY,
                FAILURE_STREAM_KEY,
                GROUP_NAME,
                CONSUMER_NAME,
                10,
                1_000,
                3
        );
        ReflectionTestUtils.setField(consumer, "groupInitialized", true);
        givenNoPendingMessages();
    }

    @Test
    void acknowledgesAfterSuccessfulPersistence() {
        MapRecord<String, String, String> record = record("1-0", "1", "user-1");
        givenNewMessages(record);
        when(persistenceService.persist(1L, "user-1")).thenReturn(IssueResult.success(1L, "user-1"));

        consumer.poll();

        verify(streamOperations).acknowledge(STREAM_KEY, GROUP_NAME, "1-0");
        verify(streamOperations, never()).add(eq(FAILURE_STREAM_KEY), any(Map.class));
    }

    @Test
    void movesDbRejectedMessageToFailureStreamAndAcknowledges() {
        MapRecord<String, String, String> record = record("1-1", "1", "user-2");
        givenNewMessages(record);
        when(persistenceService.persist(1L, "user-2")).thenReturn(IssueResult.soldOut(1L, "user-2"));

        consumer.poll();

        ArgumentCaptor<Map<String, String>> failureRecordCaptor = ArgumentCaptor.forClass(Map.class);
        verify(streamOperations).add(eq(FAILURE_STREAM_KEY), failureRecordCaptor.capture());
        verify(streamOperations).acknowledge(STREAM_KEY, GROUP_NAME, "1-1");

        assertThat(failureRecordCaptor.getValue())
                .containsEntry("originalStreamId", "1-1")
                .containsEntry("eventId", "1")
                .containsEntry("userId", "user-2")
                .containsEntry("reason", "SOLD_OUT")
                .containsEntry("detail", "DB_REJECTED");
    }

    @Test
    void doesNotAcknowledgeTransientPersistenceFailureBeforeMaxDeliveryCount() {
        MapRecord<String, String, String> record = record("1-2", "1", "user-3");
        givenNewMessages(record);
        when(persistenceService.persist(1L, "user-3")).thenThrow(new RuntimeException("db timeout"));

        consumer.poll();

        verify(streamOperations, never()).acknowledge(anyString(), anyString(), anyString());
        verify(streamOperations, never()).add(eq(FAILURE_STREAM_KEY), any(Map.class));
    }

    private void givenNoPendingMessages() {
        when(streamOperations.pending(
                eq(STREAM_KEY),
                eq(GROUP_NAME),
                any(Range.class),
                anyLong(),
                eq(Duration.ofMillis(1_000))
        )).thenReturn(new PendingMessages(GROUP_NAME, List.of()));
    }

    @SuppressWarnings("unchecked")
    private void givenNewMessages(MapRecord<String, String, String> record) {
        when(streamOperations.read(
                any(Consumer.class),
                any(StreamReadOptions.class),
                any(StreamOffset.class)
        )).thenReturn(List.of(record));
    }

    private MapRecord<String, String, String> record(String id, String eventId, String userId) {
        return MapRecord.create(
                STREAM_KEY,
                Map.of(
                        "eventId", eventId,
                        "userId", userId
                )
        ).withId(RecordId.of(id));
    }
}
