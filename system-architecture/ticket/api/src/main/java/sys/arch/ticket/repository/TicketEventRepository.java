package sys.arch.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sys.arch.ticket.domain.TicketEvent;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
            update TicketEvent event
               set event.remainingQuantity = event.remainingQuantity - 1
             where event.id = :eventId
               and event.remainingQuantity > 0
            """)
    int decreaseRemainingQuantity(@Param("eventId") Long eventId);
}
