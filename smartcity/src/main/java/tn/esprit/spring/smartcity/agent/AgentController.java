package tn.esprit.spring.smartcity.agent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.auth.CreateAgentRequest;
import tn.esprit.spring.smartcity.auth.AuthService;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserCreationResult;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.report.ReportDto;
import tn.esprit.spring.smartcity.report.ReportService;
import tn.esprit.spring.smartcity.report.TreatmentStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final ReportService reportService;
    private final UserRepository userRepository;
    private final AuthService authService;

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<ReportDto>> getMyTasks(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.toDtoList(reportService.getReportsByAssignedTo(user.getId())));
    }

    @GetMapping("/tasks/pending")
    public ResponseEntity<List<ReportDto>> getPendingTasks(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.toDtoList(
                reportService.getReportsByAssignedToAndTreatmentStatus(user.getId(), TreatmentStatus.NEW)));
    }

    @GetMapping("/tasks/in-progress")
    public ResponseEntity<List<ReportDto>> getInProgressTasks(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.toDtoList(
                reportService.getReportsByAssignedToAndTreatmentStatus(user.getId(), TreatmentStatus.IN_PROGRESS)));
    }

    @GetMapping("/tasks/resolved")
    public ResponseEntity<List<ReportDto>> getResolvedTasks(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.toDtoList(
                reportService.getReportsByAssignedToAndTreatmentStatus(user.getId(), TreatmentStatus.RESOLVED)));
    }

    @GetMapping("/me")
    public ResponseEntity<MunicipalAgentDto> getMyProfile(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(agentService.toDto(agentService.getAgentByUserId(user.getId())));
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getMyPerformance(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(agentService.getAgentPerformance(user.getId()));
    }

    @PutMapping("/disponibilite")
    public ResponseEntity<MunicipalAgentDto> toggleDisponibilite(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(agentService.toDto(agentService.toggleDisponibilite(user.getId())));
    }

    @PostMapping("/create")
    public ResponseEntity<UserCreationResult> createAgent(
            @Valid @RequestBody CreateAgentRequest request,
            Authentication auth) {
        User manager = getCurrentUser(auth);
        return ResponseEntity.ok(authService.createUserByManager(
                request.getEmail(), request.getFirstName(), request.getLastName(),
                request.getPhone(), request.getDepartmentId(), manager.getId()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MunicipalAgentDto>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents().stream()
                .map(agentService::toDto)
                .toList());
    }

    @GetMapping("/available")
    public ResponseEntity<List<MunicipalAgentDto>> getAvailableAgents() {
        return ResponseEntity.ok(agentService.getAvailableAgents().stream()
                .map(agentService::toDto)
                .toList());
    }

    @GetMapping("/by-service")
    public ResponseEntity<List<MunicipalAgentDto>> getByService(@RequestParam String service) {
        return ResponseEntity.ok(agentService.getAgentsByService(service).stream()
                .map(agentService::toDto)
                .toList());
    }
}
