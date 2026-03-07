package ao.com.angotech.service;

import ao.com.angotech.common.BusinessException;
import ao.com.angotech.dtos.UserRegistrationRequest;
import ao.com.angotech.dtos.UserRegistrationResponse;
import ao.com.angotech.entity.User;
import ao.com.angotech.enums.UserStatus;
import ao.com.angotech.mapper.UserMapper;
import ao.com.angotech.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final ApiKeyGenerator apiKeyGenerator;
    private final ActivationTokenGenerator activationTokenGenerator;
    private final EmailService emailService;

    public RegisterUserService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               PasswordPolicyValidator passwordPolicyValidator,
                               ApiKeyGenerator apiKeyGenerator,
                               ActivationTokenGenerator activationTokenGenerator,
                               EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.apiKeyGenerator = apiKeyGenerator;
        this.activationTokenGenerator = activationTokenGenerator;
        this.emailService = emailService;
    }

    @Transactional
    public UserRegistrationResponse execute(UserRegistrationRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedPhone = request.phone().trim();

        validateBusinessRules(request, normalizedEmail, normalizedPhone);

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAccountType(request.accountType());
        user.setStatus(UserStatus.PENDING);
        user.setTestApiKey(apiKeyGenerator.generate("ntp_test"));
        user.setProdApiKey(apiKeyGenerator.generate("ntp_prod"));
        user.setActivationToken(activationTokenGenerator.generate());
        user.setEmailVerified(false);


        User saved = userRepository.save(user);
        emailService.sendActivationEmail(saved);
        log.info("User created with status PEDING: id={} email={} phone={}", saved.getId(), saved.getEmail(), saved.getPhone());

        return UserMapper.toUserRegistrationResponse(saved);
    }

    private void validateBusinessRules(UserRegistrationRequest request, String normalizeEmail, String normalizedPhone) {

        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("Senha e confirmação devem ser iguais", HttpStatus.BAD_REQUEST);
        }

        if (!passwordPolicyValidator.isStrong(request.password())) {
            throw new BusinessException(
                    "Senha deve conter no mínimo 8 caracteres, incluindo maiúsculas, minúsculas, números e caracteres " +
                            "especiais",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (userRepository.existsByEmailIgnoreCase(normalizeEmail) ) {
            throw new BusinessException(
                    "Este email já está cadastrado. Faça login ou recupere sua senha",
                    HttpStatus.CONFLICT
            );
        }

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new BusinessException("Este telefone já está cadastrado.", HttpStatus.CONFLICT);
        }
    }
}
