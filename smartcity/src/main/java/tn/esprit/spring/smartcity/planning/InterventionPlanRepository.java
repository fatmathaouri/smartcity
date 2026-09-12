package tn.esprit.spring.smartcity.planning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface InterventionPlanRepository extends JpaRepository<InterventionPlan, Long> {
    List<InterventionPlan> findByAgentIdOrderByPlannedDateAsc(Long agentId);
    List<InterventionPlan> findByPlannedDateBetween(LocalDateTime start, LocalDateTime end);
    List<InterventionPlan> findByAgentIdAndPlannedDateBetween(Long agentId, LocalDateTime start, LocalDateTime end);
    List<InterventionPlan> findByStatus(PlanStatus status);

    @Query("SELECT ip FROM InterventionPlan ip WHERE ip.plannedDate BETWEEN :start AND :end ORDER BY ip.plannedDate ASC")
    List<InterventionPlan> findInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT ip FROM InterventionPlan ip WHERE ip.agent.id = :agentId " +
           "AND ip.status IN ('PLANNED', 'IN_PROGRESS') " +
           "AND ip.timeSlot = :timeSlot " +
           "AND CAST(ip.plannedDate AS LocalDate) = CAST(:plannedDate AS LocalDate)")
    List<InterventionPlan> findConflicts(@Param("agentId") Long agentId,
                                          @Param("plannedDate") LocalDateTime plannedDate,
                                          @Param("timeSlot") String timeSlot);

    @Query("SELECT ip FROM InterventionPlan ip WHERE ip.status = 'COMPLETED' " +
           "ORDER BY ip.completedAt DESC")
    List<InterventionPlan> findCompletedPlans();

    @Query("SELECT ip FROM InterventionPlan ip WHERE ip.status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED') " +
           "AND CAST(ip.plannedDate AS LocalDate) BETWEEN CAST(:start AS LocalDate) AND CAST(:end AS LocalDate) " +
           "ORDER BY ip.plannedDate ASC")
    List<InterventionPlan> findActiveInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
