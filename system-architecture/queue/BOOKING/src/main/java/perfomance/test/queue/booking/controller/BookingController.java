package perfomance.test.queue.booking.controller;

import perfomance.test.queue.booking.dto.request.BookingRequest;
import perfomance.test.queue.booking.dto.response.BookingResponse;
import perfomance.test.queue.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = {"http://localhost:8081", "http://127.0.0.1:8081"})
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> start(@ModelAttribute BookingRequest request) {
        return ResponseEntity.ok(BookingResponse.from(
                service.start(request.authority(), request.admissionToken())
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<BookingResponse> status(
            @RequestParam String eventId,
            @RequestParam String userId,
            @RequestParam long sequence,
            @RequestParam String authority
    ) {
        return ResponseEntity.ok(BookingResponse.from(
                service.find(eventId, userId, sequence, authority)
        ));
    }
}
