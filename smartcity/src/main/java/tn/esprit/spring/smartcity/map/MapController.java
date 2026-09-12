package tn.esprit.spring.smartcity.map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.report.ReportDto;
import tn.esprit.spring.smartcity.report.ReportService;

import java.util.List;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final ReportService reportService;
    private final LocationService locationService;

    @GetMapping("/nearby")
    public ResponseEntity<List<ReportDto>> getNearbyReports(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5") Double radius) {
        return ResponseEntity.ok(reportService.toDtoList(reportService.getNearbyReports(lat, lng, radius)));
    }

    @GetMapping("/distance")
    public ResponseEntity<Double> calculateDistance(
            @RequestParam Double lat1,
            @RequestParam Double lng1,
            @RequestParam Double lat2,
            @RequestParam Double lng2) {
        return ResponseEntity.ok(locationService.calculateDistance(lat1, lng1, lat2, lng2));
    }
}
