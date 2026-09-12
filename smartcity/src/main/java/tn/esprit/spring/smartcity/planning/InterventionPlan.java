package tn.esprit.spring.smartcity.planning;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.report.Report;
import java.time.LocalDateTime;

@Entity
@Table(name = "intervention_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterventionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @Column(nullable = false)
    private LocalDateTime plannedDate;

    @Column(nullable = false)
    private String timeSlot;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlanStatus status = PlanStatus.PLANNED;

    private LocalDateTime completedAt;

    private LocalDateTime validatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
