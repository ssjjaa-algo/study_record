package sys.arch.ticket.domain;

public record IssueResult(IssueStatus status, Long eventId, String userId) {

    public static IssueResult success(Long eventId, String userId) {
        return new IssueResult(IssueStatus.SUCCESS, eventId, userId);
    }

    public static IssueResult soldOut(Long eventId, String userId) {
        return new IssueResult(IssueStatus.SOLD_OUT, eventId, userId);
    }

    public static IssueResult duplicated(Long eventId, String userId) {
        return new IssueResult(IssueStatus.DUPLICATED, eventId, userId);
    }

    public static IssueResult notReady(Long eventId, String userId) {
        return new IssueResult(IssueStatus.NOT_READY, eventId, userId);
    }
}
