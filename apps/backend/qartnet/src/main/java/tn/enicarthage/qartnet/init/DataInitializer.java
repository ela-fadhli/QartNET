package tn.enicarthage.qartnet.init;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createAdminIfAbsent();
    }

    private void createAdminIfAbsent() {
        String adminEmail = "admin@enicarthage.rnu.tn";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists — skipping seed.");
            return;
        }

        User admin = User.builder()
                .publicId(UUID.randomUUID())
                .username("admin")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@QartNET2025"))
                .firstName("Admin")
                .lastName("QartNET")
                .roles(Set.of(Role.ADMIN))
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        userRepository.save(admin);
        log.warn("Default admin created: {} — CHANGE THIS PASSWORD IMMEDIATELY IN PRODUCTION!", adminEmail);
    }
}
