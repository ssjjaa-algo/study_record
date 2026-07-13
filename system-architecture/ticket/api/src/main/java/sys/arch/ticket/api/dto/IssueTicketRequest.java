package sys.arch.ticket.api.dto;

import jakarta.validation.constraints.NotBlank;

public record IssueTicketRequest(@NotBlank String userId) {
}
