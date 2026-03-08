package ao.com.angotech.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordRecoveryDelayService {

    public void delay() {
        try {
            Thread.sleep(250L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}
