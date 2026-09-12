package tn.esprit.spring.smartcity.category;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.report.Priority;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Priority defaultPriority;

    @Builder.Default
    private Integer slaHours = 168;

    @Builder.Default
    private Integer escalationHours = 72;
}
