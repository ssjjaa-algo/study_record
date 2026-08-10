package perfomance.test.queue.waiting.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class AdmissionTokenService {

    public String issue(String eventId, String userId, long sequence, Instant expiresAt) {
        String claims = String.join(
                "|",
                eventId,
                userId,
                Long.toString(sequence),
                Long.toString(expiresAt.toEpochMilli())
        );
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(claims.getBytes(StandardCharsets.UTF_8));
    }
}
