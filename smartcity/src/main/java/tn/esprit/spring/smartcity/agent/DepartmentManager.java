package tn.esprit.spring.smartcity.agent;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.department.Department;

@Entity
@Table(name = "department_managers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false)
    private String serviceGere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    private Long appointedBy;
}
