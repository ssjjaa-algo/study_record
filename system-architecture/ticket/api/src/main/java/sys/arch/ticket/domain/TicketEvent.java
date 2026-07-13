package sys.arch.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ticket_event")
public class TicketEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int remainingQuantity;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TicketEvent() {
    }

    private TicketEvent(String name, int totalQuantity) {
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.createdAt = Instant.now();
    }

    public static TicketEvent create(String name, int totalQuantity) {
        if (totalQuantity <= 0) {
            throw new IllegalArgumentException("totalQuantity must be positive");
        }
        return new TicketEvent(name, totalQuantity);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
