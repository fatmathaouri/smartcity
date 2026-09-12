package tn.esprit.spring.smartcity.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);
    List<Notification> findByCitizenIdAndReadFalse(Long citizenId);
    long countByCitizenIdAndReadFalse(Long citizenId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndReadFalse(Long userId);
    long countByUserIdAndReadFalse(Long userId);
}
