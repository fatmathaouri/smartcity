package tn.esprit.spring.smartcity.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MunicipalAgentRepository extends JpaRepository<MunicipalAgent, Long> {
    Optional<MunicipalAgent> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<MunicipalAgent> findByService(String service);
    List<MunicipalAgent> findByDisponibleTrue();
    List<MunicipalAgent> findByServiceAndDisponibleTrue(String service);
    long countByDepartmentId(Long departmentId);
    List<MunicipalAgent> findByDepartmentId(Long departmentId);
}
