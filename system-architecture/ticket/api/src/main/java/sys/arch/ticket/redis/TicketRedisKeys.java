package sys.arch.ticket.redis;

public final class TicketRedisKeys {

    public static final String ISSUE_STREAM = "ticket:issue:stream";
    public static final String CONSUMER_LAST_ID = "ticket:consumer:last-id";

    private TicketRedisKeys() {
    }

    public static String stock(Long eventId) {
        return "ticket:event:" + eventId + ":stock";
    }

    public static String issuedUsers(Long eventId) {
        return "ticket:event:" + eventId + ":issued-users";
    }

    public static String soldOut(Long eventId) {
        return "ticket:event:" + eventId + ":sold-out";
    }
}
