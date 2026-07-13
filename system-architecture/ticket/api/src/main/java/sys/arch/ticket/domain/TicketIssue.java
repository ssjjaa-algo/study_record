package sys.arch.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "ticket_issue",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_issue_event_user",
                        columnNames = {"event_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ticket_issue_event", columnList = "event_id")
        }
)
public class TicketIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    protected TicketIssue() {
    }

    private TicketIssue(Long eventId, String userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.issuedAt = Instant.now();
    }

    public static TicketIssue issue(Long eventId, String userId) {
        return new TicketIssue(eventId, userId);
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
