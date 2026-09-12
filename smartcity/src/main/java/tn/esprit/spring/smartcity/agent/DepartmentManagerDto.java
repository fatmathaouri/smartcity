package tn.esprit.spring.smartcity.agent;

import lombok.Data;

@Data
public class DepartmentManagerDto {
    private Long id;
    private Long userId;
    private String userName;
    private String email;
    private String serviceGere;
    private Long departmentId;
}
