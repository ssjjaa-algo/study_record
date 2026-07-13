package sys.arch.ticket.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sys.arch.ticket.domain.IssueResult;
import sys.arch.ticket.domain.IssueStatus;
import sys.arch.ticket.service.TicketIssuePersistenceService;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Component
@ConditionalOnProperty(name = "ticket.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class TicketIssueStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketIssueStreamConsumer.class);

    private final StringRedisTemplate redisTemplate;
    private final TicketIssuePersistenceService persistenceService;
    private final String streamKey;
    private final String failureStreamKey;
    private final String groupName;
    private final String consumerName;
    private final int batchSize;
    private final Duration pendingMinIdle;
    private final long maxDeliveryCount;
    private volatile boolean groupInitialized;

    public TicketIssueStreamConsumer(
            StringRedisTemplate redisTemplate,
            TicketIssuePersistenceService persistenceService,
            @Value("${ticket.issue.stream-key:ticket:issue:stream}") String streamKey,
            @Value("${ticket.issue.failure-stream-key:ticket:issue:stream:failed}") String failureStreamKey,
            @Value("${ticket.consumer.group-name:ticket-issue-consumers}") String groupName,
            @Value("${ticket.consumer.name:}") String consumerName,
            @Value("${ticket.consumer.batch-size:100}") int batchSize,
            @Value("${ticket.consumer.pending-min-idle-ms:10000}") long pendingMinIdleMs,
            @Value("${ticket.consumer.max-delivery-count:5}") long maxDeliveryCount
    ) {
        this.redisTemplate = redisTemplate;
        this.persistenceService = persistenceService;
        this.streamKey = streamKey;
        this.failureStreamKey = failureStreamKey;
        this.groupName = groupName;
        this.consumerName = consumerName == null || consumerName.isBlank()
                ? "ticket-consumer-" + UUID.randomUUID()
                : consumerName;
        this.batchSize = batchSize;
        this.pendingMinIdle = Duration.ofMillis(pendingMinIdleMs);
        this.maxDeliveryCount = maxDeliveryCount;
    }

    @Scheduled(fixedDelayString = "${ticket.consumer.poll-delay-ms:100}")
    public void poll() {
        try {
            ensureConsumerGroup();

            StreamOperations<String, String, String> streamOperations = redisTemplate.opsForStream();
            processPendingRecords(streamOperations);

            List<MapRecord<String, String, String>> records = streamOperations.read(
                    Consumer.from(groupName, consumerName),
                    StreamReadOptions.empty().count(batchSize),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );

            if (records == null || records.isEmpty()) {
                return;
            }

            for (MapRecord<String, String, String> record : records) {
                processRecord(streamOperations, record, 1);
            }
        } catch (RedisConnectionFailureException exception) {
            log.warn("Redis is not available. streamKey={}", streamKey);
        } catch (RedisSystemException exception) {
            if (hasMessage(exception, "NOGROUP")) {
                groupInitialized = false;
                log.debug("Redis stream consumer group is not ready. streamKey={}, groupName={}", streamKey, groupName);
                return;
            }
            throw exception;
        }
    }

    private void processPendingRecords(StreamOperations<String, String, String> streamOperations) {
        PendingMessages pendingMessages = streamOperations.pending(
                streamKey,
                groupName,
                Range.unbounded(),
                batchSize,
                pendingMinIdle
        );
        if (pendingMessages.isEmpty()) {
            return;
        }

        List<RecordId> pendingIds = StreamSupport.stream(pendingMessages.spliterator(), false)
                .map(PendingMessage::getId)
                .toList();
        Map<String, Long> deliveryCountsById = deliveryCountsById(pendingMessages);

        List<MapRecord<String, String, String>> claimedRecords = streamOperations.claim(
                streamKey,
                groupName,
                consumerName,
                RedisStreamCommands.XClaimOptions.minIdle(pendingMinIdle)
                        .ids(pendingIds.toArray(RecordId[]::new))
        );

        for (MapRecord<String, String, String> record : claimedRecords) {
            long deliveryCount = deliveryCountsById.getOrDefault(record.getId().getValue(), 1L);
            processRecord(streamOperations, record, deliveryCount);
        }
    }

    private Map<String, Long> deliveryCountsById(PendingMessages pendingMessages) {
        Map<String, Long> deliveryCountsById = new HashMap<>();
        for (PendingMessage pendingMessage : pendingMessages) {
            deliveryCountsById.put(pendingMessage.getIdAsString(), pendingMessage.getTotalDeliveryCount());
        }
        return deliveryCountsById;
    }

    private void ensureConsumerGroup() {
        if (groupInitialized) {
            return;
        }

        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] rawStreamKey = Objects.requireNonNull(serializer.serialize(streamKey));
                connection.streamCommands().xGroupCreate(rawStreamKey, groupName, ReadOffset.from("0-0"), true);
                return null;
            });
            groupInitialized = true;
        } catch (RedisSystemException exception) {
            if (hasMessage(exception, "BUSYGROUP")) {
                groupInitialized = true;
                return;
            }
            throw exception;
        }
    }

    private void processRecord(
            StreamOperations<String, String, String> streamOperations,
            MapRecord<String, String, String> record,
            long deliveryCount
    ) {
        try {
            IssueResult result = persist(record);
            if (isAckable(result.status())) {
                acknowledge(streamOperations, record);
                return;
            }

            sendToFailureStream(streamOperations, record, result.status().name(), "DB_REJECTED");
            acknowledge(streamOperations, record);
        } catch (RuntimeException exception) {
            if (exception instanceof RedisConnectionFailureException || exception instanceof RedisSystemException) {
                throw exception;
            }

            if (deliveryCount >= maxDeliveryCount) {
                sendToFailureStream(streamOperations, record, exception.getClass().getSimpleName(), exception.getMessage());
                acknowledge(streamOperations, record);
                return;
            }

            log.warn(
                    "Failed to persist ticket issue. It will be retried. streamId={}, deliveryCount={}, error={}",
                    record.getId().getValue(),
                    deliveryCount,
                    exception.getMessage()
            );
        }
    }

    private IssueResult persist(MapRecord<String, String, String> record) {
        Long eventId = eventId(record);
        String userId = userId(record);

        IssueResult result = persistenceService.persist(eventId, userId);
        log.info(
                "Persisted ticket issue. streamId={}, eventId={}, userId={}, status={}",
                record.getId().getValue(),
                eventId,
                userId,
                result.status()
        );
        return result;
    }

    private boolean isAckable(IssueStatus status) {
        return status == IssueStatus.SUCCESS || status == IssueStatus.DUPLICATED;
    }

    private void acknowledge(StreamOperations<String, String, String> streamOperations, MapRecord<String, String, String> record) {
        streamOperations.acknowledge(streamKey, groupName, record.getId().getValue());
    }

    private void sendToFailureStream(
            StreamOperations<String, String, String> streamOperations,
            MapRecord<String, String, String> record,
            String reason,
            String detail
    ) {
        Map<String, String> value = record.getValue();
        Map<String, String> failureRecord = new HashMap<>();
        failureRecord.put("originalStreamKey", streamKey);
        failureRecord.put("originalStreamId", record.getId().getValue());
        failureRecord.put("eventId", value.getOrDefault("eventId", ""));
        failureRecord.put("userId", value.getOrDefault("userId", ""));
        failureRecord.put("reason", reason);
        failureRecord.put("detail", detail == null ? "" : detail);
        failureRecord.put("failedAt", Instant.now().toString());

        streamOperations.add(failureStreamKey, failureRecord);
        log.error(
                "Moved ticket issue message to failure stream. streamId={}, failureStreamKey={}, reason={}, detail={}",
                record.getId().getValue(),
                failureStreamKey,
                reason,
                detail
        );
    }

    private Long eventId(MapRecord<String, String, String> record) {
        String eventId = record.getValue().get("eventId");
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        return Long.valueOf(eventId);
    }

    private String userId(MapRecord<String, String, String> record) {
        String userId = record.getValue().get("userId");
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId;
    }

    private boolean hasMessage(Throwable exception, String keyword) {
        Throwable current = exception;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(keyword)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
