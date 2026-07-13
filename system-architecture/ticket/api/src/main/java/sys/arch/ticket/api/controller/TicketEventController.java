package sys.arch.ticket.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sys.arch.ticket.api.dto.CreateTicketEventRequest;
import sys.arch.ticket.api.dto.IssueTicketRequest;
import sys.arch.ticket.api.dto.IssueTicketResponse;
import sys.arch.ticket.api.dto.TicketEventResponse;
import sys.arch.ticket.domain.IssueResult;
import sys.arch.ticket.domain.IssueStatus;
import sys.arch.ticket.domain.TicketEvent;
import sys.arch.ticket.redis.RedisTicketIssueService;
import sys.arch.ticket.service.TicketEventService;

@RestController
@RequestMapping("/api/ticket-events")
public class TicketEventController {

    private final TicketEventService ticketEventService;
    private final RedisTicketIssueService redisTicketIssueService;

    public TicketEventController(
            TicketEventService ticketEventService,
            RedisTicketIssueService redisTicketIssueService
    ) {
        this.ticketEventService = ticketEventService;
        this.redisTicketIssueService = redisTicketIssueService;
    }

    @PostMapping
    public ResponseEntity<TicketEventResponse> create(@Valid @RequestBody CreateTicketEventRequest request) {
        TicketEvent event = ticketEventService.create(request.name(), request.totalQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketEventResponse.from(event));
    }

    @GetMapping("/{eventId}")
    public TicketEventResponse get(@PathVariable Long eventId) {
        return TicketEventResponse.from(ticketEventService.get(eventId));
    }

    @PostMapping("/{eventId}/issues")
    public ResponseEntity<IssueTicketResponse> issue(
            @PathVariable Long eventId,
            @Valid @RequestBody IssueTicketRequest request
    ) {
        IssueResult result = redisTicketIssueService.issue(eventId, request.userId());
        return ResponseEntity.status(httpStatus(result.status())).body(IssueTicketResponse.from(result));
    }

    private HttpStatus httpStatus(IssueStatus status) {
        return switch (status) {
            case SUCCESS -> HttpStatus.ACCEPTED;
            case DUPLICATED -> HttpStatus.CONFLICT;
            case SOLD_OUT -> HttpStatus.GONE;
            case NOT_READY -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
}
