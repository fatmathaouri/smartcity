package tn.esprit.spring.smartcity.gamification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/score")
    public ResponseEntity<Map<String, Object>> getMyScore(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(gamificationService.getUserScore(user.getId()));
    }

    @GetMapping("/score/{userId}")
    public ResponseEntity<Map<String, Object>> getUserScore(@PathVariable Long userId) {
        return ResponseEntity.ok(gamificationService.getUserScore(userId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(gamificationService.getLeaderboard(limit));
    }

    @PostMapping("/init")
    public ResponseEntity<String> initBadges() {
        gamificationService.initBadges();
        return ResponseEntity.ok("Badges initialized");
    }
}
