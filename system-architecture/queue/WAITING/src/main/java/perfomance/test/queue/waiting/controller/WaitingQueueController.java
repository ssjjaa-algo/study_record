package perfomance.test.queue.waiting.controller;

import perfomance.test.queue.waiting.dto.request.QueueReleaseRequest;
import perfomance.test.queue.waiting.dto.request.QueueUserRequest;
import perfomance.test.queue.waiting.dto.response.WaitingQueueRegistrationResponse;
import perfomance.test.queue.waiting.dto.response.WaitingQueueProgressResponse;
import perfomance.test.queue.waiting.dto.response.WaitingQueueStatusResponse;
import perfomance.test.queue.waiting.service.WaitingQueueService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/waiting-events/{eventId}/queue")
public class WaitingQueueController {

    private final WaitingQueueService service;

    public WaitingQueueController(WaitingQueueService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<WaitingQueueRegistrationResponse>> register(
            @PathVariable String eventId,
            @ModelAttribute QueueUserRequest request
    ) {
        return service.register(eventId, request.userId(), request.authority())
                .map(WaitingQueueRegistrationResponse::from)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/status")
    public Mono<ResponseEntity<WaitingQueueStatusResponse>> status(
            @PathVariable String eventId,
            @ModelAttribute QueueUserRequest request
    ) {
        return service.findStatus(eventId, request.userId(), request.authority())
                .map(WaitingQueueStatusResponse::from)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/progress")
    public Mono<ResponseEntity<WaitingQueueProgressResponse>> progress(
            @PathVariable String eventId,
            @RequestParam long sequence
    ) {
        return service.findProgress(eventId, sequence)
                .map(WaitingQueueProgressResponse::from)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/release")
    public Mono<ResponseEntity<WaitingQueueStatusResponse>> release(
            @PathVariable String eventId,
            @ModelAttribute QueueReleaseRequest request
    ) {
        return service.release(eventId, request.userId(), request.sequence(), request.authority())
                .map(WaitingQueueStatusResponse::from)
                .map(ResponseEntity::ok);
    }
}
