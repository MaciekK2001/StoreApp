package produkcja.meble.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import produkcja.meble.app.entity.AccountRole;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.repository.UserAccountRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin1.username:}")
    private String admin1Username;

    @Value("${app.init.admin1.email:}")
    private String admin1Email;

    @Value("${app.init.admin1.password:}")
    private String admin1Password;

    @Value("${app.init.admin2.username:}")
    private String admin2Username;

    @Value("${app.init.admin2.email:}")
    private String admin2Email;

    @Value("${app.init.admin2.password:}")
    private String admin2Password;

    @Override
    public void run(ApplicationArguments args) {
        initAdmin(admin1Username, admin1Email, admin1Password, "admin1");
        initAdmin(admin2Username, admin2Email, admin2Password, "admin2");
    }

    private void initAdmin(String username, String email, String password, String adminLabel) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return;
        }

        String trimmedUsername = username.trim();
        String trimmedEmail = StringUtils.hasText(email) ? email.trim() : trimmedUsername + "@system.local";

        if (userAccountRepository.existsByUsername(trimmedUsername)) {
            log.info("Initial account {} ('{}') already exists, skipping creation.", adminLabel, trimmedUsername);
            return;
        }

        if (userAccountRepository.existsByEmail(trimmedEmail)) {
            log.warn("Email '{}' for initial account {} is already in use by another account, skipping creation.", trimmedEmail, adminLabel);
            return;
        }

        UserAccount adminAccount = UserAccount.builder()
                .username(trimmedUsername)
                .email(trimmedEmail)
                .password(passwordEncoder.encode(password.trim()))
                .role(AccountRole.ADMIN)
                .build();

        userAccountRepository.save(adminAccount);
        log.info("Initial admin account {} ('{}') created successfully.", adminLabel, trimmedUsername);
    }
}
