package tn.esprit.spring.smartcity.gamification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;
import tn.esprit.spring.smartcity.notification.NotificationService;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final CitizenScoreRepository citizenScoreRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final CitizenRepository citizenRepository;
    private final NotificationService notificationService;

    @PostConstruct
    public void initBadges() {
        if (badgeRepository.count() == 0) {
            badgeRepository.save(Badge.builder().name("Pionnier").description("Premier signalement").icon("star").thresholdPoints(10L).build());
            badgeRepository.save(Badge.builder().name("Analyste").description("5 commentaires").icon("chat").thresholdPoints(25L).build());
            badgeRepository.save(Badge.builder().name("Observateur").description("10 confirmations").icon("visibility").thresholdPoints(50L).build());
            badgeRepository.save(Badge.builder().name("Citoyen actif").description("10 signalements").icon("emoji_events").thresholdPoints(100L).build());
            badgeRepository.save(Badge.builder().name("Légende").description("500 points").icon("diamond").thresholdPoints(500L).build());
        }
    }

    private String computeLevel(Long points) {
        if (points >= 1000) return "Or";
        if (points >= 500) return "Argent";
        if (points >= 100) return "Bronze";
        return "Novice";
    }

    @Transactional
    public CitizenScore getOrCreateScore(Long userId) {
        return citizenScoreRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return citizenScoreRepository.save(CitizenScore.builder()
                            .user(user).points(0L).level("Novice").build());
                });
    }

    @Transactional
    public void addPointsForReport(Long userId) {
        CitizenScore score = getOrCreateScore(userId);
        score.setPoints(score.getPoints() + 10);
        score.setReportsCreated(score.getReportsCreated() + 1);
        score.setLevel(computeLevel(score.getPoints()));
        citizenScoreRepository.save(score);
        checkBadges(userId, score);
    }

    @Transactional
    public void addPointsForConfirmation(Long userId) {
        CitizenScore score = getOrCreateScore(userId);
        score.setPoints(score.getPoints() + 5);
        score.setConfirmationsGiven(score.getConfirmationsGiven() + 1);
        score.setLevel(computeLevel(score.getPoints()));
        citizenScoreRepository.save(score);
        checkBadges(userId, score);
    }

    @Transactional
    public void addPointsForComment(Long userId) {
        CitizenScore score = getOrCreateScore(userId);
        score.setPoints(score.getPoints() + 2);
        score.setCommentsCount(score.getCommentsCount() + 1);
        score.setLevel(computeLevel(score.getPoints()));
        citizenScoreRepository.save(score);
        checkBadges(userId, score);
    }

    private void checkBadges(Long userId, CitizenScore score) {
        List<Badge> allBadges = badgeRepository.findAll();
        citizenRepository.findFirstByUserIdOrderByIdDesc(userId).ifPresent(citizen -> {
            for (Badge badge : allBadges) {
                if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())
                        && score.getPoints() >= badge.getThresholdPoints()) {
                    userBadgeRepository.save(UserBadge.builder()
                            .user(userRepository.getReferenceById(userId))
                            .badge(badge).build());
                    notificationService.notifyBadgeEarned(citizen, badge.getName());
                }
            }
        });
    }

    public List<Map<String, Object>> getLeaderboard(int limit) {
        List<CitizenScore> scores = citizenScoreRepository.findTopScores();
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (CitizenScore cs : scores) {
            if (rank > limit) break;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", rank++);
            m.put("userId", cs.getUser().getId());
            m.put("userName", cs.getUser().getFirstName() + " " + cs.getUser().getLastName());
            m.put("points", cs.getPoints());
            m.put("level", cs.getLevel());
            m.put("reportsCreated", cs.getReportsCreated());
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> getUserScore(Long userId) {
        CitizenScore score = getOrCreateScore(userId);
        List<UserBadge> badges = userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
        List<Map<String, Object>> badgeList = new ArrayList<>();
        for (UserBadge ub : badges) {
            Map<String, Object> bm = new LinkedHashMap<>();
            bm.put("id", ub.getBadge().getId());
            bm.put("name", ub.getBadge().getName());
            bm.put("description", ub.getBadge().getDescription());
            bm.put("icon", ub.getBadge().getIcon());
            bm.put("earnedAt", ub.getEarnedAt().toString());
            badgeList.add(bm);
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", userId);
        m.put("points", score.getPoints());
        m.put("level", score.getLevel());
        m.put("reportsCreated", score.getReportsCreated());
        m.put("confirmationsGiven", score.getConfirmationsGiven());
        m.put("commentsCount", score.getCommentsCount());
        m.put("badges", badgeList);
        return m;
    }
}
