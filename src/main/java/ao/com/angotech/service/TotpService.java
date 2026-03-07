package ao.com.angotech.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Base64;

@Service
public class TotpService {

    public boolean validateCode(String base64Secret, String code) {
        if (base64Secret == null || base64Secret.isBlank() || code == null || !code.matches("\\d{6}")) {
            return false;
        }

        long currentStep = Instant.now().getEpochSecond() / 30;
        for (int i = -1; i <= 1; i++) {
            if (generateCode(base64Secret, currentStep + i).equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCode(String base64Secret, long timeStep) {
        try {
            byte[] secret = Base64.getDecoder().decode(base64Secret);
            byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception ex) {
            return "";
        }
    }
}
