package sys.arch.ticket.api.dto;

import sys.arch.ticket.domain.IssueResult;
import sys.arch.ticket.domain.IssueStatus;

public record IssueTicketResponse(
        IssueStatus status,
        Long eventId,
        String userId,
        String message
) {

    public static IssueTicketResponse from(IssueResult result) {
        return new IssueTicketResponse(
                result.status(),
                result.eventId(),
                result.userId(),
                message(result.status())
        );
    }

    private static String message(IssueStatus status) {
        return switch (status) {
            case SUCCESS -> "Ticket issue request accepted.";
            case SOLD_OUT -> "Ticket event is sold out.";
            case DUPLICATED -> "User already issued a ticket.";
            case NOT_READY -> "Ticket stock is not initialized.";
        };
    }
}
