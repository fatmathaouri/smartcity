package tn.esprit.spring.smartcity.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentManagerRepository extends JpaRepository<DepartmentManager, Long> {
    Optional<DepartmentManager> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    Optional<DepartmentManager> findByServiceGere(String serviceGere);
    Optional<DepartmentManager> findByDepartmentId(Long departmentId);
    boolean existsByDepartmentId(Long departmentId);
}
