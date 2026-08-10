package perfomance.test.queue.booking.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
            String[] values = decoded.split("\\|", 4);
            if (values.length != 4) {
                throw invalidToken();
            }
            AdmissionTokenClaims claims = new AdmissionTokenClaims(
                    values[0],
                    values[1],
                    Long.parseLong(values[2]),
                    Instant.ofEpochMilli(Long.parseLong(values[3]))
            );
            if (claims.expiresAt().isBefore(Instant.now())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admission token has expired.");
            }
            return claims;
        } catch (IllegalArgumentException exception) {
            throw invalidToken();
        }
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Admission token is invalid.");
    }
}
