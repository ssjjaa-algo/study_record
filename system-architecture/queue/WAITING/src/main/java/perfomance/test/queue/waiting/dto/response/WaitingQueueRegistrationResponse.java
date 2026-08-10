package perfomance.test.queue.waiting.dto.response;

import perfomance.test.queue.waiting.domain.WaitingQueueRegistration;

public record WaitingQueueRegistrationResponse(
        WaitingQueueStatusResponse queue,
        boolean newlyRegistered
) {
    public static WaitingQueueRegistrationResponse from(WaitingQueueRegistration registration) {
        return new WaitingQueueRegistrationResponse(
                WaitingQueueStatusResponse.from(registration.queue()),
                registration.newlyRegistered()
        );
    }
}
