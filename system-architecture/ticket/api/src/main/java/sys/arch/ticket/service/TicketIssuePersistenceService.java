package sys.arch.ticket.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import sys.arch.ticket.domain.IssueResult;
import sys.arch.ticket.domain.TicketIssue;
import sys.arch.ticket.repository.TicketEventRepository;
import sys.arch.ticket.repository.TicketIssueRepository;

@Service
public class TicketIssuePersistenceService {

    private final TransactionTemplate transactionTemplate;
    private final TicketEventRepository ticketEventRepository;
    private final TicketIssueRepository ticketIssueRepository;

    public TicketIssuePersistenceService(
            TransactionTemplate transactionTemplate,
            TicketEventRepository ticketEventRepository,
            TicketIssueRepository ticketIssueRepository
    ) {
        this.transactionTemplate = transactionTemplate;
        this.ticketEventRepository = ticketEventRepository;
        this.ticketIssueRepository = ticketIssueRepository;
    }

    public IssueResult persist(Long eventId, String userId) {
        return transactionTemplate.execute(status -> {
            try {
                ticketIssueRepository.saveAndFlush(TicketIssue.issue(eventId, userId));
            } catch (DataIntegrityViolationException exception) {
                status.setRollbackOnly();
                return IssueResult.duplicated(eventId, userId);
            }

            int updatedRows = ticketEventRepository.decreaseRemainingQuantity(eventId);
            if (updatedRows == 0) {
                status.setRollbackOnly();
                return IssueResult.soldOut(eventId, userId);
            }

            return IssueResult.success(eventId, userId);
        });
    }
}
