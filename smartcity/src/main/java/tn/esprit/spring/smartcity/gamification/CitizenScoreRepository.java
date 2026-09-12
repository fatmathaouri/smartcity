package tn.esprit.spring.smartcity.gamification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CitizenScoreRepository extends JpaRepository<CitizenScore, Long> {
    Optional<CitizenScore> findByUserId(Long userId);
    @Query("SELECT cs FROM CitizenScore cs ORDER BY cs.points DESC")
    List<CitizenScore> findTopScores();
}
