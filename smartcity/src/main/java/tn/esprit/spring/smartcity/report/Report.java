package tn.esprit.spring.smartcity.report;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.category.Category;
import tn.esprit.spring.smartcity.citizen.Citizen;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TreatmentStatus treatmentStatus = TreatmentStatus.NEW;

    private Double latitude;
    private Double longitude;
    private String photoUrl;
    private String address;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "citizen_id")
    private Citizen citizen;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "photo_before")
    private String photoBefore;

    @Column(name = "photo_after")
    private String photoAfter;

    @Column(columnDefinition = "TEXT", name = "agent_comment")
    private String agentComment;

    private LocalDateTime interventionDate;

    @Builder.Default
    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Builder.Default
    @Column(name = "confirm_count")
    private Long confirmCount = 0L;

    @Builder.Default
    @Column(name = "comment_count")
    private Long commentCount = 0L;

    private LocalDateTime slaDeadline;

    private LocalDateTime resolvedAt;

    @Builder.Default
    @Column(name = "sla_breached")
    private Boolean slaBreached = false;

    @ElementCollection
    @CollectionTable(name = "report_photos", joinColumns = @JoinColumn(name = "report_id"))
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    private String videoUrl;

    @Column(name = "proof_photo")
    private String proofPhoto;
}
