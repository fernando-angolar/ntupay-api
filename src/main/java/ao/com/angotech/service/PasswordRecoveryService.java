package ao.com.angotech.service;

import ao.com.angotech.common.BusinessException;
import ao.com.angotech.dtos.PasswordRecoveryRequest;
import ao.com.angotech.dtos.ResetPasswordRequest;
import ao.com.angotech.entity.PasswordRecoveryToken;
import ao.com.angotech.entity.User;
import ao.com.angotech.repository.LoginSessionRepository;
import ao.com.angotech.repository.PasswordRecoveryTokenRepository;
import ao.com.angotech.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);
    private static final String GENERIC_MESSAGE = "Se o email estiver cadastrado, você receberá instruções de recuperação";

    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
    private final PasswordRecoveryDelayService passwordRecoveryDelayService;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionRepository loginSessionRepository;
    private final EmailService emailService;

    public PasswordRecoveryService(UserRepository userRepository,
                                   PasswordRecoveryTokenRepository passwordRecoveryTokenRepository,
                                   PasswordRecoveryDelayService passwordRecoveryDelayService,
                                   PasswordPolicyValidator passwordPolicyValidator,
                                   PasswordEncoder passwordEncoder,
                                   LoginSessionRepository loginSessionRepository,
                                   EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordRecoveryTokenRepository = passwordRecoveryTokenRepository;
        this.passwordRecoveryDelayService = passwordRecoveryDelayService;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.passwordEncoder = passwordEncoder;
        this.loginSessionRepository = loginSessionRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String requestRecovery(PasswordRecoveryRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(normalizedEmail);

        if (maybeUser.isEmpty()) {
            passwordRecoveryDelayService.delay();
            log.warn("Password recovery requested for non-existing email={}", normalizedEmail);
            return GENERIC_MESSAGE;
        }

        User user = maybeUser.get();
        if (!user.isEmailVerified()) {
            passwordRecoveryDelayService.delay();
            log.warn("Password recovery requested for non-verified email userId={}", user.getId());
            return GENERIC_MESSAGE;
        }

        validateRateLimit(user.getId());

        PasswordRecoveryToken recoveryToken = new PasswordRecoveryToken();
        recoveryToken.setUser(user);
        recoveryToken.setToken(UUID.randomUUID().toString());
        recoveryToken.setExpiresAt(OffsetDateTime.now().plusHours(1));
        passwordRecoveryTokenRepository.save(recoveryToken);

        emailService.sendPasswordRecoveryEmail(user, recoveryToken.getToken());
        log.info("Password recovery token generated: userId={} tokenId={}", user.getId(), recoveryToken.getId());

        return GENERIC_MESSAGE;
    }

    @Transactional(readOnly = true)
    public void validateToken(String token) {
        findValidToken(token);
    }

    @Transactional
    public void resetPassword(String token, ResetPasswordRequest request) {
        PasswordRecoveryToken recoveryToken = findValidToken(token);

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("Senha e confirmação devem ser iguais", HttpStatus.BAD_REQUEST);
        }

        if (!passwordPolicyValidator.isStrong(request.newPassword())) {
            throw new BusinessException(
                    "Senha deve conter no mínimo 8 caracteres, incluindo maiúsculas, minúsculas, números e caracteres especiais",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = recoveryToken.getUser();
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException("Nova senha deve ser diferente da senha atual", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        recoveryToken.setUsedAt(OffsetDateTime.now());
        passwordRecoveryTokenRepository.save(recoveryToken);

        loginSessionRepository.deleteByUserId(user.getId());
        emailService.sendPasswordChangedConfirmationEmail(user);

        log.info("Password successfully reset: userId={}", user.getId());
    }

    private void validateRateLimit(UUID userId) {
        Optional<PasswordRecoveryToken> latestToken = passwordRecoveryTokenRepository
                .findTopByUserIdOrderByRequestedAtDesc(userId);

        if (latestToken.isPresent() && latestToken.get().getRequestedAt().isAfter(OffsetDateTime.now().minusMinutes(5))) {
            throw new BusinessException("Aguarde 5 minutos antes de solicitar novo link", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private PasswordRecoveryToken findValidToken(String token) {
        PasswordRecoveryToken recoveryToken = passwordRecoveryTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                        "Link de recuperação inválido ou expirado. Solicite um novo link.",
                        HttpStatus.BAD_REQUEST
                ));

        OffsetDateTime now = OffsetDateTime.now();
        if (recoveryToken.getUsedAt() != null || recoveryToken.getExpiresAt().isBefore(now)) {
            throw new BusinessException(
                    "Link de recuperação inválido ou expirado. Solicite um novo link.",
                    HttpStatus.BAD_REQUEST
            );
        }

        return recoveryToken;
    }
}
