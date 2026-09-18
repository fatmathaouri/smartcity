package tn.esprit.spring.smartcity.citizen;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    Optional<Citizen> findFirstByUserIdOrderByIdDesc(Long userId);
}
