package perfomance.test.queue.waiting.createwaitingqueue.domain;

public record WaitingEvent(
        String eventId,
        WaitingEventState state,
        WaitingQueuePolicy policy,
        long waitingCount,
        long activeCount
) {
}
