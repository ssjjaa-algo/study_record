package sys.arch.ticket.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTicketEventRequest(
        @NotBlank String name,
        @Min(1) int totalQuantity
) {
}
