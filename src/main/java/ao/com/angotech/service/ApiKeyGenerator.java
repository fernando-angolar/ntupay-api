package ao.com.angotech.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ApiKeyGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generate(String prefix) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return prefix + "_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
