package perfomance.test.queue.waiting.repository;

public final class WaitingQueueRedisKeys {

    private static final String KEY_PREFIX = "queue:";
    public static final String OPEN_EVENTS = "queue:open-events";

    private WaitingQueueRedisKeys() {
    }

    public static String meta(String eventId) {
        return eventKey(eventId, "meta");
    }

    public static String waiting(String eventId) {
        return eventKey(eventId, "waiting");
    }

    public static String active(String eventId) {
        return eventKey(eventId, "active");
    }

    public static String state(String eventId) {
        return eventKey(eventId, "state");
    }

    public static String userSequence(String eventId) {
        return eventKey(eventId, "user-sequence");
    }

    public static String activeStarted(String eventId) {
        return eventKey(eventId, "active-started");
    }

    public static String waitingLastSeen(String eventId) {
        return eventKey(eventId, "waiting-last-seen");
    }

    public static String sequence(String eventId) {
        return eventKey(eventId, "sequence");
    }

    public static String admissionBudget(String eventId) {
        return eventKey(eventId, "admission-budget");
    }

    public static String workerLease(String eventId, String workerName) {
        return eventKey(eventId, "worker-lease:" + workerName);
    }

    private static String eventKey(String eventId, String suffix) {
        return KEY_PREFIX + "{" + eventId + "}:" + suffix;
    }
}
