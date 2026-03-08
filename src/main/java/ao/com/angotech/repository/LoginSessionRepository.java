package ao.com.angotech.repository;

import ao.com.angotech.entity.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoginSessionRepository extends JpaRepository<LoginSession, UUID> {

    void deleteByUserId(UUID userId);
}
