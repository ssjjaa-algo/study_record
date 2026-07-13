package sys.arch.ticket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sys.arch.ticket.domain.TicketEvent;
import sys.arch.ticket.redis.RedisTicketStockInitializer;
import sys.arch.ticket.repository.TicketEventRepository;

@Service
public class TicketEventService {

    private final TicketEventRepository ticketEventRepository;
    private final RedisTicketStockInitializer stockInitializer;

    public TicketEventService(
            TicketEventRepository ticketEventRepository,
            RedisTicketStockInitializer stockInitializer
    ) {
        this.ticketEventRepository = ticketEventRepository;
        this.stockInitializer = stockInitializer;
    }

    @Transactional
    public TicketEvent create(String name, int totalQuantity) {
        TicketEvent event = ticketEventRepository.save(TicketEvent.create(name, totalQuantity));
        stockInitializer.initialize(event.getId(), totalQuantity);
        return event;
    }

    @Transactional(readOnly = true)
    public TicketEvent get(Long eventId) {
        return ticketEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket event not found: " + eventId));
    }
}
