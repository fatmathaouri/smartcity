package tn.esprit.spring.smartcity.sla;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deactivateExpiredTempPasswords() {
        List<User> users = userRepository.findByMustChangePasswordTrueAndTempPasswordExpiresAtBefore(
                java.time.LocalDateTime.now());
        for (User user : users) {
            user.setEnabled(false);
            userRepository.save(user);
            log.info("Deactivated user {} due to expired temp password", user.getEmail());
        }
    }
}
