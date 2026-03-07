package ao.com.angotech.repository;

import ao.com.angotech.entity.TwoFactorChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TwoFactorChallengeRepository extends JpaRepository<TwoFactorChallenge, UUID> {

    Optional<TwoFactorChallenge> findByChallengeToken(String challengeToken);
}
