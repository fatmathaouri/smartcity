package tn.esprit.spring.smartcity.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.citizen.Citizen;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final CitizenRepository citizenRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Notification> notifications = new ArrayList<>();

        citizenRepository.findByUserId(user.getId()).ifPresent(citizen ->
                notifications.addAll(notificationService.getNotificationsByCitizenId(citizen.getId())));

        notifications.addAll(notificationService.getNotificationsByUserId(user.getId()));

        notifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnread(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Notification> notifications = new ArrayList<>();

        citizenRepository.findByUserId(user.getId()).ifPresent(citizen ->
                notifications.addAll(notificationService.getUnreadNotifications(citizen.getId())));

        notifications.addAll(notificationService.getUnreadNotificationsByUserId(user.getId()));

        notifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        long count = notificationService.getUnreadCountByUserId(user.getId());

        var citizenCount = citizenRepository.findByUserId(user.getId())
                .map(citizen -> notificationService.getUnreadCount(citizen.getId()))
                .orElse(0L);

        return ResponseEntity.ok(count + citizenCount);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);
        citizenRepository.findByUserId(user.getId()).ifPresent(citizen ->
                notificationService.markAllAsRead(citizen.getId()));
        notificationService.markAllAsReadForUser(user.getId());
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
