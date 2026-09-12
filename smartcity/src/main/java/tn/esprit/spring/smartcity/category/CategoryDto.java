package tn.esprit.spring.smartcity.category;

import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private String defaultPriority;
    private Integer slaHours;
    private Integer escalationHours;
}
