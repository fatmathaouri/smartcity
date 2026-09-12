package tn.esprit.spring.smartcity.gamification;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.auth.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Builder.Default
    private Long points = 0L;

    @Builder.Default
    private String level = "Bronze";

    @Builder.Default
    private Long reportsCreated = 0L;

    @Builder.Default
    private Long confirmationsGiven = 0L;

    @Builder.Default
    private Long commentsCount = 0L;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
