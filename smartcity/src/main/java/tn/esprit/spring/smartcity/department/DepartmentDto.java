package tn.esprit.spring.smartcity.department;

import lombok.Data;

@Data
public class DepartmentDto {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Boolean isActive;
    private Integer agentCount;
}
