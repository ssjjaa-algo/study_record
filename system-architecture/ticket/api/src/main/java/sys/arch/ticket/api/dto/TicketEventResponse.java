package sys.arch.ticket.api.dto;

import sys.arch.ticket.domain.TicketEvent;

import java.time.Instant;

public record TicketEventResponse(
        Long id,
        String name,
        int totalQuantity,
        int remainingQuantity,
        Instant createdAt
) {

    public static TicketEventResponse from(TicketEvent event) {
        return new TicketEventResponse(
                event.getId(),
                event.getName(),
                event.getTotalQuantity(),
                event.getRemainingQuantity(),
                event.getCreatedAt()
        );
    }
}
