package perfomance.test.queue.waiting.dto.response;

import perfomance.test.queue.waiting.domain.WaitingQueueProgress;

public record WaitingQueueProgressResponse(
        String eventId,
        long lastAdmittedSequence,
        long activeCount,
        int nextPollAfterSeconds
) {
    public static WaitingQueueProgressResponse from(WaitingQueueProgress progress) {
        return new WaitingQueueProgressResponse(
                progress.eventId(),
                progress.lastAdmittedSequence(),
                progress.activeCount(),
                progress.nextPollAfterSeconds()
        );
    }
}
