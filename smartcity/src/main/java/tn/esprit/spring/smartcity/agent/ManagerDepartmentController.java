package tn.esprit.spring.smartcity.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.report.ReportDto;
import tn.esprit.spring.smartcity.report.ReportService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class ManagerDepartmentController {

    private final ManagerDepartmentService departmentService;
    private final AgentService agentService;
    private final ReportService reportService;
    private final UserRepository userRepository;
    private final MunicipalAgentRepository agentRepository;

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/me")
    public ResponseEntity<DepartmentManagerDto> getMyProfile(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(departmentService.toDto(departmentService.getManagerByUserId(user.getId())));
    }

    @GetMapping("/agents")
    public ResponseEntity<List<MunicipalAgentDto>> getMyAgents(Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = departmentService.getManagerByUserId(user.getId());
        return ResponseEntity.ok(
                departmentService.getAgentsForService(manager.getServiceGere()).stream()
                        .map(agentService::toDto)
                        .toList());
    }

    @GetMapping("/my-agents")
    public ResponseEntity<List<MunicipalAgentDto>> getMyAgentsFiltered(Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = departmentService.getManagerByUserId(user.getId());
        return ResponseEntity.ok(
                departmentService.getAgentsForService(manager.getServiceGere()).stream()
                        .map(agentService::toDto)
                        .toList());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDepartmentStats(Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = departmentService.getManagerByUserId(user.getId());
        return ResponseEntity.ok(departmentService.getDepartmentStats(manager.getServiceGere()));
    }

    @GetMapping("/agent-performances")
    public ResponseEntity<List<Map<String, Object>>> getAgentPerformances(Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = departmentService.getManagerByUserId(user.getId());
        return ResponseEntity.ok(departmentService.getAgentPerformances(manager.getServiceGere()));
    }

    @PostMapping("/agents")
    public ResponseEntity<MunicipalAgentDto> addAgent(
            @RequestParam Long userId,
            @RequestParam String zone,
            @RequestParam(defaultValue = "") String specialty,
            Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = departmentService.getManagerByUserId(user.getId());
        return ResponseEntity.ok(agentService.toDto(
                agentService.createAgent(userId, manager.getServiceGere(), zone, specialty)));
    }

    @PutMapping("/assign")
    public ResponseEntity<ReportDto> assignAgent(
            @RequestParam Long reportId,
            @RequestParam Long agentId,
            Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = departmentService.getManagerByUserId(user.getId());

        MunicipalAgent agent = agentRepository.findByUserId(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        if (agent.getDepartment() == null || manager.getDepartment() == null
                || !agent.getDepartment().getId().equals(manager.getDepartment().getId())) {
            throw new RuntimeException("Cet agent n'appartient pas à votre département");
        }

        return ResponseEntity.ok(reportService.toDto(reportService.assignAgent(reportId, agentId)));
    }

    @GetMapping("/suggest-agent")
    public ResponseEntity<Map<String, Object>> suggestAgent(@RequestParam Long reportId, Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = departmentService.getManagerByUserId(user.getId());
        return ResponseEntity.ok(departmentService.suggestAgent(reportId, manager.getServiceGere()));
    }

    @PostMapping("/create")
    public ResponseEntity<DepartmentManagerDto> createManager(
            @RequestParam Long userId,
            @RequestParam String serviceGere) {
        return ResponseEntity.ok(departmentService.toDto(departmentService.createManager(userId, serviceGere)));
    }

    @GetMapping("/unassigned-reports")
    public ResponseEntity<List<ReportDto>> getUnassignedReports(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(
                reportService.toDtoList(departmentService.getUnassignedReports(user.getId())));
    }

    @DeleteMapping("/agents/{agentId}")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long agentId, Authentication auth) {
        User user = getCurrentUser(auth);
        departmentService.deleteAgent(agentId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/agents/{agentId}")
    public ResponseEntity<MunicipalAgentDto> updateAgent(
            @PathVariable Long agentId,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String specialty,
            Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(agentService.toDto(
                departmentService.updateAgent(agentId, zone, specialty, user.getId())));
    }
}
