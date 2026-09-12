package tn.esprit.spring.smartcity.citizen;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.spring.smartcity.auth.User;

@Entity
@Table(name = "citizens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Citizen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String address;
    private String city;
}
