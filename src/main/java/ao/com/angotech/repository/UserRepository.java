package ao.com.angotech.repository;

import ao.com.angotech.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmailIgnoreCase(String email);
}
