package ao.com.angotech.service;

import ao.com.angotech.common.BusinessException;
import ao.com.angotech.dtos.LoginRequest;
import ao.com.angotech.dtos.LoginResponse;
import ao.com.angotech.entity.LoginSession;
import ao.com.angotech.entity.TwoFactorChallenge;
import ao.com.angotech.entity.User;
import ao.com.angotech.enums.UserStatus;
import ao.com.angotech.repository.LoginSessionRepository;
import ao.com.angotech.repository.TwoFactorChallengeRepository;
import ao.com.angotech.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class LoginUserService {

    private static final Logger log = LoggerFactory.getLogger(LoginUserService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UserRepository userRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final TwoFactorChallengeRepository twoFactorChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final TokenHashService tokenHashService;
    private final TotpService totpService;
    private final LoginDelayService loginDelayService;
    private final EmailService emailService;

    public LoginUserService(UserRepository userRepository,
                            LoginSessionRepository loginSessionRepository,
                            TwoFactorChallengeRepository twoFactorChallengeRepository,
                            PasswordEncoder passwordEncoder,
                            JwtTokenService jwtTokenService,
                            RefreshTokenService refreshTokenService,
                            TokenHashService tokenHashService,
                            TotpService totpService,
                            LoginDelayService loginDelayService,
                            EmailService emailService) {
        this.userRepository = userRepository;
        this.loginSessionRepository = loginSessionRepository;
        this.twoFactorChallengeRepository = twoFactorChallengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.tokenHashService = tokenHashService;
        this.totpService = totpService;
        this.loginDelayService = loginDelayService;
        this.emailService = emailService;
    }

    @Transactional
    public LoginResponse execute(LoginRequest request, String ipAddress, String userAgent) {
        String identifier = request.identifier().trim();
        Optional<User> userOptional = isEmail(identifier)
                ? userRepository.findByEmailIgnoreCase(identifier.toLowerCase())
                : userRepository.findByPhone(identifier);

        if (userOptional.isEmpty()) {
            passwordEncoder.matches(request.password(), "$2a$12$udQ4vNabW2ErNszvJr6ksOFnKQf8YJx9yccw2R5fY7O0e8w2iEAX2");
            loginDelayService.delayAfterFailedAttempt();
            log.warn("Tentativa de login para identificador não encontrado: identifier={} ip={}", identifier, ipAddress);
            throw new BusinessException("Email/telefone ou senha incorretos", HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();
        validateUserBlocked(user, ipAddress);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleInvalidCredentials(user, ipAddress);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BusinessException("Sua conta foi bloqueada por múltiplas tentativas de login. " +
                    "Verifique seu email para instruções de desbloqueio.", HttpStatus.FORBIDDEN);
        }

        if (user.isTwoFactorEnabled()) {
            return processTwoFactorFlow(request, user, ipAddress, userAgent);
        }

        return finishSuccessfulLogin(user, ipAddress, userAgent);
    }

    private LoginResponse processTwoFactorFlow(LoginRequest request, User user, String ipAddress, String userAgent) {
        if (request.twoFactorSessionToken() == null || request.twoFactorCode() == null) {
            OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
            String challengeToken = jwtTokenService.generateTwoFactorSessionToken(user, expiresAt);

            TwoFactorChallenge challenge = new TwoFactorChallenge();
            challenge.setUser(user);
            challenge.setChallengeToken(challengeToken);
            challenge.setAttempts(0);
            challenge.setExpiresAt(expiresAt);
            challenge.setVerified(false);
            twoFactorChallengeRepository.save(challenge);

            log.info("2FA solicitado para userId={}", user.getId());
            return new LoginResponse(true, challengeToken, null, null, 0, 0);
        }

        TwoFactorChallenge challenge = twoFactorChallengeRepository.findByChallengeToken(request.twoFactorSessionToken())
                .orElseThrow(() -> new BusinessException("Sessão 2FA inválida ou expirada", HttpStatus.UNAUTHORIZED));

        OffsetDateTime now = OffsetDateTime.now();
        if (challenge.getBlockedUntil() != null && challenge.getBlockedUntil().isAfter(now)) {
            throw new BusinessException("Muitas tentativas de 2FA. Tente novamente em 15 minutos.", HttpStatus.FORBIDDEN);
        }

        if (challenge.getExpiresAt().isBefore(now)) {
            throw new BusinessException("Sessão 2FA inválida ou expirada", HttpStatus.UNAUTHORIZED);
        }

        if (!totpService.validateCode(user.getTotpSecret(), request.twoFactorCode())) {
            int attempts = challenge.getAttempts() + 1;
            challenge.setAttempts(attempts);
            if (attempts >= 3) {
                challenge.setBlockedUntil(now.plusMinutes(15));
                twoFactorChallengeRepository.save(challenge);
                throw new BusinessException("Muitas tentativas de 2FA. Tente novamente em 15 minutos.", HttpStatus.FORBIDDEN);
            }
            twoFactorChallengeRepository.save(challenge);
            throw new BusinessException("Código TOTP inválido", HttpStatus.UNAUTHORIZED);
        }

        challenge.setVerified(true);
        twoFactorChallengeRepository.save(challenge);
        return finishSuccessfulLogin(user, ipAddress, userAgent);
    }

    private void validateUserBlocked(User user, String ipAddress) {
        OffsetDateTime now = OffsetDateTime.now();

        if (user.getStatus() == UserStatus.BLOCKED || (user.getBlockedUntil() != null && user.getBlockedUntil().isAfter(now))) {
            log.warn("Login bloqueado para userId={} ip={}", user.getId(), ipAddress);
            throw new BusinessException("Sua conta foi bloqueada por múltiplas tentativas de login. Verifique seu email para instruções de desbloqueio.", HttpStatus.FORBIDDEN);
        }
    }

    private void handleInvalidCredentials(User user, String ipAddress) {
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= 5) {
            user.setBlockedUntil(OffsetDateTime.now().plusMinutes(15));
            user.setStatus(UserStatus.BLOCKED);
            userRepository.save(user);
            emailService.sendAccountBlockedEmail(user);
            log.warn("Conta bloqueada por tentativas excedidas: userId={} ip={}", user.getId(), ipAddress);
            throw new BusinessException("Sua conta foi bloqueada por múltiplas tentativas de login. Verifique seu email para instruções de desbloqueio.", HttpStatus.FORBIDDEN);
        }

        userRepository.save(user);
        loginDelayService.delayAfterFailedAttempt();
        log.warn("Tentativa de login inválida: userId={} ip={} failedAttempts={}", user.getId(), ipAddress, failedAttempts);
        throw new BusinessException("Email/telefone ou senha incorretos", HttpStatus.UNAUTHORIZED);
    }

    private LoginResponse finishSuccessfulLogin(User user, String ipAddress, String userAgent) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime accessExpiresAt = now.plusHours(1);
        OffsetDateTime refreshExpiresAt = now.plusDays(30);

        String accessToken = jwtTokenService.generateAccessToken(user, accessExpiresAt);
        String refreshToken = refreshTokenService.generate();

        LoginSession loginSession = new LoginSession();
        loginSession.setUser(user);
        loginSession.setRefreshTokenHash(tokenHashService.sha256(refreshToken));
        loginSession.setIpAddress(ipAddress == null ? "unknown" : ipAddress);
        loginSession.setUserAgent((userAgent == null || userAgent.isBlank()) ? "unknown" : userAgent);
        loginSession.setExpiresAt(refreshExpiresAt);
        loginSessionRepository.save(loginSession);

        user.setFailedLoginAttempts(0);
        user.setBlockedUntil(null);
        userRepository.save(user);

        log.info("Login realizado com sucesso: userId={} ip={} userAgent={}", user.getId(), ipAddress, userAgent);

        return new LoginResponse(false, null, accessToken, refreshToken, 3600, 2592000);
    }

    private boolean isEmail(String identifier) {
        return EMAIL_PATTERN.matcher(identifier).matches();
    }

}
