package tn.esprit.spring.smartcity.agent;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.department.Department;

@Entity
@Table(name = "municipal_agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MunicipalAgent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false)
    private String service;

    private String zone;

    private String specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Builder.Default
    private Boolean disponible = true;

    @Builder.Default
    private Long interventionsCount = 0L;
}
