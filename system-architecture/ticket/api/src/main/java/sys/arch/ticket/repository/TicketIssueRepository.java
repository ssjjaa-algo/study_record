package sys.arch.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sys.arch.ticket.domain.TicketIssue;

public interface TicketIssueRepository extends JpaRepository<TicketIssue, Long> {

    boolean existsByEventIdAndUserId(Long eventId, String userId);

    long countByEventId(Long eventId);
}
