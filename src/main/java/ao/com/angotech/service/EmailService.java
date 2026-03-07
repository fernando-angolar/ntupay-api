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

    @Value("${ntupay.unlock.base-url:http://localhost:5173/unlock}")
    private String unlockBaseUrl;

    public void sendActivationEmail(User user) {
        String activationLink = activationBaseUrl + "?token=" + user.getActivationToken();
        log.info("Sending activation email to {} with link {}", user.getEmail(), activationLink);
    }

    public void sendAccountBlockedEmail(User user) {
        String unlockLink = unlockBaseUrl + "?user=" + user.getId();
        log.warn("Sending account blocked notification to {} and unlock link {}", user.getEmail(), unlockLink);
    }

}
