package tn.esprit.spring.smartcity.citizen;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/citizens")
@RequiredArgsConstructor
public class CitizenController {

    private final CitizenService citizenService;

    @GetMapping("/me")
    public ResponseEntity<CitizenDto> getCurrentCitizen(Authentication authentication) {
        Citizen citizen = citizenService.getCitizenByEmail(authentication.getName());
        return ResponseEntity.ok(citizenService.toDto(citizen));
    }
}
