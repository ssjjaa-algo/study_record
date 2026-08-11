package perfomance.test.queue.booking.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import perfomance.test.queue.booking.domain.AdmissionTokenClaims;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingTokenService {

    public AdmissionTokenClaims decode(String admissionToken) {
        if (admissionToken == null || admissionToken.isBlank()) {
            throw invalidToken();
        }
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(admissionToken),
                    StandardCharsets.UTF_8
            );
            String[] values = decoded.split("\\|", 2);
            if (values.length != 2 || values[0].isBlank() || values[1].isBlank()) {
                throw invalidToken();
            }
            return new AdmissionTokenClaims(values[0], values[1]);
        } catch (IllegalArgumentException exception) {
            throw invalidToken();
        }
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Admission token is invalid.");
    }
}
