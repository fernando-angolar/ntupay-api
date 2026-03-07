package ao.com.angotech.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ActivationTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }
}
