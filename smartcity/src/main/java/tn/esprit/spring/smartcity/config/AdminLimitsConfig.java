package tn.esprit.spring.smartcity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.limits")
@Data
public class AdminLimitsConfig {
    private int agentsPerDepartment = 10;
    private int maxAdmins = 2;
    private int maxMunicipality = 3;
    private int maxManagers = 8;
    private int maxAgentsTotal = 40;
}
