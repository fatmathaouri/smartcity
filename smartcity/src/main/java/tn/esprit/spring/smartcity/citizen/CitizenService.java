package tn.esprit.spring.smartcity.citizen;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;

@Service
@RequiredArgsConstructor
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final UserRepository userRepository;

    public Citizen getCitizenByUserId(Long userId) {
        return citizenRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new RuntimeException("Citizen not found"));
    }

    public Citizen getCitizenByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return citizenRepository.findFirstByUserIdOrderByIdDesc(user.getId())
                .orElseThrow(() -> new RuntimeException("Citizen not found"));
    }

    public CitizenDto toDto(Citizen citizen) {
        CitizenDto dto = new CitizenDto();
        dto.setId(citizen.getId());
        dto.setFirstName(citizen.getUser().getFirstName());
        dto.setLastName(citizen.getUser().getLastName());
        dto.setEmail(citizen.getUser().getEmail());
        dto.setPhone(citizen.getUser().getPhone());
        dto.setAddress(citizen.getAddress());
        dto.setCity(citizen.getCity());
        return dto;
    }
}
