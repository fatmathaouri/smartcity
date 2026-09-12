package tn.esprit.spring.smartcity.report;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.auth.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"report_id", "user_id", "vote_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
