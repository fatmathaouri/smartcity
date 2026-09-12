package tn.esprit.spring.smartcity.planning;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> body) {
        try {
            Long reportId = Long.valueOf(body.get("reportId").toString());
            Long agentId = Long.valueOf(body.get("agentId").toString());
            LocalDateTime plannedDate = LocalDateTime.parse((String) body.get("plannedDate"));
            String timeSlot = (String) body.getOrDefault("timeSlot", "");
            String notes = (String) body.getOrDefault("notes", "");
            return ResponseEntity.ok(planningService.toDto(
                    planningService.createPlan(reportId, agentId, plannedDate, timeSlot, notes)));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("CONFLICT:")) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage().substring(9)));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Map<String, Object>>> getAgentPlans(@PathVariable Long agentId) {
        return ResponseEntity.ok(planningService.getPlansForAgent(agentId).stream()
                .map(planningService::toDto).toList());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyPlans(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(planningService.getPlansForAgent(user.getId()).stream()
                .map(planningService::toDto).toList());
    }

    @GetMapping("/week")
    public ResponseEntity<Map<String, Object>> getWeekOverview(
            @RequestParam(defaultValue = "") Long agentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(planningService.getWeekOverview(
                agentId != null && agentId > 0 ? agentId : null, weekStart));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Map<String, Object>>> getPlansInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(planningService.getPlansForDateRange(start, end).stream()
                .map(planningService::toDto).toList());
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Map<String, Object>>> getCompletedPlans() {
        return ResponseEntity.ok(planningService.getCompletedPlans());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlanningStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(planningService.getPlanningStats(weekStart));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(planningService.toDto(
                planningService.updateStatus(id, PlanStatus.valueOf(status))));
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validatePlan(
            @PathVariable Long id, Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(planningService.toDto(
                planningService.validatePlan(id, user.getId())));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectPlan(
            @PathVariable Long id, Authentication auth,
            @RequestBody(required = false) Map<String, String> body) {
        User user = getCurrentUser(auth);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(planningService.toDto(
                planningService.rejectPlan(id, user.getId(), reason)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePlan(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        LocalDateTime date = body.get("plannedDate") != null
                ? LocalDateTime.parse((String) body.get("plannedDate")) : null;
        String timeSlot = (String) body.get("timeSlot");
        String notes = (String) body.get("notes");
        return ResponseEntity.ok(planningService.toDto(
                planningService.updatePlan(id, date, timeSlot, notes)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        planningService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
