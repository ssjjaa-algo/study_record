package perfomance.test.queue.waiting.createwaitingqueue.dto.response;

import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingEvent;
import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingEventState;

public record WaitingEventResponse(
        String eventId,
        WaitingEventState state,
        int maxWaitingUsers,
        int maxActiveUsers,
        int admissionRatePerSecond,
        long waitingInactivityTimeoutSeconds,
        long activeInactivityTimeoutSeconds,
        long maxActiveDurationSeconds,
        long waitingCount,
        long activeCount
) {
    public static WaitingEventResponse from(WaitingEvent event) {
        return new WaitingEventResponse(
                event.eventId(),
                event.state(),
                event.policy().maxWaitingUsers(),
                event.policy().maxActiveUsers(),
                event.policy().admissionRatePerSecond(),
                event.policy().waitingInactivityTimeoutSeconds(),
                event.policy().activeInactivityTimeoutSeconds(),
                event.policy().maxActiveDurationSeconds(),
                event.waitingCount(),
                event.activeCount()
        );
    }
}
