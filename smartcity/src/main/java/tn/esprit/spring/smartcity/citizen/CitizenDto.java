package tn.esprit.spring.smartcity.citizen;

import lombok.Data;

@Data
public class CitizenDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
}
