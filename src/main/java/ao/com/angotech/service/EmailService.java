package ao.com.angotech.service;

import ao.com.angotech.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${ntupay.activation.base-url:http://localhost:5173/activate}")
    private String activationBaseUrl;

    public void sendActivationEmail(User user) {
        String activationLink = activationBaseUrl + "?token=" + user.getActivationToken();
        log.info("Seding activation email to {} with link {}", user.getEmail(), activationLink);
    }
}
