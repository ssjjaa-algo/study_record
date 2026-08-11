package perfomance.test.queue.waiting.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import perfomance.test.queue.waiting.domain.WaitingQueueState;
import perfomance.test.queue.waiting.domain.WaitingQueueStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WaitingQueueStatusResponse(
        WaitingQueueState status,
        Long sequence,
        Long position,
        Integer nextPollAfterSeconds,
        String admissionToken
) {
    public static WaitingQueueStatusResponse from(WaitingQueueStatus status) {
        boolean waiting = status.status() == WaitingQueueState.WAITING;
        return new WaitingQueueStatusResponse(
                status.status(),
                waiting ? status.sequence() : null,
                waiting ? status.position() : null,
                waiting ? status.nextPollAfterSeconds() : null,
                status.admissionToken()
        );
    }
}
