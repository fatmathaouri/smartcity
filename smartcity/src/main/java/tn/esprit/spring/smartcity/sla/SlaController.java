package tn.esprit.spring.smartcity.sla;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sla")
@RequiredArgsConstructor
public class SlaController {

    private final SlaService slaService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSlaStats() {
        return ResponseEntity.ok(slaService.getSlaStats());
    }

    @GetMapping("/config")
    public ResponseEntity<List<Map<String, Object>>> getSlaConfig() {
        return ResponseEntity.ok(slaService.getSlaConfig());
    }

    @PutMapping("/config/{categoryId}")
    public ResponseEntity<Map<String, Object>> updateSlaConfig(
            @PathVariable Long categoryId,
            @RequestBody Map<String, Integer> body) {
        var category = slaService.updateSlaConfig(categoryId, body.get("slaHours"), body.get("escalationHours"));
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("categoryId", category.getId());
        result.put("categoryName", category.getName());
        result.put("slaHours", category.getSlaHours());
        result.put("escalationHours", category.getEscalationHours());
        return ResponseEntity.ok(result);
    }
}
