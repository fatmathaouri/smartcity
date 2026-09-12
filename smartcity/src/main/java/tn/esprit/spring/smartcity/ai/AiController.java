package tn.esprit.spring.smartcity.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.dashboard.DashboardStatsDto;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final ClassificationService classificationService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final PredictionService predictionService;

    @PostMapping("/classify")
    public ResponseEntity<ClassificationService.ClassificationResult> classify(@RequestBody String text) {
        return ResponseEntity.ok(classificationService.classify(text));
    }

    @GetMapping("/duplicates/{reportId}")
    public ResponseEntity<List<DuplicateDetectionService.DuplicateResult>> findDuplicates(
            @PathVariable Long reportId) {
        return ResponseEntity.ok(duplicateDetectionService.findDuplicates(reportId));
    }

    @GetMapping("/predictions")
    public ResponseEntity<List<DashboardStatsDto.ZonePrediction>> getPredictions() {
        return ResponseEntity.ok(predictionService.predictProblemZones());
    }
}
