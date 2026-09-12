package tn.esprit.spring.smartcity.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByCitizenId(Long citizenId);

    @Query("SELECT r FROM Report r WHERE r.citizen.user.id = :userId")
    List<Report> findByCitizenUserId(@Param("userId") Long userId);
    List<Report> findByCategoryId(Long categoryId);
    List<Report> findByStatus(ReportStatus status);
    List<Report> findByAssignedToId(Long userId);
    List<Report> findByTreatmentStatus(TreatmentStatus treatmentStatus);

    @Query("SELECT r FROM Report r WHERE r.latitude BETWEEN :minLat AND :maxLat AND r.longitude BETWEEN :minLng AND :maxLng")
    List<Report> findByLocationBounds(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng,
        @Param("maxLng") Double maxLng
    );

    List<Report> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(ReportStatus status);

    @Query("SELECT r.category.id, COUNT(r) FROM Report r GROUP BY r.category.id")
    List<Object[]> countByCategory();

    @Query("SELECT FUNCTION('MONTH', r.createdAt), COUNT(r) FROM Report r GROUP BY FUNCTION('MONTH', r.createdAt)")
    List<Object[]> countByMonth();

    List<Report> findByAssignedToIdAndTreatmentStatus(Long userId, TreatmentStatus treatmentStatus);

    List<Report> findBySlaDeadlineBeforeAndSlaBreachedAndTreatmentStatusNotIn(
            LocalDateTime deadline, Boolean slaBreached, List<TreatmentStatus> treatmentStatuses);

    List<Report> findByAssignedToIsNullAndTreatmentStatus(TreatmentStatus treatmentStatus);

    List<Report> findByAssignedToIsNullAndCategoryIdIn(List<Long> categoryIds);

    @Query("SELECT r FROM Report r WHERE r.category.id = :categoryId AND r.assignedTo IS NULL AND r.treatmentStatus = :status")
    List<Report> findByCategoryIdAndAssignedToIsNullAndTreatmentStatus(
            @Param("categoryId") Long categoryId, @Param("status") TreatmentStatus status);
}
