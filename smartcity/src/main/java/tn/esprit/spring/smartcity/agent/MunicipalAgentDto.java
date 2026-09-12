package tn.esprit.spring.smartcity.agent;

import lombok.Data;

@Data
public class MunicipalAgentDto {
    private Long id;
    private Long userId;
    private String userName;
    private String email;
    private String service;
    private String zone;
    private String specialty;
    private Boolean disponible;
    private Long interventionsCount;
}
