package tn.esprit.spring.smartcity.auth;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {
    List<RoleAssignment> findByUserId(Long userId);
    List<RoleAssignment> findByAssignedById(Long assignedById);
    long countByRoleIdAndRevokedAtIsNull(Long roleId);
}
