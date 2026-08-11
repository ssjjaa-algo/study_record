package perfomance.test.queue.waiting.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class AdmissionTokenService {

    public String issue(String eventId, String userId) {
        String claims = String.join("|", eventId, userId);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(claims.getBytes(StandardCharsets.UTF_8));
    }
}
