package ao.com.angotech.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordPolicyValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_+=-])[A-Za-z\\d@$!%*?&.#_+=-]{8,}$");

    public boolean isStrong(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
