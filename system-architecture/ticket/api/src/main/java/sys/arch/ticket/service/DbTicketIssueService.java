package sys.arch.ticket.service;

import org.springframework.stereotype.Service;
import sys.arch.ticket.domain.IssueResult;

@Service
public class DbTicketIssueService {

    private final TicketIssuePersistenceService persistenceService;

    public DbTicketIssueService(TicketIssuePersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public IssueResult issue(Long eventId, String userId) {
        return persistenceService.persist(eventId, userId);
    }
}
