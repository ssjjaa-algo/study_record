package perfomance.test.queue.waiting.createwaitingqueue.controller;

import perfomance.test.queue.waiting.createwaitingqueue.dto.response.WaitingEventResponse;
import perfomance.test.queue.waiting.createwaitingqueue.service.WaitingEventService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/waiting-events/{eventId}")
public class WaitingEventController {

    private final WaitingEventService service;

    public WaitingEventController(WaitingEventService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<WaitingEventResponse>> create(@PathVariable String eventId) {
        return service.create(eventId).map(WaitingEventResponse::from).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<WaitingEventResponse>> find(@PathVariable String eventId) {
        return service.find(eventId).map(WaitingEventResponse::from).map(ResponseEntity::ok);
    }

    @PostMapping("/drain")
    public Mono<ResponseEntity<WaitingEventResponse>> drain(@PathVariable String eventId) {
        return service.drain(eventId).map(WaitingEventResponse::from).map(ResponseEntity::ok);
    }

    @PostMapping("/close")
    public Mono<ResponseEntity<WaitingEventResponse>> close(@PathVariable String eventId) {
        return service.close(eventId).map(WaitingEventResponse::from).map(ResponseEntity::ok);
    }
}
