package ao.com.angotech.repository;

import ao.com.angotech.entity.PasswordRecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, UUID> {
    Optional<PasswordRecoveryToken> findByToken(String token);

    Optional<PasswordRecoveryToken> findTopByUserIdOrderByRequestedAtDesc(UUID userId);

}
