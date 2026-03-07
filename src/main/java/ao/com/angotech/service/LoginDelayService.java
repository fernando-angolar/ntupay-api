package ao.com.angotech.service;

import org.springframework.stereotype.Service;

@Service
public class LoginDelayService {

    public void delayAfterFailedAttempt() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
