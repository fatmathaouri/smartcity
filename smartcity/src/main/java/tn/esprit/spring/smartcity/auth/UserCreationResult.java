package tn.esprit.spring.smartcity.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class UserCreationResult {
    private Long userId;
    private String email;
    private String username;
    private String temporaryPassword;
    private String role;
    private String departmentName;
    private LocalDateTime createdAt;
}
