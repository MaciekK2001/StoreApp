package produkcja.meble.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import produkcja.meble.app.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
